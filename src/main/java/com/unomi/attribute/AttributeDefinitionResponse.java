package com.unomi.attribute;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Attribute definition stored in PostgreSQL.")
public record AttributeDefinitionResponse(
    @Schema(description = "Attribute definition ID.")
    UUID id,
    @Schema(description = "Unique camelCase attribute key.", example = "lifetimeValue")
    String key,
    @Schema(description = "Human readable attribute name.", example = "Lifetime Value")
    String name,
    @Schema(description = "Value type accepted for this attribute.", example = "NUMBER")
    AttributeValueType type,
    @Schema(description = "Customer profile merge priority. Null means this attribute is not used as a merge identifier.", example = "10")
    Integer mergePriority,
    @Schema(description = "Customer profile merge strategy. Null for event attributes.", example = "SOURCE_PRIORITY")
    CustomerAttributeMergeStrategy mergeStrategy,
    @Schema(description = "Whether this customer attribute contains personally identifiable information. Null for event attributes.", example = "true")
    Boolean pii,
    @Schema(description = "Creation timestamp in UTC.")
    Instant createdAt,
    @Schema(description = "Last update timestamp in UTC.")
    Instant updatedAt
) {
    static AttributeDefinitionResponse from(CustomerAttributeDefinitionEntity entity) {
        return new AttributeDefinitionResponse(
            entity.getId(),
            entity.getKey(),
            entity.getName(),
            entity.getType(),
            entity.getMergePriority(),
            entity.getMergeStrategy(),
            entity.isPii(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static AttributeDefinitionResponse from(EventAttributeDefinitionEntity entity) {
        return new AttributeDefinitionResponse(
            entity.getId(),
            entity.getKey(),
            entity.getName(),
            entity.getType(),
            null,
            null,
            null,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
