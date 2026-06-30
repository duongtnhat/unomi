package com.unomi.webhook;

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

import com.unomi.shared.ApiError;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/webhook-templates")
@Validated
@Tag(name = "Webhook Templates", description = "Manage webhook templates and inspect webhook call history.")
@SecurityRequirement(name = "apiKeyAuth")
public class WebhookTemplateController {

    private final WebhookTemplateService service;

    public WebhookTemplateController(WebhookTemplateService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
        summary = "Create a webhook template",
        description = "Creates a webhook template. The body uses Mustache syntax and is rendered from action payload context."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Webhook template created"),
        @ApiResponse(responseCode = "400", description = "Invalid webhook template payload",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<WebhookTemplateResponse> create(@Valid @RequestBody WebhookTemplateRequest request) {
        WebhookTemplateResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/webhook-templates/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a webhook template")
    public WebhookTemplateResponse update(
        @PathVariable UUID id,
        @Valid @RequestBody WebhookTemplateRequest request
    ) {
        return service.update(id, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a webhook template")
    public WebhookTemplateResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping
    @Operation(summary = "List webhook templates")
    public List<WebhookTemplateResponse> list(@RequestParam(required = false) Boolean active) {
        return service.list(active);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a webhook template")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/calls")
    @Operation(summary = "List webhook calls for a template")
    public List<WebhookCallResponse> calls(@PathVariable UUID id) {
        return service.calls(id);
    }

    @GetMapping("/calls")
    @Operation(summary = "List recent webhook calls")
    public List<WebhookCallResponse> calls() {
        return service.calls();
    }
}
