package com.firefighter.emergency.controller;

import com.firefighter.emergency.service.EmergencyService;
import com.firefighter.emergency.dto.FacilityDto;
import com.firefighter.emergency.dto.FireDto;
import com.firefighter.emergency.dto.VehicleDto;
import com.firefighter.emergency.dto.Coord;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/emergency")
public class EmergencyController {

    @Autowired
    private EmergencyService emergencyService;

    @GetMapping("/fire")
    public List<FireDto> getAllFires() {
        return emergencyService.getAllFires();
    }

    @GetMapping("/vehicles")
    public List<VehicleDto> getAllVehicles() {
        return emergencyService.getAllVehicles();
    }

    @PutMapping("/vehicle/move/{id}")
    public VehicleDto moveVehicle(@PathVariable String id, @RequestBody Coord coord) {
        return emergencyService.moveVehicle(id, coord);
    }

    @GetMapping("/facilities")
    public List<FacilityDto> getAllFacilities() {
        return emergencyService.getTeamFacilities();
    }
}