package com.unomi.condition;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ConditionEvaluatorService {

    public boolean evaluate(ConditionNode condition, Map<String, Object> profile, Map<String, Object> event) {
        if (condition == null || !StringUtils.hasText(condition.type())) {
            return false;
        }

        return switch (condition.type()) {
            case "boolean" -> evaluateBoolean(condition, profile, event);
            case "profileProperty" -> evaluateProperty(profile, condition.parameters());
            case "eventProperty" -> evaluateProperty(event, condition.parameters());
            case "eventType" -> evaluateSimpleField(event, "eventType", condition.parameters());
            case "profileId" -> evaluateSimpleField(profile, "id", condition.parameters());
            case "exists" -> evaluateExists(condition.parameters(), profile, event);
            default -> false;
        };
    }

    private boolean evaluateBoolean(ConditionNode condition, Map<String, Object> profile, Map<String, Object> event) {
        String operator = stringParam(condition.parameters(), "operator", "and");
        List<ConditionNode> children = condition.conditions() == null ? List.of() : condition.conditions();

        return switch (operator) {
            case "and" -> children.stream().allMatch(child -> evaluate(child, profile, event));
            case "or" -> children.stream().anyMatch(child -> evaluate(child, profile, event));
            case "not" -> children.stream().findFirst().map(child -> !evaluate(child, profile, event)).orElse(false);
            default -> false;
        };
    }

    private boolean evaluateProperty(Map<String, Object> root, Map<String, Object> parameters) {
        String propertyName = stringParam(parameters, "propertyName", null);
        String operator = stringParam(parameters, "operator", "equals");
        if (!StringUtils.hasText(propertyName)) {
            return false;
        }
        Object actual = readPath(root, propertyName);
        Object expected = parameters == null ? null : parameters.get("value");
        return compare(actual, expected, operator);
    }

    private boolean evaluateSimpleField(Map<String, Object> root, String field, Map<String, Object> parameters) {
        String operator = stringParam(parameters, "operator", "equals");
        Object expected = parameters == null ? null : parameters.get("value");
        return compare(readPath(root, field), expected, operator);
    }

    private boolean evaluateExists(Map<String, Object> parameters, Map<String, Object> profile, Map<String, Object> event) {
        String target = stringParam(parameters, "target", "profile");
        String propertyName = stringParam(parameters, "propertyName", null);
        if (!StringUtils.hasText(propertyName)) {
            return false;
        }
        Map<String, Object> root = "event".equals(target) ? event : profile;
        return readPath(root, propertyName) != null;
    }

    private boolean compare(Object actual, Object expected, String operator) {
        return switch (operator) {
            case "equals" -> Objects.equals(normalize(actual), normalize(expected));
            case "notEquals" -> !Objects.equals(normalize(actual), normalize(expected));
            case "contains" -> contains(actual, expected);
            case "in" -> expected instanceof Collection<?> collection
                && collection.stream().anyMatch(value -> Objects.equals(normalize(actual), normalize(value)));
            case "gt" -> compareValues(actual, expected) > 0;
            case "gte" -> compareValues(actual, expected) >= 0;
            case "lt" -> compareValues(actual, expected) < 0;
            case "lte" -> compareValues(actual, expected) <= 0;
            case "exists" -> actual != null;
            case "missing" -> actual == null;
            default -> false;
        };
    }

    private boolean contains(Object actual, Object expected) {
        if (actual instanceof Collection<?> collection) {
            return collection.stream().anyMatch(value -> Objects.equals(normalize(value), normalize(expected)));
        }
        if (actual instanceof String text && expected != null) {
            return text.contains(String.valueOf(expected));
        }
        return false;
    }

    private int compareValues(Object actual, Object expected) {
        BigDecimal leftNumber = toNumber(actual);
        BigDecimal rightNumber = toNumber(expected);
        if (leftNumber != null && rightNumber != null) {
            return leftNumber.compareTo(rightNumber);
        }

        Instant leftInstant = toInstant(actual);
        Instant rightInstant = toInstant(expected);
        if (leftInstant != null && rightInstant != null) {
            return leftInstant.compareTo(rightInstant);
        }

        if (actual == null || expected == null) {
            return -1;
        }
        return String.valueOf(actual).compareTo(String.valueOf(expected));
    }

    private Object readPath(Map<String, Object> root, String path) {
        if (root == null || !StringUtils.hasText(path)) {
            return null;
        }
        Object current = root;
        for (String part : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = map.get(part);
        }
        return current;
    }

    private String stringParam(Map<String, Object> parameters, String key, String defaultValue) {
        if (parameters == null || parameters.get(key) == null) {
            return defaultValue;
        }
        return String.valueOf(parameters.get(key));
    }

    private Object normalize(Object value) {
        if (value instanceof Number number) {
            return toNumber(number);
        }
        Instant instant = toInstant(value);
        return instant == null ? value : instant;
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

    private Instant toInstant(Object value) {
        if (value instanceof Instant instant) {
            return instant;
        }
        if (!(value instanceof String text)) {
            return null;
        }
        try {
            return OffsetDateTime.parse(text).toInstant();
        } catch (DateTimeParseException exception) {
            try {
                return Instant.parse(text);
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }
}
