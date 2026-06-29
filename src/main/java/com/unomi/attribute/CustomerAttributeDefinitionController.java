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
@RequestMapping("/api/customer-attributes")
@Validated
@Tag(name = "Customer Attributes", description = "CRUD API for customer attribute definitions stored in PostgreSQL.")
@SecurityRequirement(name = "apiKeyAuth")
public class CustomerAttributeDefinitionController {

    private final AttributeDefinitionService service;

    public CustomerAttributeDefinitionController(AttributeDefinitionService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create customer attribute definition")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Customer attribute definition created"),
        @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<AttributeDefinitionResponse> create(@Valid @RequestBody AttributeDefinitionRequest request) {
        AttributeDefinitionResponse response = service.createCustomer(request);
        return ResponseEntity.created(URI.create("/api/customer-attributes/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer attribute definition")
    public AttributeDefinitionResponse update(
        @Parameter(description = "Customer attribute definition ID.")
        @PathVariable UUID id,
        @Valid @RequestBody AttributeDefinitionRequest request
    ) {
        return service.updateCustomer(id, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer attribute definition")
    public AttributeDefinitionResponse get(
        @Parameter(description = "Customer attribute definition ID.")
        @PathVariable UUID id
    ) {
        return service.getCustomer(id);
    }

    @GetMapping
    @Operation(summary = "List customer attribute definitions")
    public List<AttributeDefinitionResponse> list() {
        return service.listCustomers();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer attribute definition")
    public ResponseEntity<Void> delete(
        @Parameter(description = "Customer attribute definition ID.")
        @PathVariable UUID id
    ) {
        service.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
