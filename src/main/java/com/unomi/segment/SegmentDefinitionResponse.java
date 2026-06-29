package com.unomi.segment;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Segment definition returned from PostgreSQL.")
public record SegmentDefinitionResponse(
    @Schema(description = "Segment definition ID.")
    UUID id,
    @Schema(description = "Unique segment key.", example = "adultBuyers")
    String key,
    @Schema(description = "Human readable segment name.", example = "Adult buyers")
    String name,
    @Schema(description = "Optional segment description.")
    String description,
    @Schema(description = "Condition definition ID used to decide membership.")
    UUID conditionId,
    @Schema(description = "Condition key.", example = "adult-purchase-condition")
    String conditionKey,
    @Schema(description = "Condition version.", example = "1")
    int conditionVersion,
    @Schema(description = "Condition tree payload.")
    Map<String, Object> conditionPayload,
    @Schema(description = "Whether this segment is enabled.", example = "true")
    boolean active,
    @Schema(description = "Creation timestamp in UTC.")
    Instant createdAt,
    @Schema(description = "Last update timestamp in UTC.")
    Instant updatedAt
) {
    static SegmentDefinitionResponse from(SegmentDefinitionEntity entity) {
        return new SegmentDefinitionResponse(
            entity.getId(),
            entity.getKey(),
            entity.getName(),
            entity.getDescription(),
            entity.getCondition().getId(),
            entity.getCondition().getKey(),
            entity.getCondition().getVersion(),
            entity.getCondition().getPayload(),
            entity.isActive(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
