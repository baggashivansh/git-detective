package com.gitdetective.dto.response;

import java.util.UUID;

public record PackageResponse(UUID id, String name, String path, int fileCount) {}
