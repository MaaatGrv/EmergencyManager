package com.firefighter.emergency.service;

import com.firefighter.emergency.client.EmergencyClient;
import com.firefighter.emergency.dto.Coord;
import com.firefighter.emergency.dto.FacilityDto;
import com.firefighter.emergency.dto.FireDto;
import com.firefighter.emergency.dto.LiquidType;
import com.firefighter.emergency.dto.VehicleDto;

import java.util.List;

import org.springframework.http.ResponseEntity;
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

    public List<VehicleDto> getAllVehicles() {
        return emergencyClient.getAllVehicles();
    }

    public VehicleDto moveVehicle(Integer vehicleId, Coord coord) {
        return emergencyClient.moveVehicle(vehicleId, coord);
    }

    public List<FacilityDto> getTeamFacilities() {
        List<FacilityDto> facilities = emergencyClient.getAllFacilities();
        // Get facilities with the id 35 or 3918
        facilities.removeIf(facility -> facility.getId() != 35 && facility.getId() != 3918);
        return facilities;
    }

    public List<VehicleDto> getOtherVehicles() {
        List<VehicleDto> vehicles = emergencyClient.getOtherVehicles();
        // The vehicles can not have facilityRefId 35 or 3918
        vehicles.removeIf(vehicle -> vehicle.getFacilityRefID() == 35 || vehicle.getFacilityRefID() == 3918);
        return vehicles;
    }

    public void updateVehicleLiquidType(Integer id, LiquidType bestLiquid) {
        emergencyClient.updateVehicleLiquidType(id, bestLiquid);
    }

}
