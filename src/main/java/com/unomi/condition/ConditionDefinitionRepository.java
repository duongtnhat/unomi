package com.unomi.condition;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConditionDefinitionRepository extends JpaRepository<ConditionDefinitionEntity, UUID> {

    List<ConditionDefinitionEntity> findByActiveOrderByKeyAscVersionDesc(boolean active);

    List<ConditionDefinitionEntity> findAllByOrderByKeyAscVersionDesc();

    Optional<ConditionDefinitionEntity> findByKeyAndVersion(String key, int version);
}
