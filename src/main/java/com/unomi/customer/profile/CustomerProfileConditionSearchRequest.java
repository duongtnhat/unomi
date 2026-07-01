package com.unomi.customer.profile;

import com.unomi.condition.ConditionNode;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request used to search customer profiles by condition.")
public record CustomerProfileConditionSearchRequest(
    @Valid
    @NotNull
    @Schema(description = "Profile condition tree to evaluate.")
    ConditionNode condition
) {
}
