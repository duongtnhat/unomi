package com.unomi.action;

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
@RequestMapping("/api/action-types")
@Validated
@Tag(name = "Action Types", description = "Manage action type definitions and their supported parameters.")
@SecurityRequirement(name = "apiKeyAuth")
public class ActionTypeDefinitionController {

    private final ActionTypeDefinitionService service;

    public ActionTypeDefinitionController(ActionTypeDefinitionService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
        summary = "Create an action type",
        description = "Creates an action type definition with a list of supported parameters."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Action type created"),
        @ApiResponse(responseCode = "400", description = "Invalid action type payload",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ActionTypeDefinitionResponse> create(
        @Valid @RequestBody ActionTypeDefinitionRequest request
    ) {
        ActionTypeDefinitionResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/action-types/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update an action type",
        description = "Updates an action type definition and refreshes metadata cache."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Action type updated"),
        @ApiResponse(responseCode = "400", description = "Invalid action type payload",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Action type not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ActionTypeDefinitionResponse update(
        @PathVariable UUID id,
        @Valid @RequestBody ActionTypeDefinitionRequest request
    ) {
        return service.update(id, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an action type", description = "Returns an action type definition by ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Action type returned"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Action type not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ActionTypeDefinitionResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping
    @Operation(
        summary = "List action types",
        description = "Lists action type definitions. Use active=true or active=false to filter by enabled state."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Action types returned"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<ActionTypeDefinitionResponse> list(@RequestParam(required = false) Boolean active) {
        return service.list(active);
    }

    @GetMapping("/{id}/params")
    @Operation(
        summary = "List action type params",
        description = "Returns only the parameter definitions supported by one action type."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Action type params returned"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Action type not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<ActionParameterDefinition> params(@PathVariable UUID id) {
        return service.params(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an action type", description = "Deletes an action type definition and refreshes metadata cache.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Action type deleted"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Action type not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
