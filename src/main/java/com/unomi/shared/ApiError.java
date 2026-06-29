package com.unomi.shared;

import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard API error response.")
public record ApiError(
    @Schema(description = "Error timestamp in UTC.")
    Instant timestamp,
    @Schema(description = "HTTP status code.", example = "400")
    int status,
    @Schema(description = "HTTP reason phrase.", example = "Bad Request")
    String error,
    @Schema(description = "Human readable error messages.")
    List<String> messages
) {
}
