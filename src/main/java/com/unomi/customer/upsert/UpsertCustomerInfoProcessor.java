package com.unomi.customer.upsert;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.unomi.attribute.AttributeDefinitionService;
import com.unomi.customer.event.CustomerEventDocument;
import com.unomi.customer.event.CustomerEventRepository;
import com.unomi.customer.merge.CustomerProfileMergeService;
import com.unomi.customer.profile.CustomerProfileDocument;
import com.unomi.customer.profile.CustomerProfileRepository;
import com.unomi.segment.SegmentMembershipService;
import com.unomi.customer.upsert.messaging.ElasticsearchWriteCompletedCommand;
import com.unomi.customer.upsert.messaging.ProfileMergeCompletedCommand;
import com.unomi.customer.upsert.messaging.RuleEvaluationCommand;
import com.unomi.customer.upsert.messaging.UpsertCustomerCommand;
import com.unomi.rule.RuleEvaluationService;

@Service
public class UpsertCustomerInfoProcessor {

    private final CustomerProfileRepository profileRepository;
    private final CustomerEventRepository eventRepository;
    private final AttributeDefinitionService attributeDefinitionService;
    private final CustomerProfileMergeService mergeService;
    private final SegmentMembershipService segmentMembershipService;
    private final RuleEvaluationService ruleEvaluationService;

    public UpsertCustomerInfoProcessor(
        CustomerProfileRepository profileRepository,
        CustomerEventRepository eventRepository,
        AttributeDefinitionService attributeDefinitionService,
        CustomerProfileMergeService mergeService,
        SegmentMembershipService segmentMembershipService,
        RuleEvaluationService ruleEvaluationService
    ) {
        this.profileRepository = profileRepository;
        this.eventRepository = eventRepository;
        this.attributeDefinitionService = attributeDefinitionService;
        this.mergeService = mergeService;
        this.segmentMembershipService = segmentMembershipService;
        this.ruleEvaluationService = ruleEvaluationService;
    }

    public ElasticsearchWriteCompletedCommand writeElasticsearch(UpsertCustomerCommand command) {
        CustomerProfileDocument profile = upsertProfile(command.user());
        upsertEvents(command.messageId(), command.user(), profile);
        return new ElasticsearchWriteCompletedCommand(
            command.messageId(),
            Instant.now(),
            profile.getId(),
            command.user()
        );
    }

    public ProfileMergeCompletedCommand mergeProfile(ElasticsearchWriteCompletedCommand command) {
        CustomerProfileDocument profile = profileRepository.findById(command.profileId())
            .orElseThrow(() -> new IllegalArgumentException("Profile not found for merge: " + command.profileId()));
        Map<String, Object> attributes = attributeDefinitionService.filterCustomerAttributes(safeMap(command.user().attributes()));
        CustomerProfileDocument mergedProfile = mergeService.mergeIfNeeded(profile, attributes);
        return new ProfileMergeCompletedCommand(
            command.messageId(),
            Instant.now(),
            mergedProfile.getId(),
            command.user()
        );
    }

    public CustomerProfileDocument qualifySegments(ProfileMergeCompletedCommand command) {
        CustomerProfileDocument profile = profileRepository.findById(command.profileId())
            .orElseThrow(() -> new IllegalArgumentException("Profile not found for segment qualification: " + command.profileId()));
        return segmentMembershipService.updateMembership(profile, command.user().events());
    }

    public RuleEvaluationCommand ruleEvaluationCommand(ProfileMergeCompletedCommand command) {
        return new RuleEvaluationCommand(command.messageId(), Instant.now(), command.profileId(), command.user());
    }

    public CustomerProfileDocument evaluateRules(RuleEvaluationCommand command) {
        CustomerProfileDocument profile = profileRepository.findById(command.profileId())
            .orElseThrow(() -> new IllegalArgumentException("Profile not found for rule evaluation: " + command.profileId()));
        return ruleEvaluationService.evaluate(command.messageId(), profile, command.user().events());
    }

    private CustomerProfileDocument upsertProfile(UpsertUserRequest user) {
        Instant now = Instant.now();
        String profileKey = profileKey(user);
        CustomerProfileDocument profile = findExistingProfile(user, profileKey)
            .orElseGet(CustomerProfileDocument::new);

        if (profile.getId() == null) {
            profile.setId(StringUtils.hasText(user.insiderId()) ? user.insiderId() : UUID.randomUUID().toString());
            profile.setCreatedAt(now);
        }

        profile.setProfileKey(profileKey);
        profile.setIdentifiers(new LinkedHashMap<>(safeMap(user.identifiers())));

        Object email = safeMap(user.identifiers()).get("email");
        if (email instanceof String value) {
            profile.setEmail(value);
        }
        Object phoneNumber = safeMap(user.identifiers()).get("phoneNumber");
        if (phoneNumber instanceof String value) {
            profile.setPhoneNumber(value);
        }

        Map<String, Object> attributes = attributeDefinitionService.filterCustomerAttributes(safeMap(user.attributes()));
        profile.setProperties(mergeAttributes(profile.getProperties(), attributes, shouldAppend(user)));
        profile.setUpdatedAt(now);
        return profileRepository.save(profile);
    }

    private Optional<CustomerProfileDocument> findExistingProfile(UpsertUserRequest user, String profileKey) {
        if (StringUtils.hasText(user.insiderId())) {
            Optional<CustomerProfileDocument> byId = profileRepository.findById(user.insiderId());
            if (byId.isPresent()) {
                return byId;
            }
        }

        Optional<CustomerProfileDocument> byProfileKey = profileRepository.findByProfileKey(profileKey);
        if (byProfileKey.isPresent()) {
            return byProfileKey;
        }

        Object email = safeMap(user.identifiers()).get("email");
        if (email instanceof String value) {
            Optional<CustomerProfileDocument> byEmail = profileRepository.findByEmail(value);
            if (byEmail.isPresent()) {
                return byEmail;
            }
        }

        Object phoneNumber = safeMap(user.identifiers()).get("phoneNumber");
        if (phoneNumber instanceof String value) {
            return profileRepository.findByPhoneNumber(value);
        }

        return Optional.empty();
    }

    private void upsertEvents(String messageId, UpsertUserRequest user, CustomerProfileDocument profile) {
        if (user.events() == null) {
            return;
        }

        for (int eventIndex = 0; eventIndex < user.events().size(); eventIndex++) {
            UpsertEventRequest request = user.events().get(eventIndex);
            if (!StringUtils.hasText(request.eventName())) {
                throw new IllegalArgumentException("eventName is required at events." + eventIndex);
            }
            Instant occurredAt = parseTimestamp(request.timestamp());
            if (occurredAt == null) {
                throw new IllegalArgumentException("timestamp must be a valid RFC3339 datetime at events." + eventIndex);
            }

            CustomerEventDocument event = new CustomerEventDocument();
            Map<String, Object> payload = attributeDefinitionService.filterEventAttributes(safeMap(request.eventParams()));
            event.setId(messageId + "-event-" + eventIndex);
            event.setProfileId(profile.getId());
            event.setEventType(request.eventName());
            event.setSource("upsert");
            event.setPayload(payload);
            event.setOccurredAt(occurredAt);
            event.setReceivedAt(Instant.now());
            eventRepository.save(event);
        }
    }

    private String profileKey(UpsertUserRequest user) {
        if (StringUtils.hasText(user.insiderId())) {
            return "insiderId:" + user.insiderId();
        }

        Map<String, Object> identifiers = safeMap(user.identifiers());
        for (String key : List.of("uuid", "email", "phoneNumber")) {
            Object value = identifiers.get(key);
            if (value instanceof String text && StringUtils.hasText(text)) {
                return key + ":" + text;
            }
        }

        Object custom = identifiers.get("custom");
        if (custom instanceof Map<?, ?> customIdentifiers && !customIdentifiers.isEmpty()) {
            Map.Entry<?, ?> entry = customIdentifiers.entrySet().iterator().next();
            return "custom:" + entry.getKey() + ":" + entry.getValue();
        }

        return UUID.randomUUID().toString();
    }

    private Map<String, Object> mergeAttributes(
        Map<String, Object> existing,
        Map<String, Object> incoming,
        boolean append
    ) {
        Map<String, Object> merged = new LinkedHashMap<>(safeMap(existing));
        for (Map.Entry<String, Object> entry : incoming.entrySet()) {
            Object current = merged.get(entry.getKey());
            Object next = entry.getValue();
            if (append && current instanceof Collection<?> currentValues && next instanceof Collection<?> nextValues) {
                List<Object> values = new ArrayList<>(currentValues);
                values.addAll(nextValues);
                merged.put(entry.getKey(), values);
            } else {
                merged.put(entry.getKey(), next);
            }
        }
        return merged;
    }

    private boolean shouldAppend(UpsertUserRequest user) {
        if (user.append() != null) {
            return user.append();
        }
        return !Boolean.TRUE.equals(user.notAppend());
    }

    private Instant parseTimestamp(String timestamp) {
        if (!StringUtils.hasText(timestamp)) {
            return null;
        }
        try {
            return OffsetDateTime.parse(timestamp).toInstant();
        } catch (DateTimeParseException exception) {
            try {
                return Instant.parse(timestamp);
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    private Map<String, Object> safeMap(Map<String, Object> map) {
        return map == null ? Map.of() : map;
    }

}
