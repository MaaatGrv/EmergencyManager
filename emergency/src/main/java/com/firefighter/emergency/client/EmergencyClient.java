package com.firefighter.emergency.client;

import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import com.firefighter.emergency.dto.FireDto;

@Repository
public class EmergencyClient {

    private final RestTemplate restTemplate;
    private static final String API_URL = "http://vps.cpe-sn.fr:8081/";

    public EmergencyClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<FireDto> getAllFires() {
        return restTemplate.getForObject(API_URL + "/fires", List.class);
    }
}
