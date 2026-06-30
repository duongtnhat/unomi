package com.unomi.rule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RuleDefinitionRepository extends JpaRepository<RuleDefinitionEntity, UUID> {

    @EntityGraph(attributePaths = "condition")
    List<RuleDefinitionEntity> findAllByOrderByPriorityAscKeyAsc();

    @EntityGraph(attributePaths = "condition")
    List<RuleDefinitionEntity> findByActiveOrderByPriorityAscKeyAsc(boolean active);

    @EntityGraph(attributePaths = "condition")
    Optional<RuleDefinitionEntity> findByKey(String key);

    @EntityGraph(attributePaths = "condition")
    Optional<RuleDefinitionEntity> findById(UUID id);
}
