package com.unomi.pipeline;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InboxEventRepository extends JpaRepository<InboxEventEntity, UUID> {

    boolean existsByMessageId(String messageId);
}
