package com.poc.gateway.routes;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
public class RouteLocatorProperties {

    private String downStreamURI = "http://httpbin.org:80";

    public String getDownStreamURI() {
        return downStreamURI;
    }

    @ConfigurationProperties
    public void setDownStreamURI(String downStreamURI) {
        this.downStreamURI = downStreamURI;
    }
}
