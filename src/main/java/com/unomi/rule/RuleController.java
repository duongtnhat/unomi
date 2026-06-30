package com.unomi.rule;

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
@RequestMapping("/api/rules")
@Validated
@Tag(name = "Rules", description = "Manage rule definitions evaluated asynchronously from condition definitions.")
@SecurityRequirement(name = "apiKeyAuth")
public class RuleController {

    private final RuleDefinitionService service;

    public RuleController(RuleDefinitionService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
        summary = "Create or update a rule definition",
        description = "Upserts a rule by key. When its condition matches, outputs can update profile attributes, tags, scores, or create action events."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Rule created or updated"),
        @ApiResponse(responseCode = "400", description = "Invalid rule payload",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key",
            content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "404", description = "Referenced condition not found",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<RuleDefinitionResponse> upsert(@Valid @RequestBody RuleDefinitionRequest request) {
        RuleDefinitionResponse response = service.upsert(request);
        return ResponseEntity.created(URI.create("/api/rules/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a rule definition")
    public RuleDefinitionResponse get(@PathVariable UUID id) {
        return service.get(id);
    }

    @GetMapping
    @Operation(summary = "List rule definitions")
    public List<RuleDefinitionResponse> list(@RequestParam(required = false) Boolean active) {
        return service.list(active);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a rule definition")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
