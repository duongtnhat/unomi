package com.unomi.definition;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unomi.cache.MetadataCacheService;
import com.unomi.shared.NotFoundException;

@Service
public class DefinitionService {

    private final DefinitionRepository repository;
    private final MetadataCacheService cacheService;

    public DefinitionService(DefinitionRepository repository, MetadataCacheService cacheService) {
        this.repository = repository;
        this.cacheService = cacheService;
    }

    @Transactional
    public DefinitionResponse upsert(DefinitionRequest request) {
        DefinitionEntity entity = repository
            .findByKeyAndTypeAndVersion(request.key(), request.type(), request.version())
            .orElseGet(DefinitionEntity::new);

        entity.setKey(request.key());
        entity.setType(request.type());
        entity.setVersion(request.version());
        entity.setName(request.name());
        entity.setActive(request.active());
        entity.setPayload(request.payload());

        DefinitionResponse response = DefinitionResponse.from(repository.save(entity));
        refreshDefinitionsCache();
        return response;
    }

    @Transactional(readOnly = true)
    public DefinitionResponse get(UUID id) {
        return getCachedDefinitions().stream()
            .filter(definition -> definition.id().equals(id))
            .findFirst()
            .or(() -> repository.findById(id).map(DefinitionResponse::from))
            .orElseThrow(() -> new NotFoundException("Definition not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<DefinitionResponse> list(DefinitionType type, Boolean active) {
        return getCachedDefinitions()
            .stream()
            .filter(definition -> definition.type() == type)
            .filter(definition -> active == null || definition.active() == active)
            .toList();
    }

    private List<DefinitionResponse> getCachedDefinitions() {
        return cacheService.getDefinitions().orElseGet(this::refreshDefinitionsCache);
    }

    private List<DefinitionResponse> refreshDefinitionsCache() {
        List<DefinitionResponse> definitions = repository.findAll()
            .stream()
            .map(DefinitionResponse::from)
            .toList();
        cacheService.putDefinitions(definitions);
        return definitions;
    }
}
