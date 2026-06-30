package com.unomi.email;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "SMTP configuration returned from PostgreSQL.")
public record EmailSmtpConfigResponse(
    UUID id,
    String key,
    String name,
    String host,
    int port,
    String username,
    String fromAddress,
    String fromName,
    boolean authEnabled,
    boolean startTlsEnabled,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {
    static EmailSmtpConfigResponse from(EmailSmtpConfigEntity entity) {
        return new EmailSmtpConfigResponse(
            entity.getId(),
            entity.getKey(),
            entity.getName(),
            entity.getHost(),
            entity.getPort(),
            entity.getUsername(),
            entity.getFromAddress(),
            entity.getFromName(),
            entity.isAuthEnabled(),
            entity.isStartTlsEnabled(),
            entity.isActive(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
