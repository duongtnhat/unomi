package com.unomi.customer.event;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/events")
@Validated
@Tag(name = "Customer Events", description = "Ingest and query customer events stored in Elasticsearch.")
@SecurityRequirement(name = "apiKeyAuth")
public class CustomerEventController {

    private final CustomerEventService service;

    public CustomerEventController(CustomerEventService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
        summary = "Ingest a customer event",
        description = "Writes one event document to Elasticsearch. Use the batch upsert API when sending attributes and events together."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Event ingested"),
        @ApiResponse(responseCode = "400", description = "Invalid request body",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<CustomerEventResponse> ingest(@Valid @RequestBody CustomerEventRequest request) {
        CustomerEventResponse response = service.ingest(request);
        return ResponseEntity.created(URI.create("/api/events/" + response.id())).body(response);
    }

    @GetMapping
    @Operation(
        summary = "List events for a profile",
        description = "Returns the latest 50 events for a customer profile, ordered by occurredAt descending."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Events returned"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<CustomerEventResponse> listByProfile(
        @Parameter(description = "Profile ID to query events for.", example = "5b412499-dfbd-43f1-8db1-ceeb6185ecd2")
        @RequestParam String profileId
    ) {
        return service.listByProfile(profileId);
    }
}
