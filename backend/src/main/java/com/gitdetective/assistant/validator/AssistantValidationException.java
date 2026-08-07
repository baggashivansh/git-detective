package com.gitdetective.assistant.validator;

/** Raised when an AI response fails evidence validation. */
public class AssistantValidationException extends RuntimeException {

    public AssistantValidationException(String message) {
        super(message);
    }

    public AssistantValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
