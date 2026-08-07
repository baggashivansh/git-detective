package com.gitdetective.service;

import com.gitdetective.dto.response.HealthResponse;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HealthService {

    private final String applicationName;
    private final String applicationVersion;

    public HealthService(
            @Value("${spring.application.name}") String applicationName,
            @Value("${gitdetective.application.version}") String applicationVersion) {
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
    }

    public HealthResponse getHealth() {
        return HealthResponse.builder()
                .status("UP")
                .application(applicationName)
                .version(applicationVersion)
                .timestamp(Instant.now())
                .build();
    }
}
