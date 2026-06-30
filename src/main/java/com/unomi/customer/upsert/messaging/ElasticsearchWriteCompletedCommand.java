package com.unomi.customer.upsert.messaging;

import java.time.Instant;

import com.unomi.customer.upsert.UpsertUserRequest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Kafka command emitted after the Elasticsearch write worker saves profile and event data.")
public record ElasticsearchWriteCompletedCommand(
    @Schema(description = "Original command message ID.")
    String messageId,
    @Schema(description = "Timestamp when Elasticsearch write completed.")
    Instant completedAt,
    @Schema(description = "Profile ID written by the Elasticsearch write worker.")
    String profileId,
    @Schema(description = "Original user payload used by downstream workers.")
    UpsertUserRequest user
) {
}
