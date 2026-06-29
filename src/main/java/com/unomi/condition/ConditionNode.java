package com.unomi.condition;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Condition tree node inspired by Apache Unomi condition objects.")
public record ConditionNode(
    @Schema(description = "Condition type. Supported values: boolean, profileProperty, eventProperty, eventType, profileId, exists.", example = "profileProperty")
    String type,
    @Schema(description = "Parameters used by the condition evaluator.", example = "{\"propertyName\":\"age\",\"operator\":\"gte\",\"value\":18}")
    Map<String, Object> parameters,
    @Schema(description = "Child conditions for boolean composition.")
    List<ConditionNode> conditions
) {
}
