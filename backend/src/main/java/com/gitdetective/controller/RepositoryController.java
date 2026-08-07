package com.gitdetective.controller;

import com.gitdetective.analyzer.RepositoryCommandService;
import com.gitdetective.analyzer.RepositoryQueryService;
import com.gitdetective.common.ApiResponse;
import com.gitdetective.dto.request.AnalyzeRepositoryRequest;
import com.gitdetective.dto.response.CodeTypeResponse;
import com.gitdetective.dto.response.CommitResponse;
import com.gitdetective.dto.response.ContributorResponse;
import com.gitdetective.dto.response.LanguageStatisticResponse;
import com.gitdetective.dto.response.PackageResponse;
import com.gitdetective.dto.response.RepositoryStatisticsResponse;
import com.gitdetective.dto.response.RepositorySummaryResponse;
import com.gitdetective.dto.response.RepositoryTreeNodeResponse;
import com.gitdetective.dto.response.SearchResultResponse;
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
@RequestMapping("/repositories")
@RequiredArgsConstructor
@Tag(name = "Repositories", description = "Repository intelligence APIs")
public class RepositoryController {

    private final RepositoryCommandService repositoryCommandService;
    private final RepositoryQueryService repositoryQueryService;

    @PostMapping("/analyze")
    @Operation(summary = "Queue repository analysis for a local path or public GitHub URL")
    public ResponseEntity<ApiResponse<RepositorySummaryResponse>> analyze(
            @Valid @RequestBody AnalyzeRepositoryRequest request) {
        RepositorySummaryResponse response = repositoryCommandService.analyze(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.ok(response));
    }

    @GetMapping
    @Operation(summary = "List analyzed repositories")
    public ResponseEntity<ApiResponse<List<RepositorySummaryResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(repositoryQueryService.listRepositories()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get repository summary and analysis progress")
    public ResponseEntity<ApiResponse<RepositorySummaryResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(repositoryQueryService.getRepository(id)));
    }

    @GetMapping("/{id}/tree")
    public ResponseEntity<ApiResponse<List<RepositoryTreeNodeResponse>>> tree(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(repositoryQueryService.getTree(id)));
    }

    @GetMapping("/{id}/contributors")
    public ResponseEntity<ApiResponse<List<ContributorResponse>>> contributors(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(repositoryQueryService.getContributors(id)));
    }

    @GetMapping("/{id}/languages")
    public ResponseEntity<ApiResponse<List<LanguageStatisticResponse>>> languages(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(repositoryQueryService.getLanguages(id)));
    }

    @GetMapping("/{id}/commits")
    public ResponseEntity<ApiResponse<List<CommitResponse>>> commits(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.ok(repositoryQueryService.getCommits(id, page, size)));
    }

    @GetMapping("/{id}/statistics")
    public ResponseEntity<ApiResponse<RepositoryStatisticsResponse>> statistics(
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(repositoryQueryService.getStatistics(id)));
    }

    @GetMapping("/{id}/packages")
    public ResponseEntity<ApiResponse<List<PackageResponse>>> packages(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(repositoryQueryService.getPackages(id)));
    }

    @GetMapping("/{id}/classes")
    public ResponseEntity<ApiResponse<List<CodeTypeResponse>>> classes(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(repositoryQueryService.getClasses(id)));
    }

    @GetMapping("/{id}/search")
    @Operation(summary = "Search files, folders, classes, packages, commits, branches, and tags")
    public ResponseEntity<ApiResponse<SearchResultResponse>> search(
            @PathVariable UUID id, @RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.ok(repositoryQueryService.search(id, q)));
    }
}
