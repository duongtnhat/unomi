package com.unomi.scoring;

import java.math.BigDecimal;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Profile score state returned from Elasticsearch.")
public record ProfileScoreResponse(
    @Schema(description = "Customer profile ID.", example = "profile-1")
    String profileId,
    @Schema(description = "Score values keyed by scoring key.")
    Map<String, Object> scores,
    @Schema(description = "Changed score key when a single score was updated.", example = "engagement")
    String scoreKey,
    @Schema(description = "Previous score value.")
    BigDecimal previousValue,
    @Schema(description = "New score value.")
    BigDecimal newValue,
    @Schema(description = "Whether the request changed the stored profile.")
    boolean changed
) {
    public static ProfileScoreResponse scores(String profileId, Map<String, Object> scores) {
        return new ProfileScoreResponse(profileId, scores, null, null, null, false);
    }
}
