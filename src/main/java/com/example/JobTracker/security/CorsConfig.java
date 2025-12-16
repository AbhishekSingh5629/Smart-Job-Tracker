package com.example.JobTracker.security;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials (cookies, authorization headers)
        config.setAllowCredentials(true);

        // Allow frontend origins
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",      // React default
                "http://localhost:5173",      // Vite default
                "http://localhost:5174",
                "http://127.0.0.1:3000",
                "http://127.0.0.1:5173",
                "http://localhost:5175",
                "http://127.0.0.1:5175",
                "http://127.0.0.1:5500"       // Live Server
        ));

        // Allow all headers
        config.setAllowedHeaders(Arrays.asList("*"));

        // Allow all methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Expose Authorization header
        config.setExposedHeaders(Arrays.asList("Authorization"));

        // Max age
        config.setMaxAge(3600L);

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}