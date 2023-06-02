package com.firefighter.emergency.service;

import com.firefighter.emergency.client.EmergencyClient;
import com.firefighter.emergency.dto.FireDto;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class EmergencyService {

    private final EmergencyClient emergencyClient;

    public EmergencyService(EmergencyClient emergencyClient) {
        this.emergencyClient = emergencyClient;
    }

    public List<FireDto> getAllFires() {
        return emergencyClient.getAllFires();
    }
}
