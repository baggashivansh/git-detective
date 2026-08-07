package com.gitdetective.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${gitdetective.application.version}")
    private String applicationVersion;

    @Bean
    public OpenAPI gitDetectiveOpenApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Git Detective API")
                                .description(
                                        "Production API for the Git Detective investigation platform. "
                                                + "Phase 1 exposes infrastructure endpoints only.")
                                .version(applicationVersion)
                                .contact(
                                        new Contact()
                                                .name("Shivansh Bagga")
                                                .url("https://github.com/shivanshbagga"))
                                .license(
                                        new License()
                                                .name("Proprietary")
                                                .url("https://github.com")));
    }
}
