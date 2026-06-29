package com.unomi.definition;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DefinitionRepository extends JpaRepository<DefinitionEntity, UUID> {

    List<DefinitionEntity> findByTypeAndActiveOrderByKeyAscVersionDesc(DefinitionType type, boolean active);

    List<DefinitionEntity> findByTypeOrderByKeyAscVersionDesc(DefinitionType type);

    Optional<DefinitionEntity> findByKeyAndTypeAndVersion(String key, DefinitionType type, int version);
}
