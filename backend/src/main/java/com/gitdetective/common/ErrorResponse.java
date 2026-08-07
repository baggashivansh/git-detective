package com.gitdetective.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

/** Standard envelope for API error responses. */
@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    boolean success;
    String message;
    String errorCode;
    String path;
    Instant timestamp;
    List<FieldViolation> violations;
    Map<String, Object> details;

    @Value
    @Builder
    public static class FieldViolation {
        String field;
        String message;
        Object rejectedValue;
    }
}
