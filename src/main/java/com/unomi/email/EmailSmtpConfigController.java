package com.unomi.email;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/email-smtp-configs")
@Validated
@Tag(name = "Email SMTP Configs", description = "Manage SMTP server and sender settings for email actions.")
@SecurityRequirement(name = "apiKeyAuth")
public class EmailSmtpConfigController {

    private final EmailSmtpConfigService service;

    public EmailSmtpConfigController(EmailSmtpConfigService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create an SMTP config")
    public ResponseEntity<EmailSmtpConfigResponse> create(@Valid @RequestBody EmailSmtpConfigRequest request) {
        EmailSmtpConfigResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/email-smtp-configs/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an SMTP config")
    public EmailSmtpConfigResponse update(
        @PathVariable UUID id,
        @Valid @RequestBody EmailSmtpConfigRequest request
    ) {
        return service.update(id, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an SMTP config")
    public EmailSmtpConfigResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping
    @Operation(summary = "List SMTP configs")
    public List<EmailSmtpConfigResponse> list(@RequestParam(required = false) Boolean active) {
        return service.list(active);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an SMTP config")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/calls")
    @Operation(summary = "List email calls for an SMTP config")
    public List<EmailCallResponse> calls(@PathVariable UUID id) {
        return service.calls(id);
    }
}
