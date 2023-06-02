package com.firefighter.vehicle;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class VehicleConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
