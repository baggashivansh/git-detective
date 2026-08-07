package com.gitdetective.entity;

public enum AnalysisStatus {
    QUEUED,
    CLONING,
    SCANNING,
    PARSING,
    INDEXING,
    COMPLETED,
    FAILED
}
