package com.poc.gateway.routes;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteLocatorConfiguration {

    private final RouteLocatorProperties properties;

    public RouteLocatorConfiguration(RouteLocatorProperties properties) {
        this.properties = properties;
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("get", p -> p
                        .path("/get")
                        .filters(f -> f.addRequestHeader("Hello", "World"))
                        .uri(properties.getDownStreamURI())
                )
                .route("circuitBreaker", p -> p
                        .host("*.circuitbreaker.com")
                        .filters(f -> f.circuitBreaker(config -> config
                                .setName("myCommand")
                                .setFallbackUri("redirect:/fallback")))
                        .uri(properties.getDownStreamURI())
                )
                .build();
    }
}
