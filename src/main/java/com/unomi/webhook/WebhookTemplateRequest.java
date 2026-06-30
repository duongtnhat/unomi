package com.unomi.webhook;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Webhook template request.")
public record WebhookTemplateRequest(
    @Schema(description = "Unique camelCase template key.", example = "highValuePurchase")
    @NotBlank String key,
    @Schema(description = "Human readable template name.", example = "High Value Purchase Webhook")
    @NotBlank String name,
    @Schema(description = "HTTP method.", example = "POST")
    @NotBlank String method,
    @Schema(description = "Webhook target URL.", example = "https://example.com/hooks/high-value-purchase")
    @NotBlank String url,
    @Schema(description = "Static HTTP headers.", example = "{\"Content-Type\":\"application/json\"}")
    Map<String, String> headers,
    @Schema(
        description = "Mustache body template rendered with action payload context.",
        example = "{\"profileId\":\"{{profileId}}\",\"reason\":\"{{payload.reason}}\"}"
    )
    @NotBlank String body,
    @Schema(description = "Whether this template can be used.", example = "true")
    Boolean active
) {
}
