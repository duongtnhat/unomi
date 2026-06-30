package com.unomi.action;

import java.util.LinkedHashSet;
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
public class ActionTypeDefinitionService {

    private final ActionTypeDefinitionRepository repository;
    private final MetadataCacheService cacheService;

    public ActionTypeDefinitionService(
        ActionTypeDefinitionRepository repository,
        MetadataCacheService cacheService
    ) {
        this.repository = repository;
        this.cacheService = cacheService;
    }

    @Transactional
    public ActionTypeDefinitionResponse create(ActionTypeDefinitionRequest request) {
        validate(request);
        if (repository.existsByKey(request.key())) {
            throw new IllegalArgumentException("Action type key already exists: " + request.key());
        }

        ActionTypeDefinitionEntity entity = new ActionTypeDefinitionEntity();
        apply(entity, request);
        ActionTypeDefinitionResponse response = ActionTypeDefinitionResponse.from(repository.save(entity));
        refreshCache();
        return response;
    }

    @Transactional
    public ActionTypeDefinitionResponse update(UUID id, ActionTypeDefinitionRequest request) {
        validate(request);
        ActionTypeDefinitionEntity entity = repository.findById(id)
            .orElseThrow(() -> new NotFoundException("Action type definition not found: " + id));
        repository.findByKey(request.key())
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> {
                throw new IllegalArgumentException("Action type key already exists: " + request.key());
            });

        apply(entity, request);
        ActionTypeDefinitionResponse response = ActionTypeDefinitionResponse.from(repository.save(entity));
        refreshCache();
        return response;
    }

    @Transactional(readOnly = true)
    public ActionTypeDefinitionResponse get(UUID id) {
        return repository.findById(id)
            .map(ActionTypeDefinitionResponse::from)
            .orElseThrow(() -> new NotFoundException("Action type definition not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<ActionTypeDefinitionResponse> list(Boolean active) {
        return getCachedActionTypes().stream()
            .filter(actionType -> active == null || actionType.active() == active)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ActionParameterDefinition> params(UUID id) {
        return get(id).params();
    }

    @Transactional(readOnly = true)
    public Map<String, ActionTypeDefinitionResponse> activeByKey() {
        return list(true).stream()
            .collect(Collectors.toMap(ActionTypeDefinitionResponse::key, Function.identity()));
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Action type definition not found: " + id);
        }
        repository.deleteById(id);
        refreshCache();
    }

    private List<ActionTypeDefinitionResponse> getCachedActionTypes() {
        return cacheService.getActionTypes().orElseGet(this::refreshCache);
    }

    private List<ActionTypeDefinitionResponse> refreshCache() {
        List<ActionTypeDefinitionResponse> actionTypes = repository.findAllByOrderByKeyAsc()
            .stream()
            .map(ActionTypeDefinitionResponse::from)
            .toList();
        cacheService.putActionTypes(actionTypes);
        return actionTypes;
    }

    private void apply(ActionTypeDefinitionEntity entity, ActionTypeDefinitionRequest request) {
        entity.setKey(request.key());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setProcessingChannel(request.processingChannel());
        entity.setActive(request.active() == null || request.active());
        entity.setParams(request.params());
    }

    private void validate(ActionTypeDefinitionRequest request) {
        LinkedHashSet<String> keys = new LinkedHashSet<>();
        for (ActionParameterDefinition param : request.params()) {
            if (!keys.add(param.key())) {
                throw new IllegalArgumentException("Action parameter key already exists: " + param.key());
            }
        }
    }
}
