package com.unomi.customer.profile;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Paged customer profile search response.")
public record CustomerProfileSearchResponse(
    @Schema(description = "Matched profiles for this page.")
    List<CustomerProfileResponse> content,
    @Schema(description = "Zero-based page index.", example = "0")
    int page,
    @Schema(description = "Requested page size.", example = "20")
    int size,
    @Schema(description = "Total matched profiles.", example = "125")
    long totalElements,
    @Schema(description = "Total pages.", example = "7")
    int totalPages,
    @Schema(description = "Whether this is the first page.", example = "true")
    boolean first,
    @Schema(description = "Whether this is the last page.", example = "false")
    boolean last
) {
}
