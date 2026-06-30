package com.unomi.cache;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unomi.action.ActionTypeDefinitionResponse;
import com.unomi.condition.ConditionDefinitionResponse;
import com.unomi.definition.DefinitionResponse;
import com.unomi.rule.RuleDefinitionResponse;
import com.unomi.scoring.ScoringDefinitionResponse;
import com.unomi.segment.SegmentDefinitionResponse;

@Service
public class MetadataCacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataCacheService.class);

    private static final String CUSTOMER_ATTRIBUTES_KEY = "unomi:metadata:customer-attributes";
    private static final String EVENT_ATTRIBUTES_KEY = "unomi:metadata:event-attributes";
    private static final String DEFINITIONS_KEY = "unomi:metadata:definitions";
    private static final String CONDITIONS_KEY = "unomi:metadata:conditions";
    private static final String SEGMENTS_KEY = "unomi:metadata:segments";
    private static final String RULES_KEY = "unomi:metadata:rules";
    private static final String SCORINGS_KEY = "unomi:metadata:scorings";
    private static final String ACTION_TYPES_KEY = "unomi:metadata:action-types";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public MetadataCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void putCustomerAttributes(List<CachedAttributeDefinition> definitions) {
        put(CUSTOMER_ATTRIBUTES_KEY, definitions);
    }

    public Optional<List<CachedAttributeDefinition>> getCustomerAttributes() {
        return get(CUSTOMER_ATTRIBUTES_KEY, new TypeReference<>() {
        });
    }

    public void evictCustomerAttributes() {
        delete(CUSTOMER_ATTRIBUTES_KEY);
    }

    public void putEventAttributes(List<CachedAttributeDefinition> definitions) {
        put(EVENT_ATTRIBUTES_KEY, definitions);
    }

    public Optional<List<CachedAttributeDefinition>> getEventAttributes() {
        return get(EVENT_ATTRIBUTES_KEY, new TypeReference<>() {
        });
    }

    public void evictEventAttributes() {
        delete(EVENT_ATTRIBUTES_KEY);
    }

    public void putDefinitions(List<DefinitionResponse> definitions) {
        put(DEFINITIONS_KEY, definitions);
    }

    public Optional<List<DefinitionResponse>> getDefinitions() {
        return get(DEFINITIONS_KEY, new TypeReference<>() {
        });
    }

    public void evictDefinitions() {
        delete(DEFINITIONS_KEY);
    }

    public void putConditions(List<ConditionDefinitionResponse> conditions) {
        put(CONDITIONS_KEY, conditions);
    }

    public Optional<List<ConditionDefinitionResponse>> getConditions() {
        return get(CONDITIONS_KEY, new TypeReference<>() {
        });
    }

    public void evictConditions() {
        delete(CONDITIONS_KEY);
    }

    public void putSegments(List<SegmentDefinitionResponse> segments) {
        put(SEGMENTS_KEY, segments);
    }

    public Optional<List<SegmentDefinitionResponse>> getSegments() {
        return get(SEGMENTS_KEY, new TypeReference<>() {
        });
    }

    public void evictSegments() {
        delete(SEGMENTS_KEY);
    }

    public void putRules(List<RuleDefinitionResponse> rules) {
        put(RULES_KEY, rules);
    }

    public Optional<List<RuleDefinitionResponse>> getRules() {
        return get(RULES_KEY, new TypeReference<>() {
        });
    }

    public void evictRules() {
        delete(RULES_KEY);
    }

    public void putScorings(List<ScoringDefinitionResponse> scorings) {
        put(SCORINGS_KEY, scorings);
    }

    public Optional<List<ScoringDefinitionResponse>> getScorings() {
        return get(SCORINGS_KEY, new TypeReference<>() {
        });
    }

    public void evictScorings() {
        delete(SCORINGS_KEY);
    }

    public void putActionTypes(List<ActionTypeDefinitionResponse> actionTypes) {
        put(ACTION_TYPES_KEY, actionTypes);
    }

    public Optional<List<ActionTypeDefinitionResponse>> getActionTypes() {
        return get(ACTION_TYPES_KEY, new TypeReference<>() {
        });
    }

    public void evictActionTypes() {
        delete(ACTION_TYPES_KEY);
    }

    private void put(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value));
        } catch (Exception exception) {
            LOGGER.warn("Unable to write metadata cache key {}", key, exception);
        }
    }

    private <T> Optional<T> get(String key, TypeReference<T> typeReference) {
        try {
            String value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(value, typeReference));
        } catch (Exception exception) {
            LOGGER.warn("Unable to read metadata cache key {}", key, exception);
            return Optional.empty();
        }
    }

    private void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception exception) {
            LOGGER.warn("Unable to delete metadata cache key {}", key, exception);
        }
    }
}
