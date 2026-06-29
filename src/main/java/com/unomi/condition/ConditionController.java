package com.unomi.condition;

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
@RequestMapping("/api/conditions")
@Validated
@Tag(name = "Conditions", description = "Manage and evaluate condition trees inspired by Apache Unomi.")
@SecurityRequirement(name = "apiKeyAuth")
public class ConditionController {

    private final ConditionEvaluatorService evaluatorService;
    private final ConditionDefinitionService definitionService;

    public ConditionController(
        ConditionEvaluatorService evaluatorService,
        ConditionDefinitionService definitionService
    ) {
        this.evaluatorService = evaluatorService;
        this.definitionService = definitionService;
    }

    @PostMapping
    @Operation(
        summary = "Create or update a condition definition",
        description = "Upserts a condition definition by key and version. The payload is stored as PostgreSQL JSONB."
    )
    public ResponseEntity<ConditionDefinitionResponse> upsert(
        @Valid @RequestBody ConditionDefinitionRequest request
    ) {
        ConditionDefinitionResponse response = definitionService.upsert(request);
        return ResponseEntity.created(URI.create("/api/conditions/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a condition definition")
    public ConditionDefinitionResponse get(@PathVariable UUID id) {
        return definitionService.get(id);
    }

    @GetMapping
    @Operation(summary = "List condition definitions")
    public List<ConditionDefinitionResponse> list(@RequestParam(required = false) Boolean active) {
        return definitionService.list(active);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a condition definition")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        definitionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/evaluate")
    @Operation(
        summary = "Evaluate a condition",
        description = "Evaluates a condition tree against supplied profile and event context."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Condition evaluated"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ConditionEvaluationResponse evaluate(@RequestBody ConditionEvaluationRequest request) {
        boolean matched = evaluatorService.evaluate(request.condition(), request.profile(), request.event());
        return new ConditionEvaluationResponse(matched);
    }
}
