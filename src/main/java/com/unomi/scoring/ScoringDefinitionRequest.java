package com.unomi.scoring;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Scoring definition request.")
public record ScoringDefinitionRequest(
    @Schema(description = "Unique camelCase scoring key.", example = "engagement")
    @NotBlank String key,
    @Schema(description = "Human readable scoring name.", example = "Engagement")
    @NotBlank String name,
    @Schema(description = "Scoring value type.", example = "NUMBER")
    @NotNull ScoringType type,
    @Schema(description = "Default score value when profile does not have this score yet.", example = "0")
    @NotNull BigDecimal startValue,
    @Schema(description = "Minimum allowed score value.", example = "0")
    BigDecimal minValue,
    @Schema(description = "Maximum allowed score value.", example = "100")
    BigDecimal maxValue,
    @Schema(description = "When true, score updates that would lower the value are ignored.", example = "true")
    Boolean onlyIncrease,
    @Schema(description = "When true, score updates that would raise the value are ignored.", example = "false")
    Boolean onlyDecrease,
    @Schema(description = "Whether this scoring definition is enabled.", example = "true")
    Boolean active
) {
}
