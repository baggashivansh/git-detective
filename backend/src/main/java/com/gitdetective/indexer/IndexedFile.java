package com.gitdetective.indexer;

import java.nio.file.Path;
import java.time.Instant;

public record IndexedFile(
        Path absolutePath,
        String relativePath,
        String name,
        String parentPath,
        String extension,
        String language,
        long sizeBytes,
        int lineCount,
        boolean directory,
        boolean binary,
        boolean hidden,
        boolean ignored,
        String contentHash,
        Instant createdAtFs,
        Instant modifiedAtFs) {}
