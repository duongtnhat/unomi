package com.unomi.customer.event;

import java.time.Instant;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Customer event document returned from Elasticsearch.")
public record CustomerEventResponse(
    @Schema(description = "Internal event document ID.")
    String id,
    @Schema(description = "Profile ID that owns this event.")
    String profileId,
    @Schema(description = "Event type name.", example = "pageView")
    String eventType,
    @Schema(description = "Origin of the event.", example = "web")
    String source,
    @Schema(description = "Event-specific properties.")
    Map<String, Object> payload,
    @Schema(description = "When the event happened.")
    Instant occurredAt,
    @Schema(description = "When the platform received the event.")
    Instant receivedAt
) {
    static CustomerEventResponse from(CustomerEventDocument event) {
        return new CustomerEventResponse(
            event.getId(),
            event.getProfileId(),
            event.getEventType(),
            event.getSource(),
            event.getPayload(),
            event.getOccurredAt(),
            event.getReceivedAt()
        );
    }
}
