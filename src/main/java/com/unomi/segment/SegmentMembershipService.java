package com.unomi.segment;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unomi.attribute.AttributeDefinitionService;
import com.unomi.condition.ConditionEvaluatorService;
import com.unomi.condition.ConditionNode;
import com.unomi.customer.profile.CustomerProfileDocument;
import com.unomi.customer.profile.CustomerProfileRepository;
import com.unomi.customer.upsert.UpsertEventRequest;

@Service
public class SegmentMembershipService {

    private final SegmentDefinitionService segmentDefinitionService;
    private final ConditionEvaluatorService conditionEvaluatorService;
    private final CustomerProfileRepository profileRepository;
    private final AttributeDefinitionService attributeDefinitionService;
    private final ObjectMapper objectMapper;

    public SegmentMembershipService(
        SegmentDefinitionService segmentDefinitionService,
        ConditionEvaluatorService conditionEvaluatorService,
        CustomerProfileRepository profileRepository,
        AttributeDefinitionService attributeDefinitionService,
        ObjectMapper objectMapper
    ) {
        this.segmentDefinitionService = segmentDefinitionService;
        this.conditionEvaluatorService = conditionEvaluatorService;
        this.profileRepository = profileRepository;
        this.attributeDefinitionService = attributeDefinitionService;
        this.objectMapper = objectMapper;
    }

    public CustomerProfileDocument updateMembership(CustomerProfileDocument profile, List<UpsertEventRequest> events) {
        Map<String, Object> profileContext = profileContext(profile);
        List<Map<String, Object>> eventContexts = eventContexts(events);
        List<String> segmentIds = new ArrayList<>();
        List<String> segmentKeys = new ArrayList<>();

        for (SegmentDefinitionResponse segment : segmentDefinitionService.activeSegments()) {
            ConditionNode condition = objectMapper.convertValue(segment.conditionPayload(), ConditionNode.class);
            if (matches(condition, profileContext, eventContexts)) {
                segmentIds.add(segment.id().toString());
                segmentKeys.add(segment.key());
            }
        }

        profile.setSegmentIds(segmentIds);
        profile.setSegmentKeys(segmentKeys);
        profile.setUpdatedAt(Instant.now());
        return profileRepository.save(profile);
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
}
