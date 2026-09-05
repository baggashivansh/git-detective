package com.gitdetective.incident;

import com.gitdetective.analyzer.RepositoryQueryService;
import com.gitdetective.assistant.intent.AssistantIntent;
import com.gitdetective.assistant.intent.IntentDetector;
import com.gitdetective.dto.response.SearchResultResponse;
import com.gitdetective.dto.response.SearchResultResponse.SearchHit;
import com.gitdetective.entity.BranchEntity;
import com.gitdetective.entity.CodeRepository;
import com.gitdetective.entity.InvestigationTargetType;
import com.gitdetective.exception.ResourceNotFoundException;
import com.gitdetective.repository.BranchJpaRepository;
import com.gitdetective.repository.CodeRepositoryJpaRepository;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Maps an investigation question to an existing indexed target. Never invents files, classes, or
 * branches.
 */
@Component
@RequiredArgsConstructor
public class IncidentScopeResolver {

    private static final Pattern TOKEN = Pattern.compile("[A-Za-z][A-Za-z0-9_-]{2,}");
    private static final Set<String> STOPWORDS =
            Set.of(
                    "the",
                    "and",
                    "for",
                    "why",
                    "did",
                    "does",
                    "how",
                    "what",
                    "when",
                    "who",
                    "after",
                    "before",
                    "recent",
                    "changes",
                    "change",
                    "become",
                    "became",
                    "this",
                    "that",
                    "with",
                    "from",
                    "into",
                    "was",
                    "were",
                    "are",
                    "been",
                    "have",
                    "has",
                    "had",
                    "not",
                    "our",
                    "your",
                    "their",
                    "about",
                    "over",
                    "under",
                    "than",
                    "then",
                    "they",
                    "them",
                    "code",
                    "repo",
                    "repository",
                    "please",
                    "investigate",
                    "investigation",
                    "incident");

    private final IntentDetector intentDetector;
    private final RepositoryQueryService repositoryQueryService;
    private final CodeRepositoryJpaRepository codeRepositoryJpaRepository;
    private final BranchJpaRepository branchJpaRepository;

    public IncidentScope resolve(UUID repositoryId, String question) {
        AssistantIntent intent = intentDetector.detect(question);
        for (String term : searchTerms(question, intent)) {
            SearchResultResponse results = repositoryQueryService.search(repositoryId, term);
            IncidentScope hit = firstHit(results);
            if (hit != null) {
                return hit;
            }
        }
        return defaultBranchScope(repositoryId);
    }

    List<String> searchTerms(String question, AssistantIntent intent) {
        LinkedHashSet<String> terms = new LinkedHashSet<>();
        if (question != null) {
            var matcher = TOKEN.matcher(question);
            while (matcher.find()) {
                String token = matcher.group().toLowerCase(Locale.ROOT);
                if (!STOPWORDS.contains(token)) {
                    terms.add(token);
                }
            }
        }
        terms.addAll(intentTerms(intent));
        return List.copyOf(terms);
    }

    private static List<String> intentTerms(AssistantIntent intent) {
        return switch (intent) {
            case AUTHENTICATION -> List.of("auth", "security", "login", "jwt");
            case REQUEST_FLOW -> List.of("controller", "request", "servlet");
            case OWNERSHIP -> List.of("owner", "contributor");
            case HOTSPOT -> List.of("hotspot", "service");
            case PACKAGE_HEALTH -> List.of("package");
            default -> List.of();
        };
    }

    private static IncidentScope firstHit(SearchResultResponse results) {
        SearchHit classHit = first(results.classes());
        if (classHit != null) {
            return new IncidentScope(
                    InvestigationTargetType.CLASS,
                    classHit.secondary() == null || classHit.secondary().isBlank()
                            ? classHit.id()
                            : classHit.secondary(),
                    "Matched indexed class for question terms");
        }
        SearchHit fileHit = first(results.files());
        if (fileHit != null) {
            return new IncidentScope(
                    InvestigationTargetType.FILE,
                    fileHit.secondary() == null || fileHit.secondary().isBlank()
                            ? fileHit.id()
                            : fileHit.secondary(),
                    "Matched indexed file for question terms");
        }
        SearchHit packageHit = first(results.packages());
        if (packageHit != null) {
            return new IncidentScope(
                    InvestigationTargetType.PACKAGE,
                    packageHit.label(),
                    "Matched indexed package for question terms");
        }
        return null;
    }

    private static SearchHit first(List<SearchHit> hits) {
        return hits == null || hits.isEmpty() ? null : hits.get(0);
    }

    private IncidentScope defaultBranchScope(UUID repositoryId) {
        CodeRepository repository =
                codeRepositoryJpaRepository
                        .findById(repositoryId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Repository not found: " + repositoryId));
        List<String> candidates = new ArrayList<>();
        if (repository.getDefaultBranch() != null && !repository.getDefaultBranch().isBlank()) {
            candidates.add(repository.getDefaultBranch());
        }
        candidates.add("main");
        candidates.add("master");
        for (String name : candidates) {
            if (branchJpaRepository.findByRepositoryIdAndName(repositoryId, name).isPresent()) {
                return new IncidentScope(
                        InvestigationTargetType.BRANCH,
                        name,
                        "No question-specific target; using repository branch " + name);
            }
        }
        return branchJpaRepository.findByRepositoryId(repositoryId).stream()
                .findFirst()
                .map(BranchEntity::getName)
                .map(
                        name ->
                                new IncidentScope(
                                        InvestigationTargetType.BRANCH,
                                        name,
                                        "No question-specific target; using first indexed branch"))
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "No indexed branch available for investigation"));
    }
}
