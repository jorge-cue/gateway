package com.poc.gateway.routes;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "downstreamURI=http://localhost:${wiremock.server.port}"
)
@WireMockTest
class RouteLocatorConfigurationTest {

    @Autowired
    private WebTestClient webClient;

    @Test
    void getRouteInjectsHelloHeader() {
        stubFor(get("/get").willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withBody("{\"headers\":[\"Hello\":\"World\"]}"))
        );

        webClient.get().uri("/get")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.headers.Hello").isEqualTo("World");
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void circuitBreakerWorksAsExpected() {
        stubFor(get("/delay/3").willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withBody("no fallback")
                .withFixedDelay(3000))
        );

        webClient.get()
                .uri("/delay/3")
                .header("Host", "test.circuitbreaker.com")
                .exchange()
                .expectStatus().isOk()
                .expectBody().consumeWith(result -> assertThat(new String(result.getResponseBody())).isEqualTo("fallback"));
    }
}