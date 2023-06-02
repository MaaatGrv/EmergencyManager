package com.firefighter.emergency.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.firefighter.emergency.dto.VehicleDto;
import com.firefighter.emergency.dto.Coord;
import com.firefighter.emergency.dto.FacilityDto;
import com.firefighter.emergency.dto.FireDto;
import com.firefighter.emergency.dto.LiquidType;

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
        List<VehicleDto> vehicles = emergencyService.getAllVehicles();
        List<FacilityDto> facilities = emergencyService.getTeamFacilities();

        // Determine the most needed liquid type based on the current fires
        LiquidType mostNeededLiquidType = determineMostNeededLiquidType(fires);

        for (VehicleDto vehicle : vehicles) {
            if (vehicle.getLiquidQuantity() <= 0) {
                // This vehicle is out of liquid, move it to the nearest facility to refuel
                FacilityDto nearestFacility = findNearestFacility(vehicle, facilities);
                Coord nearestFacilityCoord = new Coord();
                nearestFacilityCoord.setLat(nearestFacility.getLat());
                nearestFacilityCoord.setLon(nearestFacility.getLon());
                emergencyService.moveVehicle(vehicle.getId(), nearestFacilityCoord);
                // Change the vehicle's liquid type to the most needed one
                vehicle.setLiquidType(mostNeededLiquidType);
                continue;
            }
            for (FireDto fire : fires) {
                // Find best vehicle for this fire
                VehicleDto bestVehicle = findBestVehicleForFire(fire);
                // Move vehicle to fire location
                emergencyService.moveVehicle(bestVehicle.getId(), fire.getCoord());
            }
        }
    }

    private LiquidType determineMostNeededLiquidType(List<FireDto> fires) {
        // We'll keep track of the total efficiency score for each liquid type here
        Map<LiquidType, Double> liquidScores = new HashMap<>();

        for (LiquidType liquidType : LiquidType.values()) {
            double liquidScore = 0;
            for (FireDto fire : fires) {
                liquidScore += liquidType.getEfficiency(fire.getType()) * computeFireImportance(fire);
            }
            liquidScores.put(liquidType, liquidScore);
        }

        // Now we just need to find the liquid type with the highest score
        LiquidType bestLiquidType = null;
        double highestScore = Double.NEGATIVE_INFINITY;
        for (Map.Entry<LiquidType, Double> entry : liquidScores.entrySet()) {
            if (entry.getValue() > highestScore) {
                highestScore = entry.getValue();
                bestLiquidType = entry.getKey();
            }
        }

        return bestLiquidType;
    }

    private double computeFireImportance(FireDto fire) {
        // This method should return a score representing the importance of putting out
        // this fire.
        // This could be based on factors like the fire's size, intensity, and proximity
        // to populated areas.
        // For now, let's just return 1, treating all fires as equally important.
        return 1;
    }

    private FacilityDto findNearestFacility(VehicleDto vehicle, List<FacilityDto> facilities) {
        FacilityDto nearestFacility = null;
        double shortestDistance = Double.POSITIVE_INFINITY;

        for (FacilityDto facility : facilities) {

            Coord facilityCoord = new Coord();
            facilityCoord.setLat(facility.getLat());
            facilityCoord.setLon(facility.getLon());

            Coord vehicleCoord = new Coord();
            vehicleCoord.setLat(vehicle.getLat());
            vehicleCoord.setLon(vehicle.getLon());

            double distance = calculateDistance(vehicleCoord, facilityCoord);
            if (distance < shortestDistance) {
                shortestDistance = distance;
                nearestFacility = facility;
            }
        }

        if (nearestFacility == null) {
            throw new RuntimeException("No facilities found");
        }

        return nearestFacility;
    }

    private VehicleDto findBestVehicleForFire(FireDto fire) {
        List<VehicleDto> vehicles = emergencyService.getAllVehicles();
        List<VehicleDto> otherVehicles = emergencyService.getOtherVehicles();
        VehicleDto bestVehicle = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        // Check if other vehicles are already at this fire
        boolean isFireBeingHandled = otherVehicles.stream()
                .anyMatch(v -> v.getLat() == fire.getCoord().getLat() && v.getLon() == fire.getCoord().getLon());

        if (isFireBeingHandled) {
            return null; // If the fire is being handled, we don't need to send a vehicle
        }

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
