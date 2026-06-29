package com.unomi.definition;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
@RequestMapping("/api/definitions")
@Validated
@Tag(name = "Definitions", description = "Manage platform definitions stored in PostgreSQL JSONB.")
@SecurityRequirement(name = "apiKeyAuth")
public class DefinitionController {

    private final DefinitionService service;

    public DefinitionController(DefinitionService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
        summary = "Create or update a definition",
        description = "Upserts a definition by key, type, and version. The payload is stored as PostgreSQL JSONB."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Definition created or updated"),
        @ApiResponse(responseCode = "400", description = "Invalid request body",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<DefinitionResponse> upsert(@Valid @RequestBody DefinitionRequest request) {
        DefinitionResponse response = service.upsert(request);
        return ResponseEntity
            .created(URI.create("/api/definitions/" + response.id()))
            .body(response);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get a definition",
        description = "Returns one definition by ID."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Definition found"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Definition not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public DefinitionResponse get(
        @Parameter(description = "Definition ID.", example = "73fb11ba-e318-4d4a-98da-872099a6e0a1")
        @PathVariable UUID id
    ) {
        return service.get(id);
    }

    @GetMapping
    @Operation(
        summary = "List definitions",
        description = "Lists definitions by type, optionally filtered by active status."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Definitions returned"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<DefinitionResponse> list(
        @Parameter(description = "Definition type.", example = "SEGMENT")
        @RequestParam DefinitionType type,
        @Parameter(description = "Optional active flag filter.", example = "true")
        @RequestParam(required = false) Boolean active
    ) {
        return service.list(type, active);
    }
}
