package com.unomi.segment;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
@RequestMapping("/api/segments")
@Validated
@Tag(name = "Segments", description = "Manage segment definitions evaluated from condition definitions.")
@SecurityRequirement(name = "apiKeyAuth")
public class SegmentController {

    private final SegmentDefinitionService service;

    public SegmentController(SegmentDefinitionService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
        summary = "Create or update a segment definition",
        description = "Upserts a segment by key. Membership is calculated from the referenced condition definition."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Segment created or updated"),
        @ApiResponse(responseCode = "400", description = "Invalid segment payload",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Referenced condition not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<SegmentDefinitionResponse> upsert(@Valid @RequestBody SegmentDefinitionRequest request) {
        SegmentDefinitionResponse response = service.upsert(request);
        return ResponseEntity.created(URI.create("/api/segments/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a segment definition")
    public SegmentDefinitionResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping
    @Operation(summary = "List segment definitions")
    public List<SegmentDefinitionResponse> list(@RequestParam(required = false) Boolean active) {
        return service.list(active);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a segment definition")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
