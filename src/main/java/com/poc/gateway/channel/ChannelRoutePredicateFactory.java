package com.poc.gateway.channel;

import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.function.Predicate;

public class ChannelRoutePredicateFactory extends AbstractRoutePredicateFactory<ChannelRoutePredicateFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger("com.poc.gateway.channel.ChannelRoutePredicate");

    public static final String X_ORIGIN_CHANNEL = "X-Origin-Channel";

    public ChannelRoutePredicateFactory(Class<Config> configClass) {
        super(configClass);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return exchange -> {
            try {
                var request = exchange.getRequest();
                var headers = request.getHeaders();
                log.info("Testing if exchange is for channel {} using request from {} and headers {}", config.id, exchange.getRequest().getRemoteAddress(), headers);
                if (headers.containsKey(X_ORIGIN_CHANNEL)) {
                    log.info("Testing Header whether it is assigned to Channel {}.", config.id);
                    return headers.get(X_ORIGIN_CHANNEL).contains(config.id);
                }
                final var remoteAddress = request.getRemoteAddress().getHostString();
                log.info("Testing if remoteAddress {} belongs to one of {} for channel {}", remoteAddress, config.networkSpecs, config.id);
                return config.networkSpecs.stream().map(IpAddressMatcher::new).anyMatch(matcher -> matcher.matches(remoteAddress));
            } catch (Exception x) {
                log.error("Exception testing for channel {}", config.id, x);
                return false;
            }
        };
    }

    @Data(staticConstructor = "of")
    public static class Config {
        private final String id;
        /**
         * List of subnets where a request can come from, for example:
         *
         * <table>
         *     <theader>
         *         <tr>
         *          <th>Channel name</th>
         *          <th>Sub networks</th>
         *          <th>Examples and Notes</th>
         *         <tr></tr>
         *     </theader>
         *     <tbody>
         *         <tr>
         *             <td>WEB</td>
         *             <td>10.1.0.0/8</td>
         *             <td>Any Server in the range 10.1.0.1 through 10.1.0.254 is accepted as belonging to  Web Channel.</td>
         *         </tr>
         *         <tr>
         *             <td>Mobile</td>
         *             <td>10.1.1.0/8</td>
         *             <td>Any server in the range 10.1.1.1 through 10.1.1.254 is accepted as belonging to Mobile channel</td>
         *         </tr>
         *         <tr>
         *             <td>Open Banking</td>
         *             <td>10.1.2.0/8</td>
         *             <td>Any server in the range 10.1.2.1 through 10.1.2.254 is accepted as belonging to Open Banking channel</td>
         *         </tr>
         *     </tbody>
         * </table>
         *
         */
        private final List<String> networkSpecs;
    }
}
