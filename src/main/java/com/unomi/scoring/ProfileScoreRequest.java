package com.unomi.scoring;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to set, increase, or decrease one score on a profile.")
public record ProfileScoreRequest(
    @Schema(description = "Score operation.", example = "INCREASE")
    @NotNull ScoreOperation operation,
    @Schema(description = "Score value used by the operation.", example = "10")
    @NotNull BigDecimal value
) {
}
