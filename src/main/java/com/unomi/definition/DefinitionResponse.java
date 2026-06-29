package com.unomi.definition;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Definition returned from PostgreSQL.")
public record DefinitionResponse(
    @Schema(description = "Definition ID.")
    UUID id,
    @Schema(description = "Unique definition key within its type and version.")
    String key,
    @Schema(description = "Definition category.")
    DefinitionType type,
    @Schema(description = "Definition version.")
    int version,
    @Schema(description = "Human readable definition name.")
    String name,
    @Schema(description = "Whether this definition is enabled.")
    boolean active,
    @Schema(description = "Flexible definition body stored as JSONB.")
    Map<String, Object> payload,
    @Schema(description = "Creation timestamp in UTC.")
    Instant createdAt,
    @Schema(description = "Last update timestamp in UTC.")
    Instant updatedAt
) {
    static DefinitionResponse from(DefinitionEntity entity) {
        return new DefinitionResponse(
            entity.getId(),
            entity.getKey(),
            entity.getType(),
            entity.getVersion(),
            entity.getName(),
            entity.isActive(),
            entity.getPayload(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
