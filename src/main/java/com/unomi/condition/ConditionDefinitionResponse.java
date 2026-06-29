package com.unomi.condition;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Condition definition returned from PostgreSQL.")
public record ConditionDefinitionResponse(
    @Schema(description = "Condition definition ID.")
    UUID id,
    @Schema(description = "Unique condition key within its version.", example = "adult-purchase-condition")
    String key,
    @Schema(description = "Condition version.", example = "1")
    int version,
    @Schema(description = "Human readable condition name.", example = "Adult purchase condition")
    String name,
    @Schema(description = "Whether this condition definition is enabled.", example = "true")
    boolean active,
    @Schema(description = "Condition tree payload.")
    Map<String, Object> payload,
    @Schema(description = "Creation timestamp in UTC.")
    Instant createdAt,
    @Schema(description = "Last update timestamp in UTC.")
    Instant updatedAt
) {
    static ConditionDefinitionResponse from(ConditionDefinitionEntity entity) {
        return new ConditionDefinitionResponse(
            entity.getId(),
            entity.getKey(),
            entity.getVersion(),
            entity.getName(),
            entity.isActive(),
            entity.getPayload(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
