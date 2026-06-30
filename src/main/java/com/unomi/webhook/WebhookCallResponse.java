package com.unomi.webhook;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Webhook call history item.")
public record WebhookCallResponse(
    @Schema(description = "Webhook call ID.")
    UUID id,
    @Schema(description = "Webhook template ID.")
    UUID templateId,
    @Schema(description = "Webhook template key.")
    String templateKey,
    @Schema(description = "Rule action event ID.")
    UUID actionEventId,
    @Schema(description = "Action tracking UUID carried across action consumers.")
    UUID trackingId,
    @Schema(description = "Original pipeline message ID.")
    String messageId,
    @Schema(description = "Customer profile ID.")
    String profileId,
    @Schema(description = "Rule key.")
    String ruleKey,
    @Schema(description = "Action key.")
    String actionKey,
    @Schema(description = "Call status.", example = "SUCCESS")
    String status,
    @Schema(description = "HTTP method.")
    String method,
    @Schema(description = "Target URL.")
    String url,
    @Schema(description = "Request headers.")
    Map<String, Object> requestHeaders,
    @Schema(description = "Rendered request body.")
    String requestBody,
    @Schema(description = "HTTP response status.")
    Integer responseStatus,
    @Schema(description = "Response headers.")
    Map<String, Object> responseHeaders,
    @Schema(description = "Response body.")
    String responseBody,
    @Schema(description = "Error message when the call failed.")
    String errorMessage,
    @Schema(description = "Creation timestamp in UTC.")
    Instant createdAt,
    @Schema(description = "Completion timestamp in UTC.")
    Instant completedAt
) {
    static WebhookCallResponse from(WebhookCallEntity entity) {
        return new WebhookCallResponse(
            entity.getId(),
            entity.getTemplate().getId(),
            entity.getTemplate().getKey(),
            entity.getActionEventId(),
            entity.getTrackingId(),
            entity.getMessageId(),
            entity.getProfileId(),
            entity.getRuleKey(),
            entity.getActionKey(),
            entity.getStatus(),
            entity.getMethod(),
            entity.getUrl(),
            entity.getRequestHeaders(),
            entity.getRequestBody(),
            entity.getResponseStatus(),
            entity.getResponseHeaders(),
            entity.getResponseBody(),
            entity.getErrorMessage(),
            entity.getCreatedAt(),
            entity.getCompletedAt()
        );
    }
}
