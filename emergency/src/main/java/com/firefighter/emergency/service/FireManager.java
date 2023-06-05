package com.firefighter.emergency.service;

import java.util.ArrayList;
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

    @Scheduled(fixedRate = 30000) // Run every 30 seconds
    public void handleFires() {
        List<FireDto> fires = emergencyService.getAllFires();
        List<VehicleDto> vehicles = emergencyService.getAllVehicles();
        List<FacilityDto> facilities = emergencyService.getTeamFacilities();

        List<FireDto> unhandledFires = new ArrayList<>(fires);

        for (VehicleDto vehicle : vehicles) {
            if (vehicle.getLiquidQuantity() <= 0) {
                FacilityDto nearestFacility = findNearestFacility(vehicle, facilities);
                emergencyService.moveVehicleUniformly(vehicle.getId(), nearestFacility.getCoord());

                // if the vehicle is at the facility, refill its liquid
                double epsilon = 0.00001;
                while ((Math.abs(vehicle.getCoord().getLat() - nearestFacility.getLat()) < epsilon
                        && Math.abs(vehicle.getCoord().getLon() - nearestFacility.getLon()) < epsilon)) {
                    System.out.println(
                            "Vehicle " + vehicle.getId() + " is moving to facility " + nearestFacility.getId());
                }

                while (true) {
                    // Récupère les données mises à jour du véhicule depuis l'API
                    vehicle = emergencyService.getVehicleById(vehicle.getId());

                    // Vérifie si le réservoir de liquide est plein
                    if (vehicle.getLiquidQuantity() >= vehicle.getType().getLiquidCapacity()) {
                        break;
                    }

                    System.out.println("Vehicle " + vehicle.getId() + " is refilling liquid : "
                            + vehicle.getLiquidQuantity() + "/" + vehicle.getType().getLiquidCapacity());

                    // Met en pause le thread pour un intervalle de temps avant la prochaine
                    // vérification
                    try {
                        Thread.sleep(6000); // Pause pour 1 seconde
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // if the vehicle's liquid quantity is at its capacity, update its liquid type
                if (vehicle.getLiquidQuantity() == vehicle.getType().getLiquidCapacity()) {
                    // Set the liquid type to the most needed type
                    LiquidType mostNeededLiquidType = determineMostNeededLiquidType(unhandledFires);
                    vehicle.setLiquidType(mostNeededLiquidType);
                } else {
                    // If the vehicle is not yet refueled, skip the rest of this iteration
                    continue;
                }
            }

            updateVehicles();
            FireDto bestFire = null;
            double bestScore = Double.NEGATIVE_INFINITY;
            for (FireDto fire : unhandledFires) {
                double score = calculateScore(vehicle, fire);
                if (score > bestScore) {
                    bestScore = score;
                    bestFire = fire;
                }
            }

            if (bestFire == null) {
                continue;
            }

            emergencyService.moveVehicleUniformly(vehicle.getId(), bestFire.getCoord());
            unhandledFires.remove(bestFire);
            System.out.println("Vehicle " + vehicle.getId() + " is handling fire " + bestFire.getId());
        }
    }

    private double calculateScore(VehicleDto vehicle, FireDto fire) {
        Coord vehicleCoord = new Coord();
        vehicleCoord.setLat(vehicle.getLat());
        vehicleCoord.setLon(vehicle.getLon());

        double distance = calculateDistance(vehicleCoord, fire.getCoord());
        double distanceInKm = distance / 1000;
        double fuelNeeded = distanceInKm * vehicle.getType().getFuelConsumption();

        if (vehicle.getFuel() < fuelNeeded) {
            return Double.NEGATIVE_INFINITY; // This vehicle can't reach the fire
        }

        double liquidScore = vehicle.getLiquidQuantity() * vehicle.getLiquidType().getEfficiency(fire.getType());
        double efficiencyScore = vehicle.getType().getEfficiency() * 10;
        double distanceScore = 1 / distance;

        return liquidScore + efficiencyScore + distanceScore;
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
            System.out.println("Distance: " + distance);
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

    public void updateVehicles() {
        List<VehicleDto> vehicles = emergencyService.getAllVehicles();
        List<FireDto> fires = emergencyService.getAllFires();
        for (VehicleDto vehicle : vehicles) {
            LiquidType bestLiquid = determineBestLiquidForVehicle(vehicle, fires);
            if (!bestLiquid.equals(vehicle.getLiquidType())) {
                // Update the liquid type of the vehicle if a better one is found
                emergencyService.updateVehicleLiquidType(vehicle.getId(), bestLiquid);
            }
        }
    }

    private LiquidType determineBestLiquidForVehicle(VehicleDto vehicle, List<FireDto> fires) {
        LiquidType bestLiquid = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        for (LiquidType liquid : LiquidType.values()) {
            double score = 0.0;
            for (FireDto fire : fires) {
                // Only consider fires that the vehicle is capable of handling
                if (vehicle.getLiquidQuantity() * liquid.getEfficiency(fire.getType()) > fire.getIntensity()) {
                    score += liquid.getEfficiency(fire.getType()) * fire.getIntensity();
                }
            }
            if (score > bestScore) {
                bestScore = score;
                bestLiquid = liquid;
            }
        }
        return bestLiquid;
    }
}
