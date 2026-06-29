package com.unomi.definition;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Definition payload stored in PostgreSQL as JSONB.")
public record DefinitionRequest(
    @Schema(description = "Unique definition key within its type and version.", example = "vip-customers")
    @NotBlank String key,
    @Schema(description = "Definition category.", example = "SEGMENT")
    @NotNull DefinitionType type,
    @Schema(description = "Definition version. Starts at 1.", example = "1")
    @Min(1) int version,
    @Schema(description = "Human readable definition name.", example = "VIP Customers")
    @NotBlank String name,
    @Schema(description = "Whether this definition is enabled.", example = "true")
    boolean active,
    @Schema(description = "Flexible definition body stored as JSONB.", example = "{\"conditions\":[{\"property\":\"lifetimeValue\",\"operator\":\"gte\",\"value\":1000}]}")
    @NotNull Map<String, Object> payload
) {
}
