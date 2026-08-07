package com.gitdetective.controller;

import com.gitdetective.common.ApiResponse;
import com.gitdetective.dto.request.CreateInvestigationRequest;
import com.gitdetective.dto.response.InvestigationDetailResponse;
import com.gitdetective.dto.response.InvestigationReportResponse;
import com.gitdetective.dto.response.InvestigationSummaryResponse;
import com.gitdetective.investigation.InvestigationService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/investigations")
@RequiredArgsConstructor
@Tag(name = "Investigations", description = "Deterministic investigation engine APIs")
public class InvestigationController {

    private final InvestigationService investigationService;

    @PostMapping
    @Operation(
            summary = "Create a deterministic investigation case from indexed repository metadata")
    public ResponseEntity<ApiResponse<InvestigationSummaryResponse>> create(
            @Valid @RequestBody CreateInvestigationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(investigationService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InvestigationSummaryResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(investigationService.list()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvestigationDetailResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(investigationService.get(id)));
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<ApiResponse<InvestigationDetailResponse>> timeline(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(investigationService.timeline(id)));
    }

    @GetMapping("/{id}/ownership")
    public ResponseEntity<ApiResponse<InvestigationDetailResponse>> ownership(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(investigationService.ownership(id)));
    }

    @GetMapping("/{id}/impact")
    public ResponseEntity<ApiResponse<InvestigationDetailResponse>> impact(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(investigationService.impact(id)));
    }

    @GetMapping("/{id}/relationships")
    public ResponseEntity<ApiResponse<InvestigationDetailResponse>> relationships(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(investigationService.relationships(id)));
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<ApiResponse<InvestigationReportResponse>> report(
            @PathVariable UUID id, @RequestParam(defaultValue = "json") String format) {
        return ResponseEntity.ok(ApiResponse.ok(investigationService.report(id, format)));
    }
}
