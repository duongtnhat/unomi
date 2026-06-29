package com.unomi.segment;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unomi.cache.MetadataCacheService;
import com.unomi.condition.ConditionDefinitionRepository;
import com.unomi.shared.NotFoundException;

@Service
public class SegmentDefinitionService {

    private final SegmentDefinitionRepository repository;
    private final ConditionDefinitionRepository conditionRepository;
    private final MetadataCacheService cacheService;

    public SegmentDefinitionService(
        SegmentDefinitionRepository repository,
        ConditionDefinitionRepository conditionRepository,
        MetadataCacheService cacheService
    ) {
        this.repository = repository;
        this.conditionRepository = conditionRepository;
        this.cacheService = cacheService;
    }

    @Transactional
    public SegmentDefinitionResponse upsert(SegmentDefinitionRequest request) {
        SegmentDefinitionEntity entity = repository.findByKey(request.key())
            .orElseGet(SegmentDefinitionEntity::new);
        entity.setKey(request.key());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setCondition(conditionRepository.findById(request.conditionId())
            .orElseThrow(() -> new NotFoundException("Condition definition not found: " + request.conditionId())));
        entity.setActive(request.active());

        SegmentDefinitionResponse response = SegmentDefinitionResponse.from(repository.save(entity));
        refreshCache();
        return response;
    }

    @Transactional(readOnly = true)
    public SegmentDefinitionResponse get(UUID id) {
        return getCachedSegments().stream()
            .filter(segment -> segment.id().equals(id))
            .findFirst()
            .or(() -> repository.findById(id).map(SegmentDefinitionResponse::from))
            .orElseThrow(() -> new NotFoundException("Segment definition not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<SegmentDefinitionResponse> list(Boolean active) {
        return getCachedSegments().stream()
            .filter(segment -> active == null || segment.active() == active)
            .toList();
    }

    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Segment definition not found: " + id);
        }
        repository.deleteById(id);
        refreshCache();
    }

    public List<SegmentDefinitionResponse> activeSegments() {
        return list(true);
    }

    private List<SegmentDefinitionResponse> getCachedSegments() {
        return cacheService.getSegments().orElseGet(this::refreshCache);
    }

    private List<SegmentDefinitionResponse> refreshCache() {
        List<SegmentDefinitionResponse> segments = repository.findAllByOrderByKeyAsc()
            .stream()
            .map(SegmentDefinitionResponse::from)
            .toList();
        cacheService.putSegments(segments);
        return segments;
    }
}
