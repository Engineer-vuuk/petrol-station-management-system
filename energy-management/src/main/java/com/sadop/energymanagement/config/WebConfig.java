package com.sadop.energymanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")  // Allow CORS for all API endpoints
                .allowedOrigins("http://localhost:5173")  // Frontend URL (no trailing slash)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Add OPTIONS for preflight request
                .allowedHeaders("*")
                .allowCredentials(true);  // Allow credentials (cookies, authorization headers, etc.)
    }
}
