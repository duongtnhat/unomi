package com.unomi.condition;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request used to evaluate a condition tree against profile and event data.")
public record ConditionEvaluationRequest(
    @Schema(description = "Condition tree to evaluate.")
    ConditionNode condition,
    @Schema(description = "Profile context. Use the same shape as a customer profile document.")
    Map<String, Object> profile,
    @Schema(description = "Event context. Use the same shape as a customer event document.")
    Map<String, Object> event
) {
}
