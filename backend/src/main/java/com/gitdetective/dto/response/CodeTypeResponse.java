package com.gitdetective.dto.response;

import com.gitdetective.entity.CodeTypeKind;
import java.util.UUID;

public record CodeTypeResponse(
        UUID id,
        String name,
        String fullyQualifiedName,
        CodeTypeKind kind,
        String visibility,
        String superclassName,
        String packageName) {}
