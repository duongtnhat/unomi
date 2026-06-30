package com.unomi.rule;

import java.util.Map;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Rule definition evaluated asynchronously after segment qualification.")
public record RuleDefinitionRequest(
    @Schema(description = "Unique rule key.", example = "vipCustomerRule")
    @NotBlank String key,
    @Schema(description = "Human readable rule name.", example = "VIP customer rule")
    @NotBlank String name,
    @Schema(description = "Optional rule description.", example = "Adds VIP tag and score when customer matches VIP condition.")
    String description,
    @Schema(description = "Condition definition ID used to decide whether the rule activates.")
    @NotNull UUID conditionId,
    @Schema(description = "Rule priority. Lower number runs first.", example = "100")
    int priority,
    @Schema(description = "Whether this rule is enabled.", example = "true")
    boolean active,
    @Schema(
        description = "Rule outputs. Supported keys: attributes, tags, scores, actions.",
        example = "{\"attributes\":{\"loyaltyTier\":\"gold\"},\"tags\":[\"vip\"],\"scores\":{\"engagement\":{\"operation\":\"INCREASE\",\"value\":10}},\"actions\":[{\"key\":\"notifyCrm\",\"type\":\"WEBHOOK\",\"payload\":{\"channel\":\"crm\"}}]}"
    )
    @NotNull Map<String, Object> outputs
) {
}
