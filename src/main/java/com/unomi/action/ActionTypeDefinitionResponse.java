package com.unomi.action;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Action type definition stored in PostgreSQL.")
public record ActionTypeDefinitionResponse(
    @Schema(description = "Action type definition ID.")
    UUID id,
    @Schema(description = "Unique camelCase action type key.", example = "webhook")
    String key,
    @Schema(description = "Human readable action type name.", example = "Webhook")
    String name,
    @Schema(description = "Optional action type description.")
    String description,
    @Schema(description = "Whether this action type can be used by rules.", example = "true")
    boolean active,
    @Schema(description = "Parameters supported by this action type.")
    List<ActionParameterDefinition> params,
    @Schema(description = "Creation timestamp in UTC.")
    Instant createdAt,
    @Schema(description = "Last update timestamp in UTC.")
    Instant updatedAt
) {
    static ActionTypeDefinitionResponse from(ActionTypeDefinitionEntity entity) {
        return new ActionTypeDefinitionResponse(
            entity.getId(),
            entity.getKey(),
            entity.getName(),
            entity.getDescription(),
            entity.isActive(),
            entity.getParams(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
