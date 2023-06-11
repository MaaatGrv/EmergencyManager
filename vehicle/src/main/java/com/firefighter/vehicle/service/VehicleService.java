package com.firefighter.vehicle.service;

import com.firefighter.vehicle.model.Vehicle;
import com.firefighter.vehicle.repository.VehicleRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VehicleService {
    @Autowired
    private VehicleRepository vehicleRepository;

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public Vehicle getVehicle(Integer id) {
        return vehicleRepository.findById(id).orElse(null);
    }

    public Vehicle addVehicle(Vehicle vehicle) {
        return vehicleRepository.save(vehicle);
    }

    public Vehicle updateVehicle(Integer id, Vehicle vehicle) {
        vehicle.setId(id);
        return vehicleRepository.save(vehicle);
    }

    public void deleteVehicle(Integer id) {
        vehicleRepository.deleteById(id);
    }
}
