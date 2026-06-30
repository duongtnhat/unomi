package com.unomi.email;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Email call history item.")
public record EmailCallResponse(
    UUID id,
    UUID templateId,
    String templateKey,
    UUID smtpConfigId,
    String smtpConfigKey,
    UUID actionEventId,
    UUID trackingId,
    String messageId,
    String profileId,
    String ruleKey,
    String actionKey,
    String status,
    String fromAddress,
    String toAddress,
    String subject,
    String body,
    String errorMessage,
    Instant createdAt,
    Instant completedAt
) {
    static EmailCallResponse from(EmailCallEntity entity) {
        return new EmailCallResponse(
            entity.getId(),
            entity.getTemplate().getId(),
            entity.getTemplate().getKey(),
            entity.getSmtpConfig().getId(),
            entity.getSmtpConfig().getKey(),
            entity.getActionEventId(),
            entity.getTrackingId(),
            entity.getMessageId(),
            entity.getProfileId(),
            entity.getRuleKey(),
            entity.getActionKey(),
            entity.getStatus(),
            entity.getFromAddress(),
            entity.getToAddress(),
            entity.getSubject(),
            entity.getBody(),
            entity.getErrorMessage(),
            entity.getCreatedAt(),
            entity.getCompletedAt()
        );
    }
}
