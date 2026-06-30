package com.unomi.action;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Action type definition request.")
public record ActionTypeDefinitionRequest(
    @Schema(description = "Unique camelCase action type key.", example = "webhook")
    @NotBlank String key,
    @Schema(description = "Human readable action type name.", example = "Webhook")
    @NotBlank String name,
    @Schema(description = "Optional action type description.", example = "Calls an external HTTP endpoint.")
    String description,
    @Schema(description = "Whether this action type can be used by rules.", example = "true")
    Boolean active,
    @Schema(description = "Parameters supported by this action type.")
    @NotNull List<@Valid ActionParameterDefinition> params
) {
}
