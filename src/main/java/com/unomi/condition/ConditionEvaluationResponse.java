package com.unomi.condition;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Condition evaluation result.")
public record ConditionEvaluationResponse(
    @Schema(description = "True when the condition matches the supplied context.", example = "true")
    boolean matched
) {
}
