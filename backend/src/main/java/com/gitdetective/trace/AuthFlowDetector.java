package com.gitdetective.trace;

import com.gitdetective.entity.AnnotationEntity;
import com.gitdetective.entity.CodeTypeEntity;
import com.gitdetective.entity.MethodEntity;
import com.gitdetective.investigation.InvestigationTarget;
import com.gitdetective.repository.AnnotationJpaRepository;
import com.gitdetective.repository.CodeTypeJpaRepository;
import com.gitdetective.repository.MethodJpaRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Detects Spring Security authentication artifacts only when concrete types/methods exist. */
@Component
@RequiredArgsConstructor
public class AuthFlowDetector {

    private static final Set<String> SECURITY_CONFIG_MARKERS =
            Set.of("SecurityFilterChain", "WebSecurityConfigurerAdapter", "SecurityConfig");
    private static final Set<String> AUTH_ANNOTATIONS =
            Set.of("EnableWebSecurity", "Configuration", "EnableMethodSecurity");

    private final CodeTypeJpaRepository codeTypeJpaRepository;
    private final MethodJpaRepository methodJpaRepository;
    private final AnnotationJpaRepository annotationJpaRepository;

    public List<TraceEngineStep> detect(InvestigationTarget target) {
        List<TraceEngineStep> steps = new ArrayList<>();
        int order = 1;
        for (CodeTypeEntity type :
                codeTypeJpaRepository.findByRepositoryIdOrderByFullyQualifiedNameAsc(
                        target.repositoryId())) {
            String name = type.getName();
            String fqn = type.getFullyQualifiedName();
            boolean securityConfig =
                    containsAny(name, SECURITY_CONFIG_MARKERS)
                            || containsAny(fqn, SECURITY_CONFIG_MARKERS)
                            || hasAnyAnnotation(type.getId(), AUTH_ANNOTATIONS);

            if (securityConfig) {
                steps.add(
                        new TraceEngineStep(
                                order++,
                                "SECURITY_CONFIG",
                                fqn,
                                type.getId().toString(),
                                "code_type:" + type.getId(),
                                "Security configuration type detected by name/annotation evidence"));
            }

            if (name.toLowerCase(Locale.ROOT).contains("jwt")
                    || name.toLowerCase(Locale.ROOT).contains("filter")) {
                if (name.toLowerCase(Locale.ROOT).contains("jwt") || name.endsWith("Filter")) {
                    steps.add(
                            new TraceEngineStep(
                                    order++,
                                    "JWT_OR_FILTER",
                                    fqn,
                                    type.getId().toString(),
                                    "code_type:" + type.getId(),
                                    "Filter/JWT type detected by naming evidence"));
                }
            }

            if (name.contains("AuthenticationProvider") || fqn.endsWith("AuthenticationProvider")) {
                steps.add(
                        new TraceEngineStep(
                                order++,
                                "AUTHENTICATION_PROVIDER",
                                fqn,
                                type.getId().toString(),
                                "code_type:" + type.getId(),
                                "AuthenticationProvider type detected"));
            }

            if (name.contains("UserDetailsService") || fqn.endsWith("UserDetailsService")) {
                steps.add(
                        new TraceEngineStep(
                                order++,
                                "USER_DETAILS_SERVICE",
                                fqn,
                                type.getId().toString(),
                                "code_type:" + type.getId(),
                                "UserDetailsService type detected"));
            }

            for (MethodEntity method : methodJpaRepository.findByTypeId(type.getId())) {
                if ("securityFilterChain".equals(method.getName())
                        || "filterChain".equals(method.getName())
                        || (method.getReturnType() != null
                                && method.getReturnType().contains("SecurityFilterChain"))) {
                    steps.add(
                            new TraceEngineStep(
                                    order++,
                                    "FILTER_CHAIN",
                                    fqn + "#" + method.getName(),
                                    method.getId().toString(),
                                    "method:" + method.getId(),
                                    "SecurityFilterChain bean method evidence: "
                                            + method.getSignature()));
                }
            }
        }
        return steps;
    }

    private boolean containsAny(String value, Set<String> markers) {
        for (String marker : markers) {
            if (value.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAnyAnnotation(java.util.UUID typeId, Set<String> annotations) {
        return annotationJpaRepository.findByOwnerKindAndOwnerId("TYPE", typeId).stream()
                .map(AnnotationEntity::getName)
                .anyMatch(annotations::contains);
    }

    public record TraceEngineStep(
            int stepOrder,
            String stepLabel,
            String displayName,
            String stepRef,
            String evidenceRef,
            String detail) {}
}
