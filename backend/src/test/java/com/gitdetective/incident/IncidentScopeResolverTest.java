package com.gitdetective.incident;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.gitdetective.analyzer.RepositoryQueryService;
import com.gitdetective.assistant.intent.AssistantIntent;
import com.gitdetective.assistant.intent.IntentDetector;
import com.gitdetective.dto.response.SearchResultResponse;
import com.gitdetective.entity.CodeRepository;
import com.gitdetective.entity.InvestigationTargetType;
import com.gitdetective.repository.BranchJpaRepository;
import com.gitdetective.repository.CodeRepositoryJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IncidentScopeResolverTest {

    @Mock private IntentDetector intentDetector;
    @Mock private RepositoryQueryService repositoryQueryService;
    @Mock private CodeRepositoryJpaRepository codeRepositoryJpaRepository;
    @Mock private BranchJpaRepository branchJpaRepository;

    @InjectMocks private IncidentScopeResolver resolver;

    @Test
    @DisplayName("prefers an indexed class hit over the default branch")
    void resolvesClassFromSearch() {
        UUID repoId = UUID.randomUUID();
        when(intentDetector.detect(any())).thenReturn(AssistantIntent.AUTHENTICATION);
        when(repositoryQueryService.search(eq(repoId), any()))
                .thenReturn(
                        new SearchResultResponse(
                                "auth",
                                List.of(),
                                List.of(),
                                List.of(
                                        new SearchResultResponse.SearchHit(
                                                "class",
                                                UUID.randomUUID().toString(),
                                                "AuthService",
                                                "com.example.AuthService")),
                                List.of(),
                                List.of(),
                                List.of(),
                                List.of()));

        IncidentScope scope =
                resolver.resolve(
                        repoId, "Why did authentication become risky after recent changes?");

        assertThat(scope.targetType()).isEqualTo(InvestigationTargetType.CLASS);
        assertThat(scope.targetRef()).isEqualTo("com.example.AuthService");
    }

    @Test
    @DisplayName("falls back to the repository default branch when search misses")
    void fallsBackToDefaultBranch() {
        UUID repoId = UUID.randomUUID();
        when(intentDetector.detect(any())).thenReturn(AssistantIntent.GENERAL_INVESTIGATION);
        when(repositoryQueryService.search(eq(repoId), any()))
                .thenReturn(
                        new SearchResultResponse(
                                "q", List.of(), List.of(), List.of(), List.of(), List.of(),
                                List.of(), List.of()));
        when(codeRepositoryJpaRepository.findById(repoId))
                .thenReturn(
                        Optional.of(
                                CodeRepository.builder()
                                        .id(repoId)
                                        .name("demo")
                                        .sourceUri("https://github.com/acme/demo")
                                        .defaultBranch("main")
                                        .build()));
        when(branchJpaRepository.findByRepositoryIdAndName(repoId, "main"))
                .thenReturn(Optional.of(new com.gitdetective.entity.BranchEntity()));

        IncidentScope scope = resolver.resolve(repoId, "What looks risky?");

        assertThat(scope.targetType()).isEqualTo(InvestigationTargetType.BRANCH);
        assertThat(scope.targetRef()).isEqualTo("main");
    }

    @Test
    @DisplayName("extracts searchable terms and drops stopwords")
    void extractsTerms() {
        List<String> terms =
                resolver.searchTerms(
                        "Why did authentication become risky after the recent changes?",
                        AssistantIntent.AUTHENTICATION);

        assertThat(terms).contains("authentication", "risky", "auth");
        assertThat(terms).doesNotContain("why", "the", "after");
    }
}
