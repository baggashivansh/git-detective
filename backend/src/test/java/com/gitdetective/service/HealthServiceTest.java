package com.gitdetective.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.gitdetective.dto.response.HealthResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HealthServiceTest {

    @Test
    @DisplayName("getHealth returns UP status with configured metadata")
    void getHealthReturnsConfiguredMetadata() {
        HealthService healthService = new HealthService("git-detective", "1.0.0");

        HealthResponse response = healthService.getHealth();

        assertThat(response.getStatus()).isEqualTo("UP");
        assertThat(response.getApplication()).isEqualTo("git-detective");
        assertThat(response.getVersion()).isEqualTo("1.0.0");
        assertThat(response.getTimestamp()).isNotNull();
    }
}
