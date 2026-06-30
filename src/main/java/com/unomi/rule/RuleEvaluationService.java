package com.unomi.rule;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unomi.action.messaging.ActionExecutionCommand;
import com.unomi.action.messaging.ActionExecutionCommandPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unomi.attribute.AttributeDefinitionService;
import com.unomi.condition.ConditionEvaluatorService;
import com.unomi.condition.ConditionNode;
import com.unomi.customer.event.CustomerEventDocument;
import com.unomi.customer.event.CustomerEventRepository;
import com.unomi.customer.profile.CustomerProfileDocument;
import com.unomi.customer.profile.CustomerProfileRepository;
import com.unomi.customer.upsert.UpsertEventRequest;
import com.unomi.scoring.ScoreOperation;
import com.unomi.scoring.ScoringDefinitionResponse;
import com.unomi.scoring.ScoringDefinitionService;

@Service
public class RuleEvaluationService {

    private final RuleDefinitionService ruleDefinitionService;
    private final ConditionEvaluatorService conditionEvaluatorService;
    private final CustomerProfileRepository profileRepository;
    private final AttributeDefinitionService attributeDefinitionService;
    private final RuleActionEventRepository actionEventRepository;
    private final ScoringDefinitionService scoringDefinitionService;
    private final CustomerEventRepository eventRepository;
    private final ActionExecutionCommandPublisher actionExecutionCommandPublisher;
    private final ObjectMapper objectMapper;

    public RuleEvaluationService(
        RuleDefinitionService ruleDefinitionService,
        ConditionEvaluatorService conditionEvaluatorService,
        CustomerProfileRepository profileRepository,
        AttributeDefinitionService attributeDefinitionService,
        RuleActionEventRepository actionEventRepository,
        ScoringDefinitionService scoringDefinitionService,
        CustomerEventRepository eventRepository,
        ActionExecutionCommandPublisher actionExecutionCommandPublisher,
        ObjectMapper objectMapper
    ) {
        this.ruleDefinitionService = ruleDefinitionService;
        this.conditionEvaluatorService = conditionEvaluatorService;
        this.profileRepository = profileRepository;
        this.attributeDefinitionService = attributeDefinitionService;
        this.actionEventRepository = actionEventRepository;
        this.scoringDefinitionService = scoringDefinitionService;
        this.eventRepository = eventRepository;
        this.actionExecutionCommandPublisher = actionExecutionCommandPublisher;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CustomerProfileDocument evaluate(String messageId, CustomerProfileDocument profile, List<UpsertEventRequest> events) {
        Map<String, Object> profileContext = profileContext(profile);
        List<Map<String, Object>> eventContexts = eventContexts(events);
        boolean changed = false;

        for (RuleDefinitionResponse rule : ruleDefinitionService.activeRules()) {
            ConditionNode condition = objectMapper.convertValue(rule.conditionPayload(), ConditionNode.class);
            if (!matches(condition, profileContext, eventContexts)) {
                continue;
            }

            changed = applyOutputs(messageId, profile, rule) || changed;
            profileContext = profileContext(profile);
        }

        if (changed) {
            profile.setUpdatedAt(Instant.now());
            return profileRepository.save(profile);
        }
        return profile;
    }

    private boolean applyOutputs(String messageId, CustomerProfileDocument profile, RuleDefinitionResponse rule) {
        boolean changed = false;
        Map<String, Object> outputs = rule.outputs() == null ? Map.of() : rule.outputs();

        Object attributes = outputs.get("attributes");
        if (attributes instanceof Map<?, ?> attributeMap) {
            Map<String, Object> incoming = new LinkedHashMap<>();
            attributeMap.forEach((key, value) -> incoming.put(String.valueOf(key), value));
            Map<String, Object> filtered = attributeDefinitionService.filterCustomerAttributes(incoming);
            if (!filtered.isEmpty()) {
                Map<String, Object> properties = new LinkedHashMap<>(nullToEmpty(profile.getProperties()));
                properties.putAll(filtered);
                profile.setProperties(properties);
                changed = true;
            }
        }

        Object tags = outputs.get("tags");
        if (tags instanceof Collection<?> tagCollection) {
            List<String> profileTags = new ArrayList<>(nullToEmptyList(profile.getTags()));
            for (Object tag : tagCollection) {
                if (tag != null && !profileTags.contains(String.valueOf(tag))) {
                    profileTags.add(String.valueOf(tag));
                    changed = true;
                }
            }
            profile.setTags(profileTags);
        }

        Object scores = outputs.get("scores");
        if (scores instanceof Map<?, ?> scoreMap) {
            changed = applyScores(messageId, profile, rule, scoreMap) || changed;
        }

        Object actions = outputs.get("actions");
        if (actions instanceof Collection<?> actionCollection) {
            for (Object action : actionCollection) {
                if (action instanceof Map<?, ?> actionMap) {
                    recordAction(messageId, profile.getId(), rule, actionMap);
                }
            }
        }

        return changed;
    }

    private boolean applyScores(
        String messageId,
        CustomerProfileDocument profile,
        RuleDefinitionResponse rule,
        Map<?, ?> scoreMap
    ) {
        Map<String, ScoringDefinitionResponse> definitions = scoringDefinitionService.activeByKey();
        Map<String, Object> profileScores = new LinkedHashMap<>(nullToEmpty(profile.getScores()));
        boolean changed = false;

        for (Map.Entry<?, ?> entry : scoreMap.entrySet()) {
            String scoreKey = String.valueOf(entry.getKey());
            ScoringDefinitionResponse definition = definitions.get(scoreKey);
            if (definition == null) {
                continue;
            }

            ScoreChangeRequest request = scoreChangeRequest(entry.getValue());
            if (request == null) {
                continue;
            }

            BigDecimal previousValue = toNumber(profileScores.get(scoreKey));
            if (previousValue == null) {
                previousValue = definition.startValue();
            }
            BigDecimal nextValue = nextScore(previousValue, request.operation(), request.value());
            nextValue = clamp(nextValue, definition);
            if (violatesDirection(previousValue, nextValue, definition)) {
                continue;
            }
            if (previousValue.compareTo(nextValue) == 0) {
                continue;
            }

            profileScores.put(scoreKey, nextValue);
            recordScoreChangedEvent(messageId, profile.getId(), rule.key(), scoreKey, request.operation(), previousValue, nextValue);
            changed = true;
        }

        if (changed) {
            profile.setScores(profileScores);
        }
        return changed;
    }

    private ScoreChangeRequest scoreChangeRequest(Object value) {
        if (value instanceof Map<?, ?> map) {
            Object operationValue = map.containsKey("operation") ? map.get("operation") : "INCREASE";
            Object scoreValue = map.get("value");
            BigDecimal amount = toNumber(scoreValue);
            if (amount == null) {
                return null;
            }
            ScoreOperation operation = scoreOperation(operationValue);
            if (operation == null) {
                return null;
            }
            return new ScoreChangeRequest(operation, amount);
        }

        BigDecimal amount = toNumber(value);
        if (amount == null) {
            return null;
        }
        return new ScoreChangeRequest(ScoreOperation.INCREASE, amount);
    }

    private ScoreOperation scoreOperation(Object value) {
        if (value == null) {
            return ScoreOperation.INCREASE;
        }
        String normalized = String.valueOf(value).trim().toUpperCase();
        if ("ASSIGN".equals(normalized)) {
            return ScoreOperation.SET;
        }
        if ("INCREMENT".equals(normalized)) {
            return ScoreOperation.INCREASE;
        }
        if ("DECREMENT".equals(normalized)) {
            return ScoreOperation.DECREASE;
        }
        try {
            return ScoreOperation.valueOf(normalized);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private BigDecimal nextScore(BigDecimal current, ScoreOperation operation, BigDecimal value) {
        return switch (operation) {
            case SET -> value;
            case INCREASE -> current.add(value);
            case DECREASE -> current.subtract(value);
        };
    }

    private BigDecimal clamp(BigDecimal value, ScoringDefinitionResponse definition) {
        BigDecimal next = value;
        if (definition.minValue() != null && next.compareTo(definition.minValue()) < 0) {
            next = definition.minValue();
        }
        if (definition.maxValue() != null && next.compareTo(definition.maxValue()) > 0) {
            next = definition.maxValue();
        }
        return next;
    }

    private boolean violatesDirection(
        BigDecimal previousValue,
        BigDecimal nextValue,
        ScoringDefinitionResponse definition
    ) {
        if (definition.onlyIncrease() && nextValue.compareTo(previousValue) < 0) {
            return true;
        }
        return definition.onlyDecrease() && nextValue.compareTo(previousValue) > 0;
    }

    private void recordScoreChangedEvent(
        String messageId,
        String profileId,
        String ruleKey,
        String scoreKey,
        ScoreOperation operation,
        BigDecimal previousValue,
        BigDecimal nextValue
    ) {
        CustomerEventDocument event = new CustomerEventDocument();
        event.setId(messageId + "-score-" + ruleKey + "-" + scoreKey);
        event.setProfileId(profileId);
        event.setEventType("scoreChanged");
        event.setSource("rule-engine");
        event.setOccurredAt(Instant.now());
        event.setReceivedAt(Instant.now());
        event.setPayload(Map.of(
            "messageId", messageId,
            "ruleKey", ruleKey,
            "scoreKey", scoreKey,
            "operation", operation.name(),
            "previousValue", previousValue,
            "newValue", nextValue,
            "delta", nextValue.subtract(previousValue)
        ));
        eventRepository.save(event);
    }

    private void recordAction(
        String messageId,
        String profileId,
        RuleDefinitionResponse rule,
        Map<?, ?> actionMap
    ) {
        RuleActionEventEntity entity = new RuleActionEventEntity();
        entity.setRuleId(rule.id());
        entity.setRuleKey(rule.key());
        entity.setMessageId(messageId);
        entity.setProfileId(profileId);
        entity.setTrackingId(java.util.UUID.randomUUID());
        Object actionKey = actionMap.containsKey("key") ? actionMap.get("key") : rule.key();
        Object actionType = actionMap.containsKey("type") ? actionMap.get("type") : "ACTION";
        entity.setActionKey(String.valueOf(actionKey));
        entity.setActionType(String.valueOf(actionType));
        Object payload = actionMap.get("payload");
        if (payload instanceof Map<?, ?> payloadMap) {
            Map<String, Object> mappedPayload = new LinkedHashMap<>();
            payloadMap.forEach((key, value) -> mappedPayload.put(String.valueOf(key), value));
            entity.setPayload(mappedPayload);
        } else {
            entity.setPayload(Map.of());
        }
        RuleActionEventEntity saved = actionEventRepository.save(entity);
        actionExecutionCommandPublisher.publish(new ActionExecutionCommand(
            saved.getId(),
            saved.getTrackingId(),
            messageId,
            Instant.now(),
            profileId,
            rule.key(),
            saved.getActionKey(),
            saved.getActionType(),
            saved.getPayload()
        ));
    }

    private boolean matches(
        ConditionNode condition,
        Map<String, Object> profileContext,
        List<Map<String, Object>> eventContexts
    ) {
        if (conditionEvaluatorService.evaluate(condition, profileContext, Map.of())) {
            return true;
        }
        return eventContexts.stream()
            .anyMatch(eventContext -> conditionEvaluatorService.evaluate(condition, profileContext, eventContext));
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

    private List<Map<String, Object>> eventContexts(List<UpsertEventRequest> events) {
        if (events == null || events.isEmpty()) {
            return List.of();
        }

        return events.stream()
            .filter(Objects::nonNull)
            .map(event -> {
                Map<String, Object> context = new LinkedHashMap<>();
                context.put("eventType", event.eventName());
                context.put("payload", attributeDefinitionService.filterEventAttributes(nullToEmpty(event.eventParams())));
                context.put("timestamp", event.timestamp());
                return context;
            })
            .toList();
    }

    private Map<String, Object> nullToEmpty(Map<String, Object> value) {
        return value == null ? Map.of() : value;
    }

    private List<String> nullToEmptyList(List<String> value) {
        return value == null ? List.of() : value;
    }

    private BigDecimal toNumber(Object value) {
        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        if (value instanceof String text) {
            try {
                return new BigDecimal(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private record ScoreChangeRequest(ScoreOperation operation, BigDecimal value) {
    }
}
