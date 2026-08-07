package com.gitdetective.trace;

import com.gitdetective.entity.AnnotationEntity;
import com.gitdetective.entity.CodeTypeEntity;
import com.gitdetective.entity.CodeTypeKind;
import com.gitdetective.entity.FileEntity;
import com.gitdetective.entity.MethodEntity;
import com.gitdetective.investigation.InvestigationTarget;
import com.gitdetective.repository.AnnotationJpaRepository;
import com.gitdetective.repository.CodeTypeJpaRepository;
import com.gitdetective.repository.FileJpaRepository;
import com.gitdetective.repository.MethodJpaRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Discovers Controller → Service → Repository → Entity flows only when annotations/names provide
 * evidence. Never invents paths.
 */
@Component
@RequiredArgsConstructor
public class RequestTraceEngine {

    private static final Set<String> CONTROLLER_ANNOTATIONS =
            Set.of(
                    "RestController",
                    "Controller",
                    "org.springframework.web.bind.annotation.RestController");
    private static final Set<String> SERVICE_ANNOTATIONS =
            Set.of("Service", "org.springframework.stereotype.Service");
    private static final Set<String> REPOSITORY_ANNOTATIONS =
            Set.of("Repository", "org.springframework.stereotype.Repository");
    private static final Set<String> ENTITY_ANNOTATIONS =
            Set.of("Entity", "jakarta.persistence.Entity", "javax.persistence.Entity");

    private final CodeTypeJpaRepository codeTypeJpaRepository;
    private final MethodJpaRepository methodJpaRepository;
    private final AnnotationJpaRepository annotationJpaRepository;
    private final FileJpaRepository fileJpaRepository;

    public List<TraceStep> discover(InvestigationTarget target) {
        List<TraceStep> steps = new ArrayList<>();
        List<CodeTypeEntity> types =
                codeTypeJpaRepository.findByRepositoryIdOrderByFullyQualifiedNameAsc(
                        target.repositoryId());

        List<CodeTypeEntity> controllers =
                filterByAnnotationOrName(types, CONTROLLER_ANNOTATIONS, "Controller");
        List<CodeTypeEntity> services =
                filterByAnnotationOrName(types, SERVICE_ANNOTATIONS, "Service");
        List<CodeTypeEntity> repositories =
                filterByAnnotationOrName(types, REPOSITORY_ANNOTATIONS, "Repository");
        List<CodeTypeEntity> entities =
                filterByAnnotationOrName(types, ENTITY_ANNOTATIONS, "Entity");

        int order = 1;
        for (CodeTypeEntity controller : controllers) {
            if (!matchesTarget(target, controller)) {
                continue;
            }
            steps.add(step(order++, "CONTROLLER", controller, "Annotated/named controller type"));
            for (MethodEntity method : methodJpaRepository.findByTypeId(controller.getId())) {
                steps.add(
                        new TraceStep(
                                order++,
                                "CONTROLLER_METHOD",
                                controller.getFullyQualifiedName() + "#" + method.getName(),
                                method.getId().toString(),
                                "method:" + method.getId(),
                                method.getSignature()));
            }
            for (CodeTypeEntity service : services) {
                steps.add(
                        step(
                                order++,
                                "SERVICE",
                                service,
                                "Service type present in repository index"));
            }
            for (CodeTypeEntity repository : repositories) {
                steps.add(
                        step(
                                order++,
                                "REPOSITORY",
                                repository,
                                "Repository type present in repository index"));
            }
            for (CodeTypeEntity entity : entities) {
                steps.add(
                        step(
                                order++,
                                "ENTITY",
                                entity,
                                "JPA entity type present in repository index"));
            }
            break;
        }

        if (steps.isEmpty() && target.typeId() != null) {
            codeTypeJpaRepository
                    .findById(target.typeId())
                    .ifPresent(
                            type -> {
                                String role = classify(type);
                                if (role != null) {
                                    steps.add(
                                            step(
                                                    1,
                                                    role,
                                                    type,
                                                    "Target type classification from evidence"));
                                }
                            });
        }

        return steps;
    }

    private List<CodeTypeEntity> filterByAnnotationOrName(
            List<CodeTypeEntity> types, Set<String> annotations, String nameSuffix) {
        List<CodeTypeEntity> matched = new ArrayList<>();
        for (CodeTypeEntity type : types) {
            if (type.getKind() != CodeTypeKind.CLASS && type.getKind() != CodeTypeKind.INTERFACE) {
                continue;
            }
            boolean nameMatch = type.getName().endsWith(nameSuffix);
            boolean annotationMatch =
                    annotationJpaRepository.findByOwnerKindAndOwnerId("TYPE", type.getId()).stream()
                            .map(AnnotationEntity::getName)
                            .anyMatch(annotations::contains);
            if (nameMatch || annotationMatch) {
                matched.add(type);
            }
        }
        return matched;
    }

    private boolean matchesTarget(InvestigationTarget target, CodeTypeEntity type) {
        if (target.typeId() != null) {
            return target.typeId().equals(type.getId());
        }
        if (target.typeFqn() != null) {
            return target.typeFqn().equals(type.getFullyQualifiedName());
        }
        if (target.filePath() != null && type.getFileId() != null) {
            return fileJpaRepository
                    .findById(type.getFileId())
                    .map(FileEntity::getPath)
                    .map(path -> path.equals(target.filePath()))
                    .orElse(false);
        }
        return true;
    }

    private String classify(CodeTypeEntity type) {
        if (hasAnnotation(type.getId(), CONTROLLER_ANNOTATIONS)
                || type.getName().toLowerCase(Locale.ROOT).endsWith("controller")) {
            return "CONTROLLER";
        }
        if (hasAnnotation(type.getId(), SERVICE_ANNOTATIONS)
                || type.getName().toLowerCase(Locale.ROOT).endsWith("service")) {
            return "SERVICE";
        }
        if (hasAnnotation(type.getId(), REPOSITORY_ANNOTATIONS)
                || type.getName().toLowerCase(Locale.ROOT).endsWith("repository")) {
            return "REPOSITORY";
        }
        if (hasAnnotation(type.getId(), ENTITY_ANNOTATIONS)) {
            return "ENTITY";
        }
        return null;
    }

    private boolean hasAnnotation(UUID typeId, Set<String> annotations) {
        return annotationJpaRepository.findByOwnerKindAndOwnerId("TYPE", typeId).stream()
                .map(AnnotationEntity::getName)
                .anyMatch(annotations::contains);
    }

    private TraceStep step(int order, String kind, CodeTypeEntity type, String detail) {
        return new TraceStep(
                order,
                kind,
                type.getFullyQualifiedName(),
                type.getId().toString(),
                "code_type:" + type.getId(),
                detail);
    }

    public record TraceStep(
            int stepOrder,
            String stepLabel,
            String displayName,
            String stepRef,
            String evidenceRef,
            String detail) {}
}
