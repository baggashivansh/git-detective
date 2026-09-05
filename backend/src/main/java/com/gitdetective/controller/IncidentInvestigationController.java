package com.gitdetective.controller;

import com.gitdetective.common.ApiResponse;
import com.gitdetective.dto.request.StartIncidentInvestigationRequest;
import com.gitdetective.dto.response.IncidentInvestigationResponse;
import com.gitdetective.incident.IncidentInvestigationService;
import com.gitdetective.incident.IncidentPhase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/incident-investigations")
@RequiredArgsConstructor
@Tag(
        name = "Incident Investigations",
        description = "Question-driven evidence-backed incident investigation")
public class IncidentInvestigationController {

    private final IncidentInvestigationService incidentInvestigationService;

    @PostMapping
    @Operation(summary = "Start or reuse an incident investigation for a repository and question")
    public ResponseEntity<ApiResponse<IncidentInvestigationResponse>> start(
            @Valid @RequestBody StartIncidentInvestigationRequest request) {
        IncidentInvestigationResponse data = incidentInvestigationService.start(request);
        HttpStatus status =
                data.phase() == IncidentPhase.COMPLETED ? HttpStatus.CREATED : HttpStatus.ACCEPTED;
        return ResponseEntity.status(status).body(ApiResponse.ok(data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<IncidentInvestigationResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(incidentInvestigationService.list()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IncidentInvestigationResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(incidentInvestigationService.get(id)));
    }

    @PostMapping("/{id}/continue")
    @Operation(summary = "Advance a queued incident after repository analysis completes")
    public ResponseEntity<ApiResponse<IncidentInvestigationResponse>> continueIncident(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(incidentInvestigationService.advance(id)));
    }
}
