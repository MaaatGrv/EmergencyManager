package com.firefighter.vehicle.controller;

import com.firefighter.vehicle.model.Vehicle;
import com.firefighter.vehicle.service.VehicleService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class VehicleController {
    @Autowired
    private VehicleService vehicleService;

    @GetMapping("/vehicle")
    public List<Vehicle> getAllVehicles() {
        return vehicleService.getAllVehicles();
    }

    @GetMapping("/vehicle/{id}")
    public Vehicle getVehicle(@PathVariable Integer id) {
        return vehicleService.getVehicle(id);
    }

    @PostMapping("/vehicle")
    public Vehicle addVehicle(@RequestBody Vehicle vehicle) {
        return vehicleService.addVehicle(vehicle);
    }

    @PutMapping("/vehicle/{id}")
    public Vehicle updateVehicle(@PathVariable Integer id, @RequestBody Vehicle vehicle) {
        return vehicleService.updateVehicle(id, vehicle);
    }

    @DeleteMapping("/vehicle/{id}")
    public void deleteVehicle(@PathVariable Integer id) {
        vehicleService.deleteVehicle(id);
    }
}