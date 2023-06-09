package com.firefighter.emergency.controller;

import com.firefighter.emergency.service.EmergencyService;

import jakarta.annotation.security.DeclareRoles;

import com.firefighter.emergency.dto.FacilityDto;
import com.firefighter.emergency.dto.FireDto;
import com.firefighter.emergency.dto.LiquidType;
import com.firefighter.emergency.dto.VehicleDto;
import com.firefighter.emergency.dto.Coord;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/emergency")
public class EmergencyController {

    @Autowired
    private EmergencyService emergencyService;

    @GetMapping("/fire")
    public List<FireDto> getAllFires() {
        return emergencyService.getAllFires();
    }

    @GetMapping("/vehicles/team")
    public List<VehicleDto> getTeamVehicles() {
        return emergencyService.getTeamVehicles();
    }

    @GetMapping("/vehicles")
    public List<VehicleDto> getAllVehicles() {
        return emergencyService.getAllVehicles();
    }

    @PutMapping("/vehicle/move/{id}")
    public VehicleDto moveVehicle(@PathVariable Integer id, @RequestBody Coord coord) {
        return emergencyService.moveVehicle(id, coord);
    }

    @GetMapping("/facilities/team")
    public List<FacilityDto> getTeamFacilities() {
        return emergencyService.getTeamFacilities();
    }

    @GetMapping("/facilities")
    public List<FacilityDto> getAllFacilities() {
        return emergencyService.getAllFacilities();
    }

    @PutMapping("vehicle/update/{id}")
    public void updateVehicleLiquidType(@PathVariable Integer id, @RequestBody LiquidType liquidType) {
        emergencyService.updateVehicleLiquidType(id, liquidType);
    }

    @DeleteMapping("/vehicle/{id}")
    public void deleteVehicle(@PathVariable Integer id) {
        emergencyService.deleteVehicle(id);
    }

    @PostMapping("/vehicle")
    public VehicleDto createVehicle(@RequestBody VehicleDto vehicleDto) {
        return emergencyService.createVehicle(vehicleDto);
    }

}