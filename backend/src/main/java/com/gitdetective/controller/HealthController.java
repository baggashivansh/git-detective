package com.gitdetective.controller;

import com.gitdetective.common.ApiResponse;
import com.gitdetective.dto.response.HealthResponse;
import com.gitdetective.service.HealthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Health", description = "Application health and readiness")
public class HealthController {

    private final HealthService healthService;

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns application health status")
    public ResponseEntity<ApiResponse<HealthResponse>> health() {
        return ResponseEntity.ok(ApiResponse.ok(healthService.getHealth()));
    }
}
