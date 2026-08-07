package com.gitdetective.dto.response;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

/** Payload returned by {@code GET /health}. */
@Value
@Builder
public class HealthResponse {

    String status;
    String application;
    String version;
    Instant timestamp;
}
