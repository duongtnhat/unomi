package com.unomi.customer.upsert;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.unomi.shared.ApiError;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/user/v1")
@Tag(name = "Customer Upsert", description = "Batch API for upserting customer identifiers, attributes, and events.")
@SecurityRequirement(name = "apiKeyAuth")
public class UpsertCustomerInfoController {

    private final UpsertCustomerInfoService service;

    public UpsertCustomerInfoController(UpsertCustomerInfoService service) {
        this.service = service;
    }

    @PostMapping("/upsert")
    @Operation(
        summary = "Batch upsert customer info",
        description = """
            Inserts or updates up to 1000 users in one request.

            Each user must include either insiderId or identifiers, and must include at least one attributes object or one events array.
            Attributes update the customer profile in Elasticsearch. Events are written to the customer-events index.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Request accepted and processed with per-user success/failure details"),
        @ApiResponse(responseCode = "400", description = "Invalid batch payload",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<UpsertCustomerInfoResponse> upsert(@RequestBody UpsertCustomerInfoRequest request) {
        return ResponseEntity.ok(service.upsert(request));
    }
}
