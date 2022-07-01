package com.poc.gateway.routes;

import com.poc.gateway.channel.ChannelRoutePredicateFactory;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import java.time.Duration;
import java.util.List;

@Configuration
public class RouteLocatorConfiguration {

    public static final String X_CHANNEL = "X-Channel";
    private final RouteLocatorProperties routeLocatorProperties;
    private final ChannelRoutePredicateFactory.Config webChannelRoutePredicateConfig;
    private final ChannelRoutePredicateFactory.Config mobileChannelRoutePredicateConfig;
    private final ChannelRoutePredicateFactory.Config openShiftChannelRoutePredicateConfig;
    private final ChannelRoutePredicateFactory channelRoutePredicateFactory;

    public RouteLocatorConfiguration(RouteLocatorProperties routeLocatorProperties,
                                  @Qualifier("webChannelRoutePredicateConfig")  ChannelRoutePredicateFactory.Config webChannelRoutePredicateConfig,
                                  @Qualifier("mobileChannelRoutePredicateConfig")  ChannelRoutePredicateFactory.Config mobileChannelRoutePredicateConfig,
                                  @Qualifier("openShiftChannelRoutePredicateConfig")  ChannelRoutePredicateFactory.Config openShiftChannelRoutePredicateConfig,
                                     ChannelRoutePredicateFactory channelRoutePredicateFactory

    ) {
        this.routeLocatorProperties = routeLocatorProperties;
        this.webChannelRoutePredicateConfig = webChannelRoutePredicateConfig;
        this.mobileChannelRoutePredicateConfig = mobileChannelRoutePredicateConfig;
        this.openShiftChannelRoutePredicateConfig = openShiftChannelRoutePredicateConfig;
        this.channelRoutePredicateFactory = channelRoutePredicateFactory;
    }

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Routes by channel, depending on where it comes from
                .route("web-channel", p -> p
                        .predicate(channelRoutePredicateFactory.apply(webChannelRoutePredicateConfig))
                        .filters(f -> f
                                .addRequestHeader(X_CHANNEL, webChannelRoutePredicateConfig.getId())
                                .removeRequestHeader(ChannelRoutePredicateFactory.X_ORIGIN_CHANNEL)
                                .removeRequestHeader("Cookie"))
                        .uri(routeLocatorProperties.getDownStreamURI())
                )
                .route("mobile-channel", p -> p
                        .predicate(channelRoutePredicateFactory.apply(mobileChannelRoutePredicateConfig))
                        .filters(f -> f
                                .addRequestHeader(X_CHANNEL, mobileChannelRoutePredicateConfig.getId())
                                .removeRequestHeader(ChannelRoutePredicateFactory.X_ORIGIN_CHANNEL)
                                .removeRequestHeader("Cookie"))
                        .uri(routeLocatorProperties.getDownStreamURI())
                )
                .route("open-banking-channel", p -> p
                        .predicate(channelRoutePredicateFactory.apply(openShiftChannelRoutePredicateConfig))
                        .filters(f -> f
                                .addRequestHeader(X_CHANNEL, openShiftChannelRoutePredicateConfig.getId())
                                .removeRequestHeader(ChannelRoutePredicateFactory.X_ORIGIN_CHANNEL)
                                .removeRequestHeader("Cookie"))
                        .uri(routeLocatorProperties.getDownStreamURI())
                )
                // Routes to test minimum case and circuit breaker
                .route("get", p -> p
                        .method(HttpMethod.GET)
                        .and()
                        .path("/get")
                        .filters(f -> f.addRequestHeader("Hello", "World"))
                        .uri(routeLocatorProperties.getDownStreamURI())
                )
                .route("circuitBreaker", p -> p
                        .method(HttpMethod.GET)
                        .and()
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
                .build();
    }
}
