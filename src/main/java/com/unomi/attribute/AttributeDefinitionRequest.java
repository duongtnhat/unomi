package com.unomi.attribute;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Attribute definition request. Only defined attributes are accepted during customer upsert.")
public record AttributeDefinitionRequest(
    @Schema(description = "Unique camelCase attribute key.", example = "lifetimeValue")
    @NotBlank String key,
    @Schema(description = "Human readable attribute name.", example = "Lifetime Value")
    @NotBlank String name,
    @Schema(description = "Value type accepted for this attribute.", example = "NUMBER")
    @NotNull AttributeValueType type,
    @Schema(description = "Customer profile merge priority. Null means this attribute is not used as a merge identifier.", example = "10")
    Integer mergePriority,
    @Schema(description = "Customer profile merge strategy. Used only for customer attributes.", example = "SOURCE_PRIORITY")
    CustomerAttributeMergeStrategy mergeStrategy
) {
}
