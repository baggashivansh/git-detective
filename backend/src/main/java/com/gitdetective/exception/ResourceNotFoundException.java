package com.gitdetective.exception;

import org.springframework.http.HttpStatus;

/** Raised when a requested resource cannot be located. */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", message);
    }
}
