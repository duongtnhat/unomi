package com.unomi.scoring;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Scoring definition returned from PostgreSQL.")
public record ScoringDefinitionResponse(
    @Schema(description = "Scoring definition ID.")
    UUID id,
    @Schema(description = "Unique camelCase scoring key.", example = "engagement")
    String key,
    @Schema(description = "Human readable scoring name.", example = "Engagement")
    String name,
    @Schema(description = "Scoring value type.", example = "NUMBER")
    ScoringType type,
    @Schema(description = "Default score value when profile does not have this score yet.", example = "0")
    BigDecimal startValue,
    @Schema(description = "Minimum allowed score value.", example = "0")
    BigDecimal minValue,
    @Schema(description = "Maximum allowed score value.", example = "100")
    BigDecimal maxValue,
    @Schema(description = "When true, score updates that would lower the value are ignored.", example = "true")
    boolean onlyIncrease,
    @Schema(description = "When true, score updates that would raise the value are ignored.", example = "false")
    boolean onlyDecrease,
    @Schema(description = "Whether this scoring definition is enabled.", example = "true")
    boolean active,
    @Schema(description = "Creation timestamp in UTC.")
    Instant createdAt,
    @Schema(description = "Last update timestamp in UTC.")
    Instant updatedAt
) {
    static ScoringDefinitionResponse from(ScoringDefinitionEntity entity) {
        return new ScoringDefinitionResponse(
            entity.getId(),
            entity.getKey(),
            entity.getName(),
            entity.getType(),
            entity.getStartValue(),
            entity.getMinValue(),
            entity.getMaxValue(),
            entity.isOnlyIncrease(),
            entity.isOnlyDecrease(),
            entity.isActive(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
