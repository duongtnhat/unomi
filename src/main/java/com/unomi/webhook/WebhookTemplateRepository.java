package com.unomi.webhook;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookTemplateRepository extends JpaRepository<WebhookTemplateEntity, UUID> {

    List<WebhookTemplateEntity> findAllByOrderByKeyAsc();

    boolean existsByKey(String key);

    Optional<WebhookTemplateEntity> findByKey(String key);

    Optional<WebhookTemplateEntity> findByKeyAndActiveTrue(String key);
}
