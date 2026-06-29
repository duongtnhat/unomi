package com.unomi.customer.profile;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Payload used to create or update a customer profile in Elasticsearch.")
public record CustomerProfileRequest(
    @Schema(description = "Stable business key used to identify the profile.", example = "email:ada@example.com")
    @NotBlank String profileKey,
    @Schema(description = "Anonymous visitor identifier, usually generated before login.", example = "anon-123")
    String anonymousId,
    @Schema(description = "Customer email address.", example = "ada@example.com")
    String email,
    @Schema(description = "Customer attributes. Unknown keys or values with the wrong type are ignored.", example = "{\"age\":30,\"language\":\"en_US\",\"favoriteColor\":[\"green\"]}")
    @NotNull Map<String, Object> properties
) {
}
