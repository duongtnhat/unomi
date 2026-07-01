package com.unomi.email;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Email template returned from PostgreSQL.")
public record EmailTemplateResponse(
    UUID id,
    String key,
    String name,
    UUID smtpConfigId,
    String smtpConfigKey,
    String subject,
    String body,
    String contentType,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {
    static EmailTemplateResponse from(EmailTemplateEntity entity) {
        return new EmailTemplateResponse(
            entity.getId(),
            entity.getKey(),
            entity.getName(),
            entity.getSmtpConfig().getId(),
            entity.getSmtpConfig().getKey(),
            entity.getSubject(),
            entity.getBody(),
            entity.getContentType(),
            entity.isActive(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
