package com.firefighter.emergency.controller;

import com.firefighter.emergency.service.EmergencyService;
import com.firefighter.emergency.dto.FireDto;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/emergency")
public class EmergencyController {

    @Autowired
    private EmergencyService emergencyService;

    @GetMapping("/fire")
    public List<FireDto> getAllFires() {
        return emergencyService.getAllFires();
    }

}
