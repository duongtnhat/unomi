package com.unomi.scoring;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unomi.cache.MetadataCacheService;
import com.unomi.shared.NotFoundException;

@Service
public class ScoringDefinitionService {

    private final ScoringDefinitionRepository repository;
    private final MetadataCacheService cacheService;

    public ScoringDefinitionService(
        ScoringDefinitionRepository repository,
        MetadataCacheService cacheService
    ) {
        this.repository = repository;
        this.cacheService = cacheService;
    }

    @Transactional
    public ScoringDefinitionResponse create(ScoringDefinitionRequest request) {
        validate(request);
        if (repository.existsByKey(request.key())) {
            throw new IllegalArgumentException("Scoring key already exists: " + request.key());
        }

        ScoringDefinitionEntity entity = new ScoringDefinitionEntity();
        apply(entity, request);
        ScoringDefinitionResponse response = ScoringDefinitionResponse.from(repository.save(entity));
        refreshCache();
        return response;
    }

    @Transactional
    public ScoringDefinitionResponse update(UUID id, ScoringDefinitionRequest request) {
        validate(request);
        ScoringDefinitionEntity entity = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Scoring definition not found: " + id));
        repository.findByKey(request.key())
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> {
                throw new IllegalArgumentException("Scoring key already exists: " + request.key());
            });

        apply(entity, request);
        ScoringDefinitionResponse response = ScoringDefinitionResponse.from(repository.save(entity));
        refreshCache();
        return response;
    }

    @Transactional(readOnly = true)
    public ScoringDefinitionResponse get(UUID id) {
        return repository.findById(id)
            .map(ScoringDefinitionResponse::from)
            .orElseThrow(() -> new NotFoundException("Scoring definition not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<ScoringDefinitionResponse> list(Boolean active) {
        return getCachedScorings().stream()
            .filter(scoring -> active == null || scoring.active() == active)
            .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, ScoringDefinitionResponse> activeByKey() {
        return list(true).stream()
            .collect(Collectors.toMap(ScoringDefinitionResponse::key, Function.identity()));
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Scoring definition not found: " + id);
        }
        repository.deleteById(id);
        refreshCache();
    }

    private List<ScoringDefinitionResponse> getCachedScorings() {
        return cacheService.getScorings().orElseGet(this::refreshCache);
    }

    private List<ScoringDefinitionResponse> refreshCache() {
        List<ScoringDefinitionResponse> scorings = repository.findAllByOrderByKeyAsc()
            .stream()
            .map(ScoringDefinitionResponse::from)
            .toList();
        cacheService.putScorings(scorings);
        return scorings;
    }

    private void apply(ScoringDefinitionEntity entity, ScoringDefinitionRequest request) {
        entity.setKey(request.key());
        entity.setName(request.name());
        entity.setType(request.type());
        entity.setStartValue(request.startValue());
        entity.setMinValue(request.minValue());
        entity.setMaxValue(request.maxValue());
        entity.setOnlyIncrease(Boolean.TRUE.equals(request.onlyIncrease()));
        entity.setOnlyDecrease(Boolean.TRUE.equals(request.onlyDecrease()));
        entity.setActive(request.active() == null || request.active());
    }

    private void validate(ScoringDefinitionRequest request) {
        if (Boolean.TRUE.equals(request.onlyIncrease()) && Boolean.TRUE.equals(request.onlyDecrease())) {
            throw new IllegalArgumentException("onlyIncrease and onlyDecrease cannot both be true");
        }
        if (request.minValue() != null && request.maxValue() != null
            && request.minValue().compareTo(request.maxValue()) > 0) {
            throw new IllegalArgumentException("minValue must be less than or equal to maxValue");
        }
    }
}
