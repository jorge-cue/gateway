package com.poc.gateway.routes;

import com.poc.gateway.channel.ChannelRoutePredicateFactory;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

@Configuration
public class RouteLocatorConfiguration {

    private final RouteLocatorProperties routeLocatorProperties;

    public RouteLocatorConfiguration(RouteLocatorProperties routeLocatorProperties) {
        this.routeLocatorProperties = routeLocatorProperties;
    }

    private static final ChannelRoutePredicateFactory.Config WEB_CHANNEL_CONFIG = ChannelRoutePredicateFactory.Config.of(List.of("10.1.0.0/8"));
    private static final ChannelRoutePredicateFactory.Config MOBILE_CHANNEL_CONFIG = ChannelRoutePredicateFactory.Config.of(List.of("10.1.1.0/8"));
    private static final ChannelRoutePredicateFactory.Config OPEN_BANKING_CHANNEL_CONFIG = ChannelRoutePredicateFactory.Config.of(List.of("10.1.2.0/8"));

    private static final ChannelRoutePredicateFactory CHANNEL_ROUTE_PREDICATE_FACTORY = new ChannelRoutePredicateFactory(ChannelRoutePredicateFactory.Config.class);

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("get", p -> p
                        .path("/get")
                        .filters(f -> f.addRequestHeader("Hello", "World"))
                        .uri(routeLocatorProperties.getDownStreamURI())
                )
                .route("circuitBreaker", p -> p
                        .host("*.circuitbreaker.com")
                        .filters(f -> {
                            f.circuitBreaker(config -> config
                                    .setName("myCommand")
                                    .setFallbackUri("redirect:/fallback"));
                            f.retry(3);
                            return f;
                        })
                        .uri(routeLocatorProperties.getDownStreamURI())
                )
                // Routes by channel, depending on where it comes from
                .route("web-channel", p -> p
                                .predicate(CHANNEL_ROUTE_PREDICATE_FACTORY.apply(WEB_CHANNEL_CONFIG))
                                .filters(f -> f.addRequestHeader("X-Channel", "WEB"))
                                .uri(routeLocatorProperties.getDownStreamURI())
                )
                .route("mobile-channel", p -> p
                                .predicate(CHANNEL_ROUTE_PREDICATE_FACTORY.apply(MOBILE_CHANNEL_CONFIG))
                                .filters(f -> f.addRequestHeader("X-Channel", "MOBILE"))
                                .uri(routeLocatorProperties.getDownStreamURI())
                )
                .route("open-banking-channel", p -> p
                                .predicate(CHANNEL_ROUTE_PREDICATE_FACTORY.apply(OPEN_BANKING_CHANNEL_CONFIG))
                                .filters(f -> f.addRequestHeader("X-Channel", "OPEN-BANKING"))
                                .uri(routeLocatorProperties.getDownStreamURI())
                )
                .build();
    }
}
