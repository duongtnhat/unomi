package com.unomi.condition;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Condition definition stored in PostgreSQL as JSONB.")
public record ConditionDefinitionRequest(
    @Schema(description = "Unique condition key within its version.", example = "adult-purchase-condition")
    @NotBlank String key,
    @Schema(description = "Condition version. Starts at 1.", example = "1")
    @Min(1) int version,
    @Schema(description = "Human readable condition name.", example = "Adult purchase condition")
    @NotBlank String name,
    @Schema(description = "Whether this condition definition is enabled.", example = "true")
    boolean active,
    @Schema(description = "Condition tree payload.", example = "{\"type\":\"profileProperty\",\"parameters\":{\"propertyName\":\"properties.age\",\"operator\":\"gte\",\"value\":18}}")
    @NotNull Map<String, Object> payload
) {
}
