package com.unomi.customer.profile;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Service;

import com.unomi.attribute.AttributeDefinitionService;
import com.unomi.condition.ConditionEvaluatorService;
import com.unomi.shared.NotFoundException;

@Service
public class CustomerProfileService {

    private final CustomerProfileRepository repository;
    private final AttributeDefinitionService attributeDefinitionService;
    private final ConditionEvaluatorService conditionEvaluatorService;

    public CustomerProfileService(
        CustomerProfileRepository repository,
        AttributeDefinitionService attributeDefinitionService,
        ConditionEvaluatorService conditionEvaluatorService
    ) {
        this.repository = repository;
        this.attributeDefinitionService = attributeDefinitionService;
        this.conditionEvaluatorService = conditionEvaluatorService;
    }

    public CustomerProfileResponse upsert(CustomerProfileRequest request) {
        Instant now = Instant.now();
        CustomerProfileDocument profile = repository.findByProfileKey(request.profileKey())
            .orElseGet(CustomerProfileDocument::new);

        if (profile.getId() == null) {
            profile.setId(UUID.randomUUID().toString());
            profile.setCreatedAt(now);
        }
        profile.setProfileKey(request.profileKey());
        profile.setAnonymousId(request.anonymousId());
        profile.setEmail(request.email());
        profile.setProperties(attributeDefinitionService.filterCustomerAttributes(request.properties()));
        profile.setUpdatedAt(now);

        return CustomerProfileResponse.from(repository.save(profile));
    }

    public CustomerProfileResponse get(String id) {
        return repository.findById(id)
            .map(CustomerProfileResponse::from)
            .orElseThrow(() -> new NotFoundException("Customer profile not found: " + id));
    }

    public CustomerProfileSearchResponse search(CustomerProfileConditionSearchRequest request, int page, int size) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.min(Math.max(size, 1), 200);

        if (request.condition() == null) {
            throw new IllegalArgumentException("condition is required");
        }

        List<CustomerProfileResponse> matchedProfiles = StreamSupport.stream(repository.findAll().spliterator(), false)
            .filter(profile -> conditionEvaluatorService.evaluate(request.condition(), profileContext(profile), Map.of()))
            .sorted(Comparator
                .comparing(CustomerProfileDocument::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(CustomerProfileDocument::getId, Comparator.nullsLast(String::compareTo)))
            .map(CustomerProfileResponse::from)
            .toList();

        int fromIndex = Math.min(normalizedPage * normalizedSize, matchedProfiles.size());
        int toIndex = Math.min(fromIndex + normalizedSize, matchedProfiles.size());
        List<CustomerProfileResponse> content = matchedProfiles.subList(fromIndex, toIndex);
        int totalPages = matchedProfiles.isEmpty()
            ? 0
            : (int) Math.ceil((double) matchedProfiles.size() / normalizedSize);

        return new CustomerProfileSearchResponse(
            content,
            normalizedPage,
            normalizedSize,
            matchedProfiles.size(),
            totalPages,
            normalizedPage == 0,
            totalPages == 0 || normalizedPage >= totalPages - 1
        );
    }

    private Map<String, Object> profileContext(CustomerProfileDocument profile) {
        Map<String, Object> context = new LinkedHashMap<>();
        context.put("id", profile.getId());
        context.put("profileKey", profile.getProfileKey());
        context.put("anonymousId", profile.getAnonymousId());
        context.put("email", profile.getEmail());
        context.put("phoneNumber", profile.getPhoneNumber());
        context.put("identifiers", nullToEmpty(profile.getIdentifiers()));
        context.put("properties", nullToEmpty(profile.getProperties()));
        context.put("segmentIds", nullToEmptyList(profile.getSegmentIds()));
        context.put("segmentKeys", nullToEmptyList(profile.getSegmentKeys()));
        context.put("tags", nullToEmptyList(profile.getTags()));
        context.put("scores", nullToEmpty(profile.getScores()));
        context.put("createdAt", profile.getCreatedAt());
        context.put("updatedAt", profile.getUpdatedAt());
        return context;
    }

    private Map<String, Object> nullToEmpty(Map<String, Object> value) {
        return value == null ? Map.of() : value;
    }

    private List<String> nullToEmptyList(List<String> value) {
        return value == null ? List.of() : value;
    }
}
