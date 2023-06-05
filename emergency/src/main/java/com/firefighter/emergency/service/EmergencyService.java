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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class EmergencyService {

    private final EmergencyClient emergencyClient;
    private final MapBoxService mapboxService = new MapBoxService();

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
    private static final long MOVE_INTERVAL_MS = 100;
    private double speed = 10;

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

    public void moveVehicleUniformly(Integer vehicleId, Coord targetCoord) {
        // Get the current position of the vehicle
        VehicleDto vehicle = getVehicleById(vehicleId);
        Coord currentCoord = new Coord();
        currentCoord.setLat(vehicle.getLat());
        currentCoord.setLon(vehicle.getLon());

        // Calculate the total distance to travel
        double totalDistance = mapboxService.getDistance(currentCoord, targetCoord);

        // Calculate the number of steps needed
        int steps = (int) Math.ceil(totalDistance / speed);

        // Calculate the change in latitude and longitude per step
        double deltaLat = (targetCoord.getLat() - currentCoord.getLat()) / steps;
        double deltaLon = (targetCoord.getLon() - currentCoord.getLon()) / steps;

        System.out.println("Moving vehicle " + vehicleId + " to " + targetCoord + " in " + steps + " steps");

        executorService.scheduleAtFixedRate(() -> {
            // Update the current position
            currentCoord.setLat(currentCoord.getLat() + deltaLat);
            currentCoord.setLon(currentCoord.getLon() + deltaLon);

            // Move the vehicle to the new position
            emergencyClient.moveVehicle(vehicleId, currentCoord);

            // Check if the vehicle has reached its destination
            if (mapboxService.getDistance(currentCoord, targetCoord) <= speed) {
                // If the vehicle has reached its destination, cancel the task
                throw new RuntimeException("Destination reached");
            }
        }, 0, MOVE_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    public VehicleDto getVehicleById(Integer vehicleId) {
        return emergencyClient.getVehicleById(vehicleId);
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
