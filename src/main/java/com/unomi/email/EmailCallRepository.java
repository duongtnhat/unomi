package com.unomi.email;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailCallRepository extends JpaRepository<EmailCallEntity, UUID> {

    List<EmailCallEntity> findTop100ByOrderByCreatedAtDesc();

    List<EmailCallEntity> findTop100ByTemplate_IdOrderByCreatedAtDesc(UUID templateId);

    List<EmailCallEntity> findTop100BySmtpConfig_IdOrderByCreatedAtDesc(UUID smtpConfigId);
}
