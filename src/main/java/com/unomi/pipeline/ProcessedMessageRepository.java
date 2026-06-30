package com.unomi.pipeline;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessageEntity, UUID> {

    boolean existsByMessageIdAndStage(String messageId, String stage);

    Optional<ProcessedMessageEntity> findByMessageIdAndStage(String messageId, String stage);
}
