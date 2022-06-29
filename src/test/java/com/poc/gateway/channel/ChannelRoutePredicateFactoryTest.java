package com.poc.gateway.channel;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ChannelRoutePredicateFactoryTest {

    // Subject Under Test
    ChannelRoutePredicateFactory factory = new ChannelRoutePredicateFactory(ChannelRoutePredicateFactory.Config.class);

    @ParameterizedTest
    @MethodSource("channelRoutePredicateCase")
    void channelRoutePredicateCase(List<String> validNetworks, String remoteAddress, boolean expectedResult) throws UnknownHostException {
        var config = ChannelRoutePredicateFactory.Config.of(validNetworks);
        var webExchange = MockServerWebExchange.builder(
                        MockServerHttpRequest.get("/get").remoteAddress(new InetSocketAddress(InetAddress.getByName(remoteAddress), 4567)).build())
                .build();
        var actualResult = factory.apply(config).test(webExchange);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    static Stream<Arguments> channelRoutePredicateCase() {
        final var SUB_NETWORKS_1 = List.of("10.0.0.0/8");
        final var SUB_NETWORKS_2 = List.of("10.0.0.0/12");
        final var SUB_NETWORKS_3 = List.of("10.0.0.0/8", "127.0.0.1");
        return Stream.of(
                Arguments.of(SUB_NETWORKS_1, "10.0.0.1", true),
                Arguments.of(SUB_NETWORKS_1, "10.0.0.255", true),
                Arguments.of(SUB_NETWORKS_1, "10.0.1.1", false),
                Arguments.of(SUB_NETWORKS_2, "10.0.0.1", true),
                Arguments.of(SUB_NETWORKS_2, "10.0.15.254", true),
                Arguments.of(SUB_NETWORKS_2, "10.0.16.255", false),
                Arguments.of(SUB_NETWORKS_2, "10.0.255.255", false),
                Arguments.of(SUB_NETWORKS_3, "localhost", true)
        );
    }

}