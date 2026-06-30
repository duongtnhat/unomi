package com.unomi.action;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionTypeDefinitionRepository extends JpaRepository<ActionTypeDefinitionEntity, UUID> {

    List<ActionTypeDefinitionEntity> findAllByOrderByKeyAsc();

    boolean existsByKey(String key);

    Optional<ActionTypeDefinitionEntity> findByKey(String key);
}
