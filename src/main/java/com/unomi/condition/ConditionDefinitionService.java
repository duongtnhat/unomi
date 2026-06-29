package com.unomi.condition;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unomi.cache.MetadataCacheService;
import com.unomi.shared.NotFoundException;

@Service
public class ConditionDefinitionService {

    private final ConditionDefinitionRepository repository;
    private final MetadataCacheService cacheService;

    public ConditionDefinitionService(
        ConditionDefinitionRepository repository,
        MetadataCacheService cacheService
    ) {
        this.repository = repository;
        this.cacheService = cacheService;
    }

    @Transactional
    public ConditionDefinitionResponse upsert(ConditionDefinitionRequest request) {
        ConditionDefinitionEntity entity = repository.findByKeyAndVersion(request.key(), request.version())
            .orElseGet(ConditionDefinitionEntity::new);
        entity.setKey(request.key());
        entity.setVersion(request.version());
        entity.setName(request.name());
        entity.setActive(request.active());
        entity.setPayload(request.payload());

        ConditionDefinitionResponse response = ConditionDefinitionResponse.from(repository.save(entity));
        refreshCache();
        cacheService.evictSegments();
        return response;
    }

    @Transactional(readOnly = true)
    public ConditionDefinitionResponse get(UUID id) {
        return getCachedConditions().stream()
            .filter(condition -> condition.id().equals(id))
            .findFirst()
            .or(() -> repository.findById(id).map(ConditionDefinitionResponse::from))
            .orElseThrow(() -> new NotFoundException("Condition definition not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<ConditionDefinitionResponse> list(Boolean active) {
        return getCachedConditions()
            .stream()
            .filter(condition -> active == null || condition.active() == active)
            .toList();
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Condition definition not found: " + id);
        }
        repository.deleteById(id);
        refreshCache();
        cacheService.evictSegments();
    }

    private List<ConditionDefinitionResponse> getCachedConditions() {
        return cacheService.getConditions().orElseGet(this::refreshCache);
    }

    private List<ConditionDefinitionResponse> refreshCache() {
        List<ConditionDefinitionResponse> conditions = repository.findAllByOrderByKeyAscVersionDesc()
            .stream()
            .map(ConditionDefinitionResponse::from)
            .toList();
        cacheService.putConditions(conditions);
        return conditions;
    }
}
