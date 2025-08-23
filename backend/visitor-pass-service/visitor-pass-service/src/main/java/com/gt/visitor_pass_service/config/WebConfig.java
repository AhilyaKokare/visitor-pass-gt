package com.gt.visitor_pass_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    // Injects the comma-separated list of URLs from application.properties
    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    /**
     * Creates a global CORS configuration for the entire application.
     * This is the modern, recommended way to handle CORS in Spring Boot.
     * @return A WebMvcConfigurer bean that applies the CORS policy.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // Apply this policy to all API endpoints
                        .allowedOrigins(allowedOrigins) // Allow origins from our properties file
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Specify allowed methods
                        .allowedHeaders("*") // Allow all headers
                        .allowCredentials(true) // Allow cookies and authentication headers
                        .maxAge(3600); // Cache the pre-flight response for 1 hour
            }
        };
    }
}