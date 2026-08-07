package com.gitdetective.exception;

import org.springframework.http.HttpStatus;

/** Domain failures during repository analysis and indexing. */
public class RepositoryAnalysisException extends ApiException {

    public RepositoryAnalysisException(String errorCode, String message) {
        super(HttpStatus.BAD_REQUEST, errorCode, message);
    }

    public RepositoryAnalysisException(HttpStatus status, String errorCode, String message) {
        super(status, errorCode, message);
    }

    public RepositoryAnalysisException(
            HttpStatus status, String errorCode, String message, Throwable cause) {
        super(status, errorCode, message, cause);
    }

    public RepositoryAnalysisException(String errorCode, String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, errorCode, message, cause);
    }
}
