package com.gitdetective.dto.response;

import java.util.List;
import java.util.UUID;

public record RepositoryTreeNodeResponse(
        UUID id,
        String path,
        String name,
        String parentPath,
        boolean directory,
        String language,
        String extension,
        long sizeBytes,
        List<RepositoryTreeNodeResponse> children) {}
