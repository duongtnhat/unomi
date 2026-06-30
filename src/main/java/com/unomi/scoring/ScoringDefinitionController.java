package com.unomi.scoring;

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
@RequestMapping("/api/scorings")
@Validated
@Tag(name = "Scorings", description = "Manage scoring definitions used by rule outputs.")
@SecurityRequirement(name = "apiKeyAuth")
public class ScoringDefinitionController {

    private final ScoringDefinitionService service;
    private final ProfileScoreService profileScoreService;

    public ScoringDefinitionController(
        ScoringDefinitionService service,
        ProfileScoreService profileScoreService
    ) {
        this.service = service;
        this.profileScoreService = profileScoreService;
    }

    @PostMapping
    @Operation(summary = "Create a scoring definition")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Scoring definition created"),
        @ApiResponse(responseCode = "400", description = "Invalid scoring payload",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<ScoringDefinitionResponse> create(@Valid @RequestBody ScoringDefinitionRequest request) {
        ScoringDefinitionResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/scorings/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update a scoring definition",
        description = "Updates name, bounds, direction flags, active state, and value type for an existing scoring definition."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Scoring definition updated"),
        @ApiResponse(responseCode = "400", description = "Invalid scoring payload",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Scoring definition not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ScoringDefinitionResponse update(
        @PathVariable UUID id,
        @Valid @RequestBody ScoringDefinitionRequest request
    ) {
        return service.update(id, request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a scoring definition", description = "Returns a scoring definition by ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Scoring definition returned"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Scoring definition not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ScoringDefinitionResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping
    @Operation(
        summary = "List scoring definitions",
        description = "Lists scoring definitions. Use active=true or active=false to filter by enabled state."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Scoring definitions returned"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public List<ScoringDefinitionResponse> list(@RequestParam(required = false) Boolean active) {
        return service.list(active);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a scoring definition", description = "Deletes a scoring definition and refreshes the metadata cache.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Scoring definition deleted"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Scoring definition not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profiles/{profileId}/scores")
    @Operation(
        summary = "Get profile scores",
        description = "Returns all score values currently attached to one customer profile."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile scores returned"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Profile not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ProfileScoreResponse getProfileScores(@PathVariable String profileId) {
        return profileScoreService.getScores(profileId);
    }

    @DeleteMapping("/profiles/{profileId}/scores")
    @Operation(
        summary = "Clear profile scores",
        description = "Removes all score values from one customer profile and records scoreChanged events for cleared scores."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile scores cleared"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Profile not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ProfileScoreResponse clearProfileScores(@PathVariable String profileId) {
        return profileScoreService.clearScores(profileId);
    }

    @PostMapping("/profiles/{profileId}/scores/{scoreKey}")
    @Operation(
        summary = "Set, increase, or decrease one profile score",
        description = "Updates one score on a customer profile using SET, INCREASE, or DECREASE. The scoring definition must exist and be active."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile score updated"),
        @ApiResponse(responseCode = "400", description = "Invalid score payload",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Profile or active scoring definition not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ProfileScoreResponse updateProfileScore(
        @PathVariable String profileId,
        @PathVariable String scoreKey,
        @Valid @RequestBody ProfileScoreRequest request
    ) {
        return profileScoreService.updateScore(profileId, scoreKey, request);
    }
}
