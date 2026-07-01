package com.unomi.email;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Email template request.")
public record EmailTemplateRequest(
    @Schema(description = "Unique camelCase email template key.", example = "welcomeEmail")
    @NotBlank String key,
    @Schema(description = "Human readable template name.", example = "Welcome Email")
    @NotBlank String name,
    @Schema(description = "SMTP config ID used to send this template.")
    @NotNull UUID smtpConfigId,
    @Schema(description = "Mustache subject template.", example = "Welcome {{payload.firstName}}")
    @NotBlank String subject,
    @Schema(description = "Mustache body template.")
    @NotBlank String body,
    @Schema(description = "Email content type.", example = "text/html")
    String contentType,
    @Schema(description = "Whether this template can be used.", example = "true")
    Boolean active
) {
}
