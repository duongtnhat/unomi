package com.unomi.scoring;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.unomi.customer.event.CustomerEventDocument;
import com.unomi.customer.event.CustomerEventRepository;
import com.unomi.customer.profile.CustomerProfileDocument;
import com.unomi.customer.profile.CustomerProfileRepository;
import com.unomi.shared.NotFoundException;

@Service
public class ProfileScoreService {

    private final CustomerProfileRepository profileRepository;
    private final CustomerEventRepository eventRepository;
    private final ScoringDefinitionService scoringDefinitionService;

    public ProfileScoreService(
        CustomerProfileRepository profileRepository,
        CustomerEventRepository eventRepository,
        ScoringDefinitionService scoringDefinitionService
    ) {
        this.profileRepository = profileRepository;
        this.eventRepository = eventRepository;
        this.scoringDefinitionService = scoringDefinitionService;
    }

    public ProfileScoreResponse getScores(String profileId) {
        CustomerProfileDocument profile = getProfile(profileId);
        return ProfileScoreResponse.scores(profile.getId(), new LinkedHashMap<>(nullToEmpty(profile.getScores())));
    }

    public ProfileScoreResponse clearScores(String profileId) {
        CustomerProfileDocument profile = getProfile(profileId);
        Map<String, Object> scores = new LinkedHashMap<>(nullToEmpty(profile.getScores()));
        if (scores.isEmpty()) {
            return ProfileScoreResponse.scores(profile.getId(), scores);
        }

        scores.forEach((scoreKey, value) -> {
            BigDecimal previousValue = toNumber(value);
            recordScoreChangedEvent(profile.getId(), scoreKey, "CLEAR", previousValue, null);
        });

        profile.setScores(new LinkedHashMap<>());
        profile.setUpdatedAt(Instant.now());
        profileRepository.save(profile);
        return new ProfileScoreResponse(profile.getId(), Map.of(), null, null, null, true);
    }

    public ProfileScoreResponse updateScore(String profileId, String scoreKey, ProfileScoreRequest request) {
        CustomerProfileDocument profile = getProfile(profileId);
        ScoringDefinitionResponse definition = scoringDefinitionService.activeByKey().get(scoreKey);
        if (definition == null) {
            throw new NotFoundException("Active scoring definition not found: " + scoreKey);
        }

        Map<String, Object> scores = new LinkedHashMap<>(nullToEmpty(profile.getScores()));
        BigDecimal previousValue = toNumber(scores.get(scoreKey));
        if (previousValue == null) {
            previousValue = definition.startValue();
        }
        BigDecimal nextValue = clamp(nextScore(previousValue, request.operation(), request.value()), definition);
        if (violatesDirection(previousValue, nextValue, definition)) {
            return new ProfileScoreResponse(profile.getId(), scores, scoreKey, previousValue, previousValue, false);
        }
        if (previousValue.compareTo(nextValue) == 0) {
            return new ProfileScoreResponse(profile.getId(), scores, scoreKey, previousValue, nextValue, false);
        }

        scores.put(scoreKey, nextValue);
        profile.setScores(scores);
        profile.setUpdatedAt(Instant.now());
        profileRepository.save(profile);
        recordScoreChangedEvent(profile.getId(), scoreKey, request.operation().name(), previousValue, nextValue);
        return new ProfileScoreResponse(profile.getId(), scores, scoreKey, previousValue, nextValue, true);
    }

    private CustomerProfileDocument getProfile(String profileId) {
        return profileRepository.findById(profileId)
            .orElseThrow(() -> new NotFoundException("Profile not found: " + profileId));
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
        String profileId,
        String scoreKey,
        String operation,
        BigDecimal previousValue,
        BigDecimal newValue
    ) {
        Instant now = Instant.now();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("scoreKey", scoreKey);
        payload.put("operation", operation);
        payload.put("previousValue", previousValue);
        payload.put("newValue", newValue);
        if (previousValue != null && newValue != null) {
            payload.put("delta", newValue.subtract(previousValue));
        }

        CustomerEventDocument event = new CustomerEventDocument();
        event.setId(UUID.randomUUID().toString());
        event.setProfileId(profileId);
        event.setEventType("scoreChanged");
        event.setSource("scoring-api");
        event.setPayload(payload);
        event.setOccurredAt(now);
        event.setReceivedAt(now);
        eventRepository.save(event);
    }

    private Map<String, Object> nullToEmpty(Map<String, Object> value) {
        return value == null ? Map.of() : value;
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
}
