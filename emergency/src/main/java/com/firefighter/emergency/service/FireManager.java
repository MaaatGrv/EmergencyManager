package com.firefighter.emergency.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.firefighter.emergency.dto.VehicleDto;
import com.firefighter.emergency.dto.Coord;
import com.firefighter.emergency.dto.FireDto;

@Service
public class FireManager {

    private final EmergencyService emergencyService;
    private final MapBoxService mapboxService;

    public FireManager(EmergencyService emergencyService, MapBoxService mapboxService) {
        this.emergencyService = emergencyService;
        this.mapboxService = mapboxService;
    }

    @Scheduled(fixedRate = 60000) // Run every 60 seconds
    public void handleFires() {
        List<FireDto> fires = emergencyService.getAllFires();
        for (FireDto fire : fires) {
            // Find best vehicle for this fire
            VehicleDto bestVehicle = findBestVehicleForFire(fire);
            // Move vehicle to fire location
            emergencyService.moveVehicle(bestVehicle.getId(), fire.getCoord());
        }
    }

    private VehicleDto findBestVehicleForFire(FireDto fire) {
        List<VehicleDto> vehicles = emergencyService.getAllVehicles();
        VehicleDto bestVehicle = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (VehicleDto vehicle : vehicles) {

            Coord vehicleCoord = new Coord();
            vehicleCoord.setLat(vehicle.getLat());
            vehicleCoord.setLon(vehicle.getLon());

            double distance = calculateDistance(vehicleCoord, fire.getCoord());
            double fuelNeeded = distance * vehicle.getType().getFuelConsumption();
            if (vehicle.getFuel() < fuelNeeded) {
                continue; // Skip this vehicle if it doesn't have enough fuel to reach the fire
            }
            double score = calculateScore(vehicle, fire, distance);
            if (score > bestScore) {
                bestScore = score;
                bestVehicle = vehicle;
            }
        }
        return bestVehicle;
    }

    private double calculateScore(VehicleDto vehicle, FireDto fire, double distance) {
        double liquidScore = vehicle.getLiquidQuantity() * vehicle.getLiquidType().getEfficiency(fire.getType());
        double efficiencyScore = vehicle.getType().getEfficiency() * 10;
        double distanceScore = 1 / distance; // Higher score for shorter distance
        return liquidScore + efficiencyScore + distanceScore;
    }

    private double calculateDistance(Coord coord1, Coord coord2) {
        return mapboxService.getDistance(coord1, coord2);
    }

    private double computeCrewScore(VehicleDto vehicle) {
        return 0;
    }

    private double computeFuelQuantityScore(VehicleDto vehicle) {
        return 0;
    }

    private double computeLiquidTypeScore(VehicleDto vehicle, FireDto fire) {
        return 0;
    }

    private double computeLiquidQuantityScore(VehicleDto vehicle) {
        return 0;
    }

    private double computeDistanceScore(VehicleDto vehicle, FireDto fire) {
        return 0;
    }

}
