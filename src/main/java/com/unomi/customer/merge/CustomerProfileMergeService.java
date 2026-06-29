package com.unomi.customer.merge;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import com.unomi.attribute.AttributeDefinitionService;
import com.unomi.attribute.CustomerAttributeMergeStrategy;
import com.unomi.cache.CachedAttributeDefinition;
import com.unomi.customer.profile.CustomerProfileDocument;
import com.unomi.customer.profile.CustomerProfileRepository;

@Service
public class CustomerProfileMergeService {

    private final CustomerProfileRepository profileRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final AttributeDefinitionService attributeDefinitionService;

    public CustomerProfileMergeService(
        CustomerProfileRepository profileRepository,
        ElasticsearchOperations elasticsearchOperations,
        AttributeDefinitionService attributeDefinitionService
    ) {
        this.profileRepository = profileRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.attributeDefinitionService = attributeDefinitionService;
    }

    public CustomerProfileDocument mergeIfNeeded(CustomerProfileDocument currentProfile, Map<String, Object> attributes) {
        List<CachedAttributeDefinition> mergeDefinitions = attributeDefinitionService.getCustomerDefinitions()
            .stream()
            .filter(definition -> definition.mergePriority() != null)
            .filter(definition -> attributes.containsKey(definition.key()))
            .sorted(Comparator
                .comparing(CachedAttributeDefinition::mergePriority)
                .thenComparing(CachedAttributeDefinition::key))
            .toList();

        if (mergeDefinitions.isEmpty()) {
            return currentProfile;
        }

        List<CustomerProfileDocument> profilesToMerge = new ArrayList<>();
        profilesToMerge.add(currentProfile);

        for (CachedAttributeDefinition definition : mergeDefinitions) {
            List<CustomerProfileDocument> matches = findProfilesByAttribute(definition.key(), attributes.get(definition.key()))
                .stream()
                .filter(profile -> !Objects.equals(profile.getId(), currentProfile.getId()))
                .toList();
            if (!matches.isEmpty()) {
                profilesToMerge.addAll(matches);
                break;
            }
        }

        List<CustomerProfileDocument> uniqueProfiles = uniqueById(profilesToMerge);
        if (uniqueProfiles.size() < 2) {
            return currentProfile;
        }

        CustomerProfileDocument master = chooseMaster(uniqueProfiles);
        Map<String, Object> mergedProperties = mergeProperties(master, uniqueProfiles);
        master.setProperties(mergedProperties);
        master.setIdentifiers(mergeMaps(uniqueProfiles.stream().map(CustomerProfileDocument::getIdentifiers).toList()));
        master.setUpdatedAt(Instant.now());

        CustomerProfileDocument savedMaster = profileRepository.save(master);
        List<CustomerProfileDocument> profilesToDelete = uniqueProfiles.stream()
            .filter(profile -> !Objects.equals(profile.getId(), savedMaster.getId()))
            .toList();
        profileRepository.deleteAll(profilesToDelete);
        return savedMaster;
    }

    private List<CustomerProfileDocument> findProfilesByAttribute(String key, Object value) {
        Criteria criteria = Criteria.where("properties." + key).is(value);
        CriteriaQuery query = new CriteriaQuery(criteria);
        return elasticsearchOperations.search(query, CustomerProfileDocument.class)
            .stream()
            .map(SearchHit::getContent)
            .toList();
    }

    private CustomerProfileDocument chooseMaster(List<CustomerProfileDocument> profiles) {
        return profiles.stream()
            .min(Comparator
                .comparing((CustomerProfileDocument profile) -> profile.getCreatedAt() == null
                    ? Instant.EPOCH
                    : profile.getCreatedAt())
                .thenComparing(CustomerProfileDocument::getId))
            .orElseThrow();
    }

    private Map<String, Object> mergeProperties(
        CustomerProfileDocument master,
        List<CustomerProfileDocument> profiles
    ) {
        Map<String, CachedAttributeDefinition> definitions = new LinkedHashMap<>();
        attributeDefinitionService.getCustomerDefinitions()
            .forEach(definition -> definitions.put(definition.key(), definition));

        Set<String> keys = new LinkedHashSet<>();
        profiles.forEach(profile -> keys.addAll(profile.getProperties().keySet()));

        Map<String, Object> merged = new LinkedHashMap<>(master.getProperties());
        for (String key : keys) {
            CachedAttributeDefinition definition = definitions.get(key);
            if (definition == null) {
                continue;
            }
            List<CustomerProfileDocument> profilesWithValue = profiles.stream()
                .filter(profile -> profile.getProperties().containsKey(key))
                .toList();
            if (profilesWithValue.isEmpty()) {
                continue;
            }

            Object mergedValue = mergeValue(
                definition.mergeStrategy() == null ? CustomerAttributeMergeStrategy.SOURCE_PRIORITY : definition.mergeStrategy(),
                key,
                master,
                profilesWithValue
            );
            if (mergedValue != null) {
                merged.put(key, mergedValue);
            }
        }
        return merged;
    }

    private Object mergeValue(
        CustomerAttributeMergeStrategy strategy,
        String key,
        CustomerProfileDocument master,
        List<CustomerProfileDocument> profiles
    ) {
        return switch (strategy) {
            case SOURCE_PRIORITY -> master.getProperties().getOrDefault(key, profiles.getFirst().getProperties().get(key));
            case NEWEST_VALUE -> profiles.stream()
                .max(Comparator.comparing(this::updatedAt))
                .map(profile -> profile.getProperties().get(key))
                .orElse(null);
            case OLDEST_VALUE -> profiles.stream()
                .min(Comparator.comparing(this::updatedAt))
                .map(profile -> profile.getProperties().get(key))
                .orElse(null);
            case MAX_VALUE -> profiles.stream()
                .map(profile -> profile.getProperties().get(key))
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .mapToDouble(Number::doubleValue)
                .max()
                .stream()
                .boxed()
                .findFirst()
                .orElse(null);
            case MIN_VALUE -> profiles.stream()
                .map(profile -> profile.getProperties().get(key))
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .mapToDouble(Number::doubleValue)
                .min()
                .stream()
                .boxed()
                .findFirst()
                .orElse(null);
            case SUM -> profiles.stream()
                .map(profile -> profile.getProperties().get(key))
                .filter(Number.class::isInstance)
                .map(Number.class::cast)
                .mapToDouble(Number::doubleValue)
                .sum();
            case UNION -> unionValues(profiles.stream().map(profile -> profile.getProperties().get(key)).toList());
        };
    }

    private Instant updatedAt(CustomerProfileDocument profile) {
        return Optional.ofNullable(profile.getUpdatedAt()).orElse(Instant.EPOCH);
    }

    private List<Object> unionValues(List<Object> values) {
        Set<Object> merged = new LinkedHashSet<>();
        for (Object value : values) {
            if (value instanceof Collection<?> collection) {
                merged.addAll(collection);
            } else if (value != null) {
                merged.add(value);
            }
        }
        return new ArrayList<>(merged);
    }

    private Map<String, Object> mergeMaps(List<Map<String, Object>> maps) {
        Map<String, Object> merged = new LinkedHashMap<>();
        maps.stream()
            .filter(Objects::nonNull)
            .forEach(merged::putAll);
        return merged;
    }

    private List<CustomerProfileDocument> uniqueById(List<CustomerProfileDocument> profiles) {
        Map<String, CustomerProfileDocument> uniqueProfiles = new LinkedHashMap<>();
        profiles.forEach(profile -> uniqueProfiles.put(profile.getId(), profile));
        return new ArrayList<>(uniqueProfiles.values());
    }
}
