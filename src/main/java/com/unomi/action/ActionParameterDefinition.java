package com.unomi.action;

import com.unomi.attribute.AttributeValueType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Parameter definition required by an action type.")
public record ActionParameterDefinition(
    @Schema(description = "Unique camelCase parameter key.", example = "webhookUrl")
    @NotBlank String key,
    @Schema(description = "Human readable parameter name.", example = "Webhook URL")
    @NotBlank String name,
    @Schema(description = "Parameter value type.", example = "TEXT")
    @NotNull AttributeValueType type,
    @Schema(description = "Whether this parameter is required when the action is triggered.", example = "true")
    Boolean required,
    @Schema(description = "Optional default value used by action executors.")
    Object defaultValue,
    @Schema(description = "Optional parameter description.", example = "Destination endpoint used by the action executor.")
    String description
) {
    public boolean requiredValue() {
        return Boolean.TRUE.equals(required);
    }
}
