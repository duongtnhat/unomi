package com.unomi.customer.event;

import java.time.Instant;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payload used to ingest one customer event into Elasticsearch.")
public record CustomerEventRequest(
    @Schema(description = "Profile ID that owns this event.", example = "5b412499-dfbd-43f1-8db1-ceeb6185ecd2")
    @NotBlank String profileId,
    @Schema(description = "Event type name.", example = "pageView")
    @NotBlank String eventType,
    @Schema(description = "Origin of the event.", example = "web")
    @NotBlank String source,
    @Schema(description = "When the event happened. Defaults to current server time when omitted.")
    Instant occurredAt,
    @Schema(description = "Event-specific properties.", example = "{\"url\":\"/pricing\"}")
    @NotNull Map<String, Object> payload
) {
}
