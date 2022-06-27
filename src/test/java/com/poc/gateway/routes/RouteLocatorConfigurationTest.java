package com.poc.gateway.routes;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "gateway.downStreamURI=http://localhost:${wiremock.server.port}")
@WireMockTest
@AutoConfigureWireMock(port = 0, stubs = {"classpath:/wiremock/RouteLocatorConfigurationTest/mappings"})
class RouteLocatorConfigurationTest {

    public static final String FALLBACK = "fallback";
    private static final String GET = "/get";
    private static final String DELAY_3 = "/delay/3";
    public static final String TEST_CIRCUITBREAKER_COM = "test.circuitbreaker.com";
    @Autowired
    private WebTestClient webClient;

    @Test
    void getRouteInjectsHelloWorldHeader() {
        webClient.get().uri(GET)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.headers.Hello").isEqualTo("World");
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void circuitBreakerWorksAsExpected() {
        webClient.get()
                .uri(DELAY_3)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.HOST, TEST_CIRCUITBREAKER_COM)
                .exchange()
                .expectStatus().isOk()
                .expectBody().consumeWith(result -> assertThat(new String(result.getResponseBody())).isEqualTo(FALLBACK));
    }
}
