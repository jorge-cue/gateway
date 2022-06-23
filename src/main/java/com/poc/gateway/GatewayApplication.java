package com.poc.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayApplication {

    @SuppressWarnings("resource")
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
