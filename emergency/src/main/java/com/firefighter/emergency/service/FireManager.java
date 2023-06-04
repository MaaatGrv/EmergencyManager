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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class FireManager {

    private final EmergencyService emergencyService;
    private final MapBoxService mapboxService;
    private final ScheduledExecutorService executorService;

    public FireManager(EmergencyService emergencyService, MapBoxService mapboxService,
            ScheduledExecutorService executorService) {
        this.emergencyService = emergencyService;
        this.mapboxService = mapboxService;
        this.executorService = executorService;
    }

    @Scheduled(fixedRate = 60000) // Run every 60 seconds
    public void handleFires() {
        List<FireDto> fires = emergencyService.getAllFires();
        List<VehicleDto> vehicles = emergencyService.getAllVehicles();
        List<FacilityDto> facilities = emergencyService.getTeamFacilities();

        List<FireDto> unhandledFires = new ArrayList<>(fires);

        for (VehicleDto vehicle : vehicles) {
            if (vehicle.getLiquidQuantity() <= 0) {
                FacilityDto nearestFacility = findNearestFacility(vehicle, facilities);
                Coord nearestFacilityCoord = new Coord();
                nearestFacilityCoord.setLat(nearestFacility.getLat());
                nearestFacilityCoord.setLon(nearestFacility.getLon());

                // Get route to nearest facility and divide it into segments
                Coord vehicleCoord = new Coord();
                vehicleCoord.setLat(vehicle.getLat());
                vehicleCoord.setLon(vehicle.getLon());
                List<Coord> routeToFacility = mapboxService.getRoute(vehicleCoord, nearestFacilityCoord);
                List<Coord> segmentsToFacility = divideRouteIntoSegments(routeToFacility, 10);

                // Move vehicle along segments to facility
                int delay = 0;
                for (Coord segment : segmentsToFacility) {
                    executorService.schedule(() -> emergencyService.moveVehicle(vehicle.getId(), segment), delay * 100,
                            TimeUnit.MILLISECONDS);
                    delay += 1;
                }
                // Allow some delay for refilling at the facility
                executorService.schedule(() -> refillLiquid(vehicle, unhandledFires), delay * 100,
                        TimeUnit.MILLISECONDS);
                continue;
            }

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

            Coord vehicleCoord = new Coord();
            vehicleCoord.setLat(vehicle.getLat());
            vehicleCoord.setLon(vehicle.getLon());
            List<Coord> route = mapboxService.getRoute(vehicleCoord, bestFire.getCoord());
            List<Coord> segments = divideRouteIntoSegments(route, 10);

            int delay = 0;
            for (Coord segment : segments) {
                executorService.schedule(() -> emergencyService.moveVehicle(vehicle.getId(), segment), delay * 100,
                        TimeUnit.MILLISECONDS);
                delay += 1;
            }

            unhandledFires.remove(bestFire);
            System.out.println("Vehicle " + vehicle.getId() + " is handling fire " + bestFire.getId());
        }
    }

    private List<Coord> divideRouteIntoSegments(List<Coord> route, int segmentLength) {
        List<Coord> segments = new ArrayList<>();
        double remainingLength = segmentLength;

        for (int i = 0; i < route.size() - 1; i++) {
            Coord start = route.get(i);
            Coord end = route.get(i + 1);

            double distance = getDistance(start, end);
            if (distance < remainingLength) {
                remainingLength -= distance;
                continue;
            }

            double latDiff = end.getLat() - start.getLat();
            double lonDiff = end.getLon() - start.getLon();
            while (distance >= remainingLength) {
                double ratio = remainingLength / distance;
                Coord newCoord = new Coord();
                newCoord.setLat(start.getLat() + ratio * latDiff);
                newCoord.setLon(start.getLon() + ratio * lonDiff);
                segments.add(newCoord);

                remainingLength = segmentLength;
                distance -= remainingLength;
                start = newCoord;
            }

            remainingLength = segmentLength - distance;
        }

        // Make sure to add the end of the route as the last segment
        if (!segments.isEmpty() && !segments.get(segments.size() - 1).equals(route.get(route.size() - 1))) {
            segments.add(route.get(route.size() - 1));
        }

        return segments;
    }

    // Refill vehicle with the most needed liquid type
    private void refillLiquid(VehicleDto vehicle, List<FireDto> unhandledFires) {
        LiquidType mostNeededLiquidType = determineMostNeededLiquidType(unhandledFires, vehicle);
        vehicle.setLiquidType(mostNeededLiquidType);
    }

    private double calculateScore(VehicleDto vehicle, FireDto fire) {
        Coord vehicleCoord = new Coord();
        vehicleCoord.setLat(vehicle.getLat());
        vehicleCoord.setLon(vehicle.getLon());

        double distance = mapboxService.getDistance(vehicleCoord, fire.getCoord());
        double fireImportance = computeFireImportance(fire);
        double capacity = vehicle.getLiquidQuantity() * vehicle.getLiquidType().getEfficiency(fire.getType());
        if (distance == 0) {
            return Double.POSITIVE_INFINITY;
        }

        return (fireImportance * capacity) / distance;
    }

    private LiquidType determineMostNeededLiquidType(List<FireDto> fires, VehicleDto vehicle) {
        // We'll keep track of the total efficiency score for each liquid type here
        Map<LiquidType, Double> liquidScores = new HashMap<>();

        for (LiquidType liquidType : LiquidType.values()) {
            double liquidScore = 0;
            for (FireDto fire : fires) {
                // Calculate liquid required for the fire.
                // This can be adjusted based on factors like fire intensity and size.
                double liquidRequired = fire.getIntensity() * fire.getRange();
                double efficiency = liquidType.getEfficiency(fire.getType());
                // Only add to the score if the vehicle can put out the fire with its liquid
                // capacity
                if (liquidRequired <= vehicle.getLiquidQuantity() * efficiency) {
                    liquidScore += efficiency * computeFireImportance(fire);
                }
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

    private double getDistance(Coord coord1, Coord coord2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(coord2.getLat() - coord1.getLat());
        double lonDistance = Math.toRadians(coord2.getLon() - coord1.getLon());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(coord1.getLat())) * Math.cos(Math.toRadians(coord2.getLat()))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // convert to meters
    }

    private double calculateDistance(Coord coord1, Coord coord2) {
        return mapboxService.getDistance(coord1, coord2);
    }
}
