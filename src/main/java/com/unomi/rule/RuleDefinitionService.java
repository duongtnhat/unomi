package com.unomi.rule;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unomi.cache.MetadataCacheService;
import com.unomi.condition.ConditionDefinitionRepository;
import com.unomi.shared.NotFoundException;

@Service
public class RuleDefinitionService {

    private final RuleDefinitionRepository repository;
    private final ConditionDefinitionRepository conditionRepository;
    private final MetadataCacheService cacheService;

    public RuleDefinitionService(
        RuleDefinitionRepository repository,
        ConditionDefinitionRepository conditionRepository,
        MetadataCacheService cacheService
    ) {
        this.repository = repository;
        this.conditionRepository = conditionRepository;
        this.cacheService = cacheService;
    }

    @Transactional
    public RuleDefinitionResponse upsert(RuleDefinitionRequest request) {
        RuleDefinitionEntity entity = repository.findByKey(request.key())
            .orElseGet(RuleDefinitionEntity::new);
        entity.setKey(request.key());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setCondition(conditionRepository.findById(request.conditionId())
            .orElseThrow(() -> new NotFoundException("Condition definition not found: " + request.conditionId())));
        entity.setPriority(request.priority());
        entity.setActive(request.active());
        entity.setOutputs(request.outputs());

        RuleDefinitionResponse response = RuleDefinitionResponse.from(repository.save(entity));
        refreshCache();
        return response;
    }

    @Transactional(readOnly = true)
    public RuleDefinitionResponse get(UUID id) {
        return getCachedRules().stream()
            .filter(rule -> rule.id().equals(id))
            .findFirst()
            .or(() -> repository.findById(id).map(RuleDefinitionResponse::from))
            .orElseThrow(() -> new NotFoundException("Rule definition not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<RuleDefinitionResponse> list(Boolean active) {
        return getCachedRules().stream()
            .filter(rule -> active == null || rule.active() == active)
            .toList();
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Rule definition not found: " + id);
        }
        repository.deleteById(id);
        refreshCache();
    }

    public List<RuleDefinitionResponse> activeRules() {
        return list(true);
    }

    private List<RuleDefinitionResponse> getCachedRules() {
        return cacheService.getRules().orElseGet(this::refreshCache);
    }

    private List<RuleDefinitionResponse> refreshCache() {
        List<RuleDefinitionResponse> rules = repository.findAllByOrderByPriorityAscKeyAsc()
            .stream()
            .map(RuleDefinitionResponse::from)
            .toList();
        cacheService.putRules(rules);
        return rules;
    }
}
