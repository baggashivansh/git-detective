package com.gitdetective.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Production security baseline.
 *
 * <p>API remains permit-all (no end-user auth in v1.0). CSRF stays disabled for the stateless JSON
 * API. Security headers and CORS protect browser clients.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(
                        headers -> {
                            headers.contentTypeOptions(Customizer.withDefaults());
                            headers.frameOptions(frame -> frame.deny());
                            headers.referrerPolicy(
                                    referrer ->
                                            referrer.policy(
                                                    ReferrerPolicyHeaderWriter.ReferrerPolicy
                                                            .STRICT_ORIGIN_WHEN_CROSS_ORIGIN));
                            headers.permissionsPolicy(
                                    permissions ->
                                            permissions.policy(
                                                    "camera=(), microphone=(), geolocation=()"));
                            headers.httpStrictTransportSecurity(
                                    hsts ->
                                            hsts.includeSubDomains(true)
                                                    .preload(false)
                                                    .maxAgeInSeconds(31536000));
                        })
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}
