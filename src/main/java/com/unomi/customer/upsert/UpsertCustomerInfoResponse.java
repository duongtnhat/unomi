package com.unomi.customer.upsert;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Batch upsert result with per-user success and failure details.")
public record UpsertCustomerInfoResponse(
    @Schema(description = "Result data.")
    Data data
) {
    @Schema(description = "Upsert result summary.")
    public record Data(
        @Schema(description = "Successful user writes.")
        Successful successful,
        @Schema(description = "Failed user writes and validation errors.")
        Fail fail
    ) {
    }

    @Schema(description = "Successful user write summary.")
    public record Successful(
        @Schema(description = "Number of users successfully processed.", example = "1")
        int count,
        @Schema(description = "Profile IDs written by this request.")
        List<String> profileIds
    ) {
    }

    @Schema(description = "Failed user write summary.")
    public record Fail(
        @Schema(description = "Number of users that failed validation or processing.", example = "0")
        int count,
        @Schema(description = "Error messages keyed by request path.")
        Map<String, List<String>> errors
    ) {
    }
}
