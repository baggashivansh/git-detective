package com.gitdetective.evidence.validator;

/** Thrown when evidence records or bundles fail deterministic validation. */
public class EvidenceValidationException extends RuntimeException {

    public EvidenceValidationException(String message) {
        super(message);
    }
}
