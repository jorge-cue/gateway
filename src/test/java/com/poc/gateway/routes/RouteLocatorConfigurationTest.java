package com.poc.gateway.routes;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.poc.gateway.channel.ChannelRoutePredicateConfiguration;
import com.poc.gateway.channel.ChannelRoutePredicateFactory;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "routes.downStreamURI=http://localhost:${wiremock.server.port}")
@WireMockTest
@AutoConfigureWireMock(port = 0, stubs = {"classpath:/wiremock/RouteLocatorConfigurationTest/mappings"})
class RouteLocatorConfigurationTest {

    private static final Logger log = LoggerFactory.getLogger(RouteLocatorConfigurationTest.class);

    public static final String FALLBACK = "fallback";
    private static final String GET = "/get";
    private static final String DELAY_3 = "/delay/3";
    public static final String TEST_CIRCUITBREAKER_COM = "test.circuitbreaker.com";
    private final WebTestClient webClient;

    @Autowired
    public RouteLocatorConfigurationTest(WebTestClient webClient) {
        this.webClient = webClient;
    }

    @Test
    void getRouteInjectsHelloWorldHeader() {
        webClient.get().uri(GET).header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.headers.Hello").isEqualTo("World");
    }

    @Test
    void circuitBreakerWorksAsExpected() {
        webClient.get().uri(DELAY_3).header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
                .header(HttpHeaders.HOST, TEST_CIRCUITBREAKER_COM)
                .exchange()
                .expectStatus().isOk()
                .expectBody().consumeWith(result -> assertThat(new String(result.getResponseBody())).isEqualTo(FALLBACK));
    }

    @Test
    void webChannel() {
        webClient.get().uri("/")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(ChannelRoutePredicateFactory.X_ORIGIN_CHANNEL, ChannelRoutePredicateConfiguration.Channel.WEB.name())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.headers.X-Channel").isEqualTo(ChannelRoutePredicateConfiguration.Channel.WEB.name())
                .jsonPath("$.headers.X-Origin-Channel").doesNotExist();
    }

    @Test
    void mobileChannel() {
        webClient.get().uri("/")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(ChannelRoutePredicateFactory.X_ORIGIN_CHANNEL, ChannelRoutePredicateConfiguration.Channel.MOBILE.name())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.headers.X-Channel").isEqualTo(ChannelRoutePredicateConfiguration.Channel.MOBILE.name())
                .jsonPath("$.headers.X-Origin-Channel").doesNotExist();
    }

    @Test
    void openBankingChannel() {
        webClient.get().uri("/")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .header(ChannelRoutePredicateFactory.X_ORIGIN_CHANNEL, ChannelRoutePredicateConfiguration.Channel.OPEN_BANKING.name())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.headers.X-Channel").isEqualTo(ChannelRoutePredicateConfiguration.Channel.OPEN_BANKING.name())
                .jsonPath("$.headers.X-Origin-Channel").doesNotExist();
    }

}
