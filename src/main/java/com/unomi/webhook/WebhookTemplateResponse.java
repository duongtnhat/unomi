package com.unomi.webhook;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Webhook template stored in PostgreSQL.")
public record WebhookTemplateResponse(
    @Schema(description = "Webhook template ID.")
    UUID id,
    @Schema(description = "Unique camelCase template key.", example = "highValuePurchase")
    String key,
    @Schema(description = "Human readable template name.", example = "High Value Purchase Webhook")
    String name,
    @Schema(description = "HTTP method.", example = "POST")
    String method,
    @Schema(description = "Webhook target URL.")
    String url,
    @Schema(description = "Static HTTP headers.")
    Map<String, String> headers,
    @Schema(description = "Mustache body template.")
    String body,
    @Schema(description = "Whether this template can be used.", example = "true")
    boolean active,
    @Schema(description = "Creation timestamp in UTC.")
    Instant createdAt,
    @Schema(description = "Last update timestamp in UTC.")
    Instant updatedAt
) {
    static WebhookTemplateResponse from(WebhookTemplateEntity entity) {
        return new WebhookTemplateResponse(
            entity.getId(),
            entity.getKey(),
            entity.getName(),
            entity.getMethod(),
            entity.getUrl(),
            entity.getHeaders(),
            entity.getBody(),
            entity.isActive(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
