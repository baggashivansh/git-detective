package com.gitdetective.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Baseline JPA configuration.
 *
 * <p>No business entities are registered in Phase 1. Auditing is enabled so future entities can
 * inherit created/updated timestamps without additional infrastructure work.
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.gitdetective.repository")
public class JpaConfig {}
