package com.gitdetective.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA configuration for knowledge-base repositories and assistant conversation memory.
 *
 * <p>{@code @EnableJpaRepositories} replaces Boot's default repository scan, so every repository
 * package must be listed explicitly.
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(
        basePackages = {"com.gitdetective.repository", "com.gitdetective.assistant.memory"})
public class JpaConfig {}
