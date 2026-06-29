package com.unomi.segment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SegmentDefinitionRepository extends JpaRepository<SegmentDefinitionEntity, UUID> {

    @EntityGraph(attributePaths = "condition")
    List<SegmentDefinitionEntity> findAllByOrderByKeyAsc();

    @EntityGraph(attributePaths = "condition")
    List<SegmentDefinitionEntity> findByActiveOrderByKeyAsc(boolean active);

    @EntityGraph(attributePaths = "condition")
    Optional<SegmentDefinitionEntity> findByKey(String key);

    @EntityGraph(attributePaths = "condition")
    Optional<SegmentDefinitionEntity> findById(UUID id);
}
