package com.predictflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;

@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/auth/**", "/api/auth/**")
                        .uri("http://localhost:8081"))
                .route("txn-service", r -> r.path("/txn/**", "/api/txn/**")
                        .uri("http://localhost:8082"))
                .route("retry-engine", r -> r.path("/retry/**", "/api/retry/**")
                        .uri("http://localhost:8083"))
                .build();
    }
}