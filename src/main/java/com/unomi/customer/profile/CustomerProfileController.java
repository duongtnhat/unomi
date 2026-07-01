package com.unomi.customer.profile;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
@RequestMapping("/api/profiles")
@Validated
@Tag(name = "Customer Profiles", description = "Create, update, and read customer profiles stored in Elasticsearch.")
@SecurityRequirement(name = "apiKeyAuth")
public class CustomerProfileController {

    private final CustomerProfileService service;

    public CustomerProfileController(CustomerProfileService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
        summary = "Create or update a customer profile",
        description = "Upserts a profile by profileKey. Profile properties are stored in Elasticsearch."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Profile created or updated"),
        @ApiResponse(responseCode = "400", description = "Invalid request body",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<CustomerProfileResponse> upsert(@Valid @RequestBody CustomerProfileRequest request) {
        CustomerProfileResponse response = service.upsert(request);
        return ResponseEntity.created(URI.create("/api/profiles/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get a customer profile",
        description = "Returns one customer profile document by its Elasticsearch document ID."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile found"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Profile not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public CustomerProfileResponse get(
        @Parameter(description = "Profile document ID.", example = "5b412499-dfbd-43f1-8db1-ceeb6185ecd2")
        @PathVariable String id
    ) {
        return service.get(id);
    }

    @PostMapping("/search")
    @Operation(
        summary = "Search profiles by condition",
        description = "Evaluates a profile condition tree against customer profiles and returns a paged list of matched profiles."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profiles returned"),
        @ApiResponse(responseCode = "400", description = "Invalid request body",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public CustomerProfileSearchResponse search(
        @Valid @RequestBody CustomerProfileConditionSearchRequest request,
        @Parameter(description = "Zero-based page index.", example = "0")
        @org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size. Maximum value is 200.", example = "20")
        @org.springframework.web.bind.annotation.RequestParam(defaultValue = "20") int size
    ) {
        return service.search(request, page, size);
    }
}
