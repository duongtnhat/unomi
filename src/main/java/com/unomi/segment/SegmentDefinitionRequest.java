package com.unomi.segment;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Segment definition stored in PostgreSQL and evaluated from a condition definition.")
public record SegmentDefinitionRequest(
    @Schema(description = "Unique segment key.", example = "adultBuyers")
    @NotBlank String key,
    @Schema(description = "Human readable segment name.", example = "Adult buyers")
    @NotBlank String name,
    @Schema(description = "Optional segment description.", example = "Customers aged at least 18 who made a purchase")
    String description,
    @Schema(description = "Condition definition ID used to decide membership.")
    @NotNull UUID conditionId,
    @Schema(description = "Whether this segment is enabled.", example = "true")
    boolean active
) {
}
