package com.unomi.rule;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Rule definition returned from PostgreSQL.")
public record RuleDefinitionResponse(
    @Schema(description = "Rule definition ID.")
    UUID id,
    @Schema(description = "Unique rule key.", example = "vipCustomerRule")
    String key,
    @Schema(description = "Human readable rule name.", example = "VIP customer rule")
    String name,
    @Schema(description = "Optional rule description.")
    String description,
    @Schema(description = "Condition definition ID used to decide whether the rule activates.")
    UUID conditionId,
    @Schema(description = "Condition key.", example = "vipCustomer")
    String conditionKey,
    @Schema(description = "Condition version.", example = "1")
    int conditionVersion,
    @Schema(description = "Condition tree payload.")
    Map<String, Object> conditionPayload,
    @Schema(description = "Rule priority. Lower number runs first.", example = "100")
    int priority,
    @Schema(description = "Whether this rule is enabled.", example = "true")
    boolean active,
    @Schema(description = "Rule outputs.")
    Map<String, Object> outputs,
    @Schema(description = "Creation timestamp in UTC.")
    Instant createdAt,
    @Schema(description = "Last update timestamp in UTC.")
    Instant updatedAt
) {
    static RuleDefinitionResponse from(RuleDefinitionEntity entity) {
        return new RuleDefinitionResponse(
            entity.getId(),
            entity.getKey(),
            entity.getName(),
            entity.getDescription(),
            entity.getCondition().getId(),
            entity.getCondition().getKey(),
            entity.getCondition().getVersion(),
            entity.getCondition().getPayload(),
            entity.getPriority(),
            entity.isActive(),
            entity.getOutputs(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
