package com.unomi.attribute;

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
import org.springframework.web.bind.annotation.RestController;

import com.unomi.shared.ApiError;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/event-attributes")
@Validated
@Tag(name = "Event Attributes", description = "CRUD API for event attribute definitions stored in PostgreSQL.")
@SecurityRequirement(name = "apiKeyAuth")
public class EventAttributeDefinitionController {

    private final AttributeDefinitionService service;

    public EventAttributeDefinitionController(AttributeDefinitionService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create event attribute definition")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Event attribute definition created"),
        @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<AttributeDefinitionResponse> create(@Valid @RequestBody AttributeDefinitionRequest request) {
        AttributeDefinitionResponse response = service.createEvent(request);
        return ResponseEntity.created(URI.create("/api/event-attributes/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update event attribute definition")
    public AttributeDefinitionResponse update(
        @Parameter(description = "Event attribute definition ID.")
        @PathVariable UUID id,
        @Valid @RequestBody AttributeDefinitionRequest request
    ) {
        return service.updateEvent(id, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get event attribute definition")
    public AttributeDefinitionResponse get(
        @Parameter(description = "Event attribute definition ID.")
        @PathVariable UUID id
    ) {
        return service.getEvent(id);
    }

    @GetMapping
    @Operation(summary = "List event attribute definitions")
    public List<AttributeDefinitionResponse> list() {
        return service.listEvents();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete event attribute definition")
    public ResponseEntity<Void> delete(
        @Parameter(description = "Event attribute definition ID.")
        @PathVariable UUID id
    ) {
        service.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
