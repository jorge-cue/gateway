package com.poc.gateway.routes;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = "gateway.downStreamURI=http://localhost:${wiremock.server.port}")
@WireMockTest
@AutoConfigureWireMock(port = 0, stubs = {"classpath:/wiremock/mappings"})
class RouteLocatorConfigurationTest {

    private static final String GET = "/get";
    private static final String DELAY_3 = "/delay/3";
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
                .header("Host", "test.circuitbreaker.com")
                .exchange()
                .expectStatus().isOk()
                .expectBody().consumeWith(result -> assertThat(new String(result.getResponseBody())).isEqualTo("fallback"));
    }
}