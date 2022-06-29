package com.poc.gateway.channel;

import lombok.Builder;
import lombok.Data;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.function.Predicate;

public class ChannelRoutePredicateFactory extends AbstractRoutePredicateFactory<ChannelRoutePredicateFactory.Config> {

    public ChannelRoutePredicateFactory(Class<Config> configClass) {
        super(configClass);
    }

    @Override
    public Predicate<ServerWebExchange> apply(Config config) {
        return exchange -> {
            final var remoteAddress = exchange.getRequest().getRemoteAddress().getHostString();
            return config.networkSpecs.stream().map(IpAddressMatcher::new).anyMatch(matcher -> matcher.matches(remoteAddress));
        };
    }

    @Data(staticConstructor = "of")
    public static class Config {
        /**
         * List of subnets where a Channel can come from, for example:
         *
         * <table>
         *     <theader>
         *         <tr>
         *          <th>Channel name</th>
         *          <th>Subnets</th>
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
