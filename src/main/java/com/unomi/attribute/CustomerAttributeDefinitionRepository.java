package com.unomi.attribute;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerAttributeDefinitionRepository
    extends JpaRepository<CustomerAttributeDefinitionEntity, UUID> {

    Optional<CustomerAttributeDefinitionEntity> findByKey(String key);

    boolean existsByKey(String key);
}
