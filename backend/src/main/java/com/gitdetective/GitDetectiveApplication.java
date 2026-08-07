package com.gitdetective;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/**
 * Application entry point.
 *
 * <p>{@link UserDetailsServiceAutoConfiguration} is excluded because Phase 1 uses a permit-all
 * security chain without an in-memory user store. Authentication will be introduced explicitly in a
 * later phase.
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class GitDetectiveApplication {

    public static void main(String[] args) {
        SpringApplication.run(GitDetectiveApplication.class, args);
    }
}
