package com.unomi.customer.upsert.messaging;

import java.time.Instant;

import com.unomi.customer.upsert.UpsertUserRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Kafka command emitted after segment qualification to evaluate profile rules.")
public record RuleEvaluationCommand(
    @Schema(description = "Original command message ID.")
    String messageId,
    @Schema(description = "Timestamp when segment qualification completed.")
    Instant completedAt,
    @Schema(description = "Final profile ID after segment qualification.")
    String profileId,
    @Schema(description = "Original user payload used by the rule worker.")
    UpsertUserRequest user
) {
}
