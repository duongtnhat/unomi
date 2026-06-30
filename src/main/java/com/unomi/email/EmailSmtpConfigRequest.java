package com.unomi.email;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "SMTP configuration used by email templates.")
public record EmailSmtpConfigRequest(
    @Schema(description = "Unique camelCase SMTP config key.", example = "defaultSmtp")
    @NotBlank String key,
    @Schema(description = "Human readable SMTP config name.", example = "Default SMTP")
    @NotBlank String name,
    @Schema(description = "SMTP host.", example = "smtp.example.com")
    @NotBlank String host,
    @Schema(description = "SMTP port.", example = "587")
    @Min(1) int port,
    @Schema(description = "SMTP username.", example = "apikey")
    String username,
    @Schema(description = "SMTP password or token.")
    String password,
    @Schema(description = "Sender email address.", example = "noreply@example.com")
    @Email @NotBlank String fromAddress,
    @Schema(description = "Sender display name.", example = "Unomi")
    String fromName,
    @Schema(description = "Whether SMTP auth is enabled.", example = "true")
    Boolean authEnabled,
    @Schema(description = "Whether STARTTLS is enabled.", example = "true")
    Boolean startTlsEnabled,
    @Schema(description = "Whether this config can be used.", example = "true")
    Boolean active
) {
}
