package com.unomi.attribute;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventAttributeDefinitionRepository extends JpaRepository<EventAttributeDefinitionEntity, UUID> {

    Optional<EventAttributeDefinitionEntity> findByKey(String key);

    boolean existsByKey(String key);
}
