package com.unomi.customer.upsert;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.unomi.customer.upsert.messaging.UpsertCustomerCommand;
import com.unomi.customer.upsert.messaging.UpsertCustomerCommandPublisher;

@Service
public class UpsertCustomerInfoService {

    private static final int MAX_USERS_PER_REQUEST = 1_000;

    private final UpsertCustomerCommandPublisher publisher;

    public UpsertCustomerInfoService(UpsertCustomerCommandPublisher publisher) {
        this.publisher = publisher;
    }

    public UpsertCustomerInfoResponse upsert(UpsertCustomerInfoRequest request) {
        if (request.users() == null || request.users().isEmpty()) {
            throw new IllegalArgumentException("users must be defined");
        }
        if (request.users().size() > MAX_USERS_PER_REQUEST) {
            throw new IllegalArgumentException("users must not contain more than 1000 records");
        }

        Map<String, List<String>> errors = new LinkedHashMap<>();
        List<String> messageIds = new ArrayList<>();
        List<String> knownProfileIds = new ArrayList<>();
        boolean skipHook = Boolean.TRUE.equals(request.skipHook());

        for (int index = 0; index < request.users().size(); index++) {
            UpsertUserRequest user = request.users().get(index);
            List<String> userErrors = validateUser(index, user);
            if (!userErrors.isEmpty()) {
                for (String error : userErrors) {
                    addError(errors, "users." + index, error);
                }
                continue;
            }

            String messageId = UUID.randomUUID().toString();
            publisher.publish(new UpsertCustomerCommand(messageId, Instant.now(), skipHook, user));
            messageIds.add(messageId);
            if (StringUtils.hasText(user.insiderId())) {
                knownProfileIds.add(user.insiderId());
            }
        }

        int failCount = errors.isEmpty() ? 0 : errors.keySet().stream()
            .map(key -> key.split("\\.")[1])
            .distinct()
            .toList()
            .size();

        return new UpsertCustomerInfoResponse(new UpsertCustomerInfoResponse.Data(
            new UpsertCustomerInfoResponse.Successful(messageIds.size(), knownProfileIds, messageIds),
            new UpsertCustomerInfoResponse.Fail(failCount, errors)
        ));
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

    private Map<String, Object> safeMap(Map<String, Object> map) {
        return map == null ? Map.of() : map;
    }

    private void addError(Map<String, List<String>> errors, String key, String message) {
        errors.computeIfAbsent(key, ignored -> new ArrayList<>()).add(message);
    }
}
