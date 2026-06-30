package com.unomi.scoring;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoringDefinitionRepository extends JpaRepository<ScoringDefinitionEntity, UUID> {

    List<ScoringDefinitionEntity> findAllByOrderByKeyAsc();

    boolean existsByKey(String key);

    Optional<ScoringDefinitionEntity> findByKey(String key);
}
