package com.unomi.webhook;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookCallRepository extends JpaRepository<WebhookCallEntity, UUID> {

    List<WebhookCallEntity> findTop100ByOrderByCreatedAtDesc();

    List<WebhookCallEntity> findTop100ByTemplate_IdOrderByCreatedAtDesc(UUID templateId);
}
