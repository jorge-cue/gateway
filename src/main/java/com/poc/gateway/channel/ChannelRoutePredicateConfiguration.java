package com.poc.gateway.channel;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ChannelRoutePredicateConfiguration {

    public enum Channel { WEB, MOBILE, OPEN_BANKING }

    @Bean("webChannelRoutePredicateConfig")
    public ChannelRoutePredicateFactory.Config webChannelRoutePredicateConfig() {
        return ChannelRoutePredicateFactory.Config.of(Channel.WEB.name(), List.of("10.1.0.0/8"));
    }

    @Bean
    public ChannelRoutePredicateFactory.Config mobileChannelRoutePredicateConfig() {
        return ChannelRoutePredicateFactory.Config.of(Channel.MOBILE.name(), List.of("10.1.0.0/8"));
    }

    @Bean
    public ChannelRoutePredicateFactory.Config openShiftChannelRoutePredicateConfig() {
        return ChannelRoutePredicateFactory.Config.of(Channel.OPEN_BANKING.name(), List.of("10.1.0.0/8"));
    }

    @Bean
    public ChannelRoutePredicateFactory channelRoutePredicateFactory() {
        return new ChannelRoutePredicateFactory(ChannelRoutePredicateFactory.Config.class);
    }

}
