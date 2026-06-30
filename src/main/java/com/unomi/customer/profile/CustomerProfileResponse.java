package com.unomi.customer.profile;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Customer profile document returned from Elasticsearch.")
public record CustomerProfileResponse(
    @Schema(description = "Internal profile document ID.", example = "5b412499-dfbd-43f1-8db1-ceeb6185ecd2")
    String id,
    @Schema(description = "Stable business key used for lookup and upsert.", example = "email:ada@example.com")
    String profileKey,
    @Schema(description = "Anonymous visitor identifier.", example = "anon-123")
    String anonymousId,
    @Schema(description = "Customer email address.", example = "ada@example.com")
    String email,
    @Schema(description = "Profile attributes.")
    Map<String, Object> properties,
    @Schema(description = "Segment definition IDs this profile belongs to.")
    List<String> segmentIds,
    @Schema(description = "Segment keys this profile belongs to.")
    List<String> segmentKeys,
    @Schema(description = "Rule-driven tags attached to this profile.")
    List<String> tags,
    @Schema(description = "Rule-driven scores attached to this profile.")
    Map<String, Object> scores,
    @Schema(description = "Creation timestamp in UTC.")
    Instant createdAt,
    @Schema(description = "Last update timestamp in UTC.")
    Instant updatedAt
) {
    static CustomerProfileResponse from(CustomerProfileDocument profile) {
        return new CustomerProfileResponse(
            profile.getId(),
            profile.getProfileKey(),
            profile.getAnonymousId(),
            profile.getEmail(),
            profile.getProperties(),
            profile.getSegmentIds(),
            profile.getSegmentKeys(),
            profile.getTags(),
            profile.getScores(),
            profile.getCreatedAt(),
            profile.getUpdatedAt()
        );
    }
}
