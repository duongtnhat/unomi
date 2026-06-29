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

@Service
public class UpsertCustomerInfoService {

    private static final int MAX_USERS_PER_REQUEST = 1_000;

    private final CustomerProfileRepository profileRepository;
    private final CustomerEventRepository eventRepository;
    private final AttributeDefinitionService attributeDefinitionService;
    private final CustomerProfileMergeService mergeService;
    private final SegmentMembershipService segmentMembershipService;

    public UpsertCustomerInfoService(
        CustomerProfileRepository profileRepository,
        CustomerEventRepository eventRepository,
        AttributeDefinitionService attributeDefinitionService,
        CustomerProfileMergeService mergeService,
        SegmentMembershipService segmentMembershipService
    ) {
        this.profileRepository = profileRepository;
        this.eventRepository = eventRepository;
        this.attributeDefinitionService = attributeDefinitionService;
        this.mergeService = mergeService;
        this.segmentMembershipService = segmentMembershipService;
    }

    public UpsertCustomerInfoResponse upsert(UpsertCustomerInfoRequest request) {
        if (request.users() == null || request.users().isEmpty()) {
            throw new IllegalArgumentException("users must be defined");
        }
        if (request.users().size() > MAX_USERS_PER_REQUEST) {
            throw new IllegalArgumentException("users must not contain more than 1000 records");
        }

        Map<String, List<String>> errors = new LinkedHashMap<>();
        List<String> successfulProfileIds = new ArrayList<>();

        for (int index = 0; index < request.users().size(); index++) {
            UpsertUserRequest user = request.users().get(index);
            List<String> userErrors = validateUser(index, user);
            if (!userErrors.isEmpty()) {
                for (String error : userErrors) {
                    addError(errors, "users." + index, error);
                }
                continue;
            }

            boolean skipHook = Boolean.TRUE.equals(request.skipHook());
            CustomerProfileDocument profile = upsertProfile(user, skipHook);
            upsertEvents(index, user, profile, errors);
            if (!skipHook) {
                profile = segmentMembershipService.updateMembership(profile, user.events());
            }
            successfulProfileIds.add(profile.getId());
        }

        int successCount = successfulProfileIds.size();
        int failCount = errors.isEmpty() ? 0 : errors.keySet().stream()
            .map(key -> key.split("\\.")[1])
            .distinct()
            .toList()
            .size();

        return new UpsertCustomerInfoResponse(new UpsertCustomerInfoResponse.Data(
            new UpsertCustomerInfoResponse.Successful(successCount, successfulProfileIds),
            new UpsertCustomerInfoResponse.Fail(failCount, errors)
        ));
    }

    private CustomerProfileDocument upsertProfile(UpsertUserRequest user, boolean skipHook) {
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
        CustomerProfileDocument savedProfile = profileRepository.save(profile);
        if (skipHook) {
            return savedProfile;
        }
        return mergeService.mergeIfNeeded(savedProfile, attributes);
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

    private void upsertEvents(
        int userIndex,
        UpsertUserRequest user,
        CustomerProfileDocument profile,
        Map<String, List<String>> errors
    ) {
        if (user.events() == null) {
            return;
        }

        for (int eventIndex = 0; eventIndex < user.events().size(); eventIndex++) {
            UpsertEventRequest request = user.events().get(eventIndex);
            if (!StringUtils.hasText(request.eventName())) {
                addError(errors, "users." + userIndex + ".events." + eventIndex + ".eventName.required",
                    "eventName is required");
                continue;
            }
            Instant occurredAt = parseTimestamp(request.timestamp());
            if (occurredAt == null) {
                addError(errors, "users." + userIndex + ".events." + eventIndex + ".timestamp.required",
                    "timestamp must be a valid RFC3339 datetime");
                continue;
            }

            CustomerEventDocument event = new CustomerEventDocument();
            Map<String, Object> payload = attributeDefinitionService.filterEventAttributes(safeMap(request.eventParams()));
            event.setId(UUID.randomUUID().toString());
            event.setProfileId(profile.getId());
            event.setEventType(request.eventName());
            event.setSource("upsert");
            event.setPayload(payload);
            event.setOccurredAt(occurredAt);
            event.setReceivedAt(Instant.now());
            eventRepository.save(event);
        }
    }

    private List<String> validateUser(int index, UpsertUserRequest user) {
        List<String> errors = new ArrayList<>();
        if (user == null) {
            errors.add("user must be an object");
            return errors;
        }
        if (!StringUtils.hasText(user.insiderId()) && safeMap(user.identifiers()).isEmpty()) {
            errors.add("either insiderId or identifiers must be specified");
        }
        if ((user.attributes() == null || user.attributes().isEmpty())
            && (user.events() == null || user.events().isEmpty())) {
            errors.add("each user must include attributes or events");
        }
        Object email = safeMap(user.identifiers()).get("email");
        if (email instanceof String value && !value.matches("^.+@.+\\..+$")) {
            errors.add("not a valid email address at users." + index + ".identifiers.email");
        }
        Object phoneNumber = safeMap(user.identifiers()).get("phoneNumber");
        if (phoneNumber instanceof String value && !value.matches("^\\+[1-9]\\d{6,14}$")) {
            errors.add("not a valid phone number at users." + index + ".identifiers.phoneNumber");
        }
        return errors;
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

    private void addError(Map<String, List<String>> errors, String key, String message) {
        errors.computeIfAbsent(key, ignored -> new ArrayList<>()).add(message);
    }
}
