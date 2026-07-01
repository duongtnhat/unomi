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
@RequestMapping("/api/email-templates")
@Validated
@Tag(name = "Email Templates", description = "Manage email templates and inspect email call history.")
@SecurityRequirement(name = "apiKeyAuth")
public class EmailTemplateController {

    private final EmailTemplateService service;

    public EmailTemplateController(EmailTemplateService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
        summary = "Create an email template",
        description = "Creates an email template. Subject and body use Mustache syntax rendered from action payload and profile context. The recipient is resolved from payload.toAddress first, then from the customer profile email."
    )
    public ResponseEntity<EmailTemplateResponse> create(@Valid @RequestBody EmailTemplateRequest request) {
        EmailTemplateResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/email-templates/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an email template")
    public EmailTemplateResponse update(
        @PathVariable UUID id,
        @Valid @RequestBody EmailTemplateRequest request
    ) {
        return service.update(id, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an email template")
    public EmailTemplateResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping
    @Operation(summary = "List email templates")
    public List<EmailTemplateResponse> list(@RequestParam(required = false) Boolean active) {
        return service.list(active);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an email template")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/calls")
    @Operation(summary = "List email calls for a template")
    public List<EmailCallResponse> calls(@PathVariable UUID id) {
        return service.calls(id);
    }

    @GetMapping("/calls")
    @Operation(summary = "List recent email calls")
    public List<EmailCallResponse> calls() {
        return service.calls();
    }
}
