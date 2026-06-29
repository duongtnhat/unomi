package com.unomi.customer.upsert;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Event included inside the batch customer upsert API.")
public record UpsertEventRequest(
    @Schema(description = "Event name.", example = "purchase")
    String eventName,
    @Schema(description = "Event timestamp in RFC3339 format.", example = "2026-06-30T00:00:00Z")
    String timestamp,
    @Schema(description = "Event parameters.", example = "{\"eventGroupId\":\"ORDER123\",\"productId\":\"SKU-1\",\"currency\":\"USD\",\"quantity\":1,\"unitSalePrice\":89.9}")
    Map<String, Object> eventParams
) {
}
