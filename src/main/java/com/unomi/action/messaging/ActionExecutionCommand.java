package com.unomi.action.messaging;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Kafka command for asynchronously resolving a rule action event.")
public record ActionExecutionCommand(
    @Schema(description = "Rule action event ID used as the idempotency key.")
    UUID actionEventId,
    @Schema(description = "Original pipeline message ID.")
    String messageId,
    @Schema(description = "Command creation timestamp.")
    Instant requestedAt,
    @Schema(description = "Customer profile ID.")
    String profileId,
    @Schema(description = "Rule key that produced this action.")
    String ruleKey,
    @Schema(description = "Action key.")
    String actionKey,
    @Schema(description = "Action type key.")
    String actionType,
    @Schema(description = "Action payload.")
    Map<String, Object> payload
) {
}
