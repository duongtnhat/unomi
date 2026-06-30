package com.unomi.customer.upsert.messaging;

import java.time.Instant;

import com.unomi.customer.upsert.UpsertUserRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Kafka command emitted after the profile merge worker determines the final profile.")
public record ProfileMergeCompletedCommand(
    @Schema(description = "Original command message ID.")
    String messageId,
    @Schema(description = "Timestamp when profile merge completed.")
    Instant completedAt,
    @Schema(description = "Final profile ID after merge.")
    String profileId,
    @Schema(description = "Original user payload used by the segment worker.")
    UpsertUserRequest user
) {
}
