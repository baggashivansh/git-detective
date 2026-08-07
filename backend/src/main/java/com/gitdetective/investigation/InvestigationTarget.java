package com.gitdetective.investigation;

import com.gitdetective.entity.InvestigationTargetType;
import java.util.UUID;

/** Resolved investigation target with stable identifiers for evidence linking. */
public record InvestigationTarget(
        InvestigationTargetType type,
        String ref,
        String label,
        UUID repositoryId,
        UUID fileId,
        String filePath,
        UUID packageId,
        String packageName,
        UUID typeId,
        String typeFqn,
        UUID methodId,
        String methodSignature,
        UUID commitId,
        String commitSha,
        UUID contributorId,
        String contributorEmail,
        String branchName,
        String tagName) {}
