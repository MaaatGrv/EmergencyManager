package com.firefighter.emergency.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.firefighter.emergency.dto.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ser.impl.FilteredBeanPropertyWriter;
import com.firefighter.emergency.dto.Coord;
import com.firefighter.emergency.dto.FacilityDto;
import com.firefighter.emergency.dto.FireDto;
import com.firefighter.emergency.dto.FireType;
import com.firefighter.emergency.dto.LiquidType;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class FireManager {

    private final EmergencyService emergencyService;
    private final MapBoxService mapboxService;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);
    private final ConcurrentHashMap<Integer, Coord> vehiclePositions = new ConcurrentHashMap<>();
    private Map<Integer, Integer> vehicleFireMap = new ConcurrentHashMap<>();
    private Map<Integer, Integer> fireVehicleMap = new ConcurrentHashMap<>();

    public FireManager(EmergencyService emergencyService, MapBoxService mapboxService,
            ScheduledExecutorService executorService) {
        this.emergencyService = emergencyService;
        this.mapboxService = mapboxService;
    }

    @Scheduled(fixedRate = 10000)
    public void handleFires() {
        System.out.println("Vehicle Fire Map: " + vehicleFireMap);
        List<FireDto> fires = emergencyService.getAllFires();
        List<VehicleDto> vehicles = emergencyService.getTeamVehicles();
        List<FacilityDto> facilities = emergencyService.getTeamFacilities();

        for (VehicleDto vehicle : vehicles) {
            executorService.submit(() -> handleVehicle(vehicle, fires, facilities));
            // si le véhicule n'est associé à aucun feu
            if (!vehicleFireMap.containsKey(vehicle.getId())) {
                // trouver le feu le plus approprié et l'associer au véhicule
                handleFire(vehicle, fires);
            }
        }
    }

    void handleVehicle(VehicleDto vehicle, List<FireDto> fires, List<FacilityDto> facilities) {
        List<FireDto> unhandledFires = Collections.synchronizedList(new ArrayList<>(fires));

        if (vehicle.getLiquidQuantity() == 0) {
            FacilityDto nearestFacility = findNearestFacility(vehicle, facilities);
            Coord facilityCoord = nearestFacility.getCoord();
            vehiclePositions.put(vehicle.getId(), facilityCoord);
            emergencyService.moveVehicleReal(vehicle.getId(), facilityCoord);
            refillVehicle(vehicle, unhandledFires);
            handleFire(vehicle, unhandledFires);
        } else {
            if (!isFireHere(vehicle, unhandledFires)) {
                if (vehicleFireMap.containsKey(vehicle.getId())) {
                    vehicleFireMap.remove(vehicle.getId());
                }
                handleFire(vehicle, unhandledFires);
            }

            for (FacilityDto facility : facilities) {
                double epsilon = 0.00001;
                if (Math.abs(vehicle.getCoord().getLat() - facility.getLat()) < epsilon
                        && Math.abs(vehicle.getCoord().getLon() - facility.getLon()) < epsilon) {
                    if (vehicle.getLiquidQuantity() < vehicle.getType().getLiquidCapacity() - 2) {
                        refillVehicle(vehicle, unhandledFires);
                    }
                    handleFire(vehicle, unhandledFires);
                }
            }
        }
    }

    private void handleFire(VehicleDto vehicle, List<FireDto> unhandledFires) {
        FireDto bestFire;
        Coord bestFireCoord = null;
        do {
            bestFire = FindBestFire(vehicle, unhandledFires);
            if (bestFire != null) {
                bestFireCoord = bestFire.getCoord();
                unhandledFires.remove(bestFire);
            }
        } while (bestFire != null && vehiclePositions.containsValue(bestFireCoord));

        if (bestFire != null) {
            vehicleFireMap.put(vehicle.getId(), bestFire.getId());
            fireVehicleMap.put(bestFire.getId(), vehicle.getId());
            vehiclePositions.put(vehicle.getId(), bestFire.getCoord());
            emergencyService.moveVehicleReal(vehicle.getId(), bestFire.getCoord());
        }
    }

    private boolean isFireHere(VehicleDto vehicle, List<FireDto> unhandledFires) {
        Integer fireId = vehicleFireMap.get(vehicle.getId());
        if (fireId == null) {
            return false;
        }
        for (FireDto fire : unhandledFires) {
            if (fire.getId().equals(fireId)) {
                return true;
            }
        }
        fireVehicleMap.remove(vehicleFireMap.get(vehicle.getId()));
        return false;
    }

    private FireDto FindBestFire(VehicleDto vehicle, List<FireDto> unhandledFires) {
        FireDto bestFire = null;
        FireDto bestFire2 = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        double bestScore2 = Double.NEGATIVE_INFINITY;
        LiquidType liquidMostNeeded = determineMostNeededLiquidType(unhandledFires, vehicle);
        Map<FireType, LiquidType> mapOfBestLiquid = findTheBestTypeofLiquidForAllTypeOfFire();
        List<FireType> bestFiretoHandle = new ArrayList<>();
        for (LiquidType liquidType : mapOfBestLiquid.values()) {
            if (liquidType == liquidMostNeeded) {
                addBestFireToMap(mapOfBestLiquid, bestFiretoHandle, liquidType);
            }
        }
        for (FireDto fire : unhandledFires) {
            if (fireVehicleMap.containsKey(fire.getId())) {
                continue;
            }
            double score = calculateScore(vehicle, fire);
            if (score > bestScore2) {
                bestScore2 = score;
                bestFire2 = fire;
            }
            for (FireType fireType : bestFiretoHandle) {
                if (fire.getType().equals(fireType)) {
                    if (score > bestScore) {
                        bestScore = score;
                        bestFire = fire;
                    }
                }
            }
        }
        if (bestFire == null) {
            bestFire = bestFire2;
        }
        if (bestFire == null) {
            bestFire = bestFire2;
        }
        return bestFire;
    }

    private void addBestFireToMap(Map<FireType, LiquidType> bestLiquidForFire, List<FireType> bestFireToHandle,
            LiquidType liquidType) {
        for (Map.Entry<FireType, LiquidType> entry : bestLiquidForFire.entrySet()) {
            if (Objects.equals(liquidType, entry.getValue())) {
                bestFireToHandle.add(entry.getKey());
            }
        }
    }

    private Map<FireType, LiquidType> findTheBestTypeofLiquidForAllTypeOfFire() {
        Map<FireType, LiquidType> theMostEfficiencyLiquidForFireTypes = new HashMap<>();
        for (FireType fireType : FireType.values()) {
            LiquidType bestLiquid = null;
            float bestTest = 0;
            for (LiquidType liquid : LiquidType.values()) {
                float test = liquid.getEfficiency(fireType.name());
                if (test > bestTest) {
                    bestTest = test;
                    bestLiquid = liquid;
                }
            }
            theMostEfficiencyLiquidForFireTypes.put(fireType, bestLiquid);
        }
        return theMostEfficiencyLiquidForFireTypes;
    }

    private void refillVehicle(VehicleDto vehicle, List<FireDto> unhandledFires) {
        Future<?> future = executorService.scheduleAtFixedRate(() -> {
            VehicleDto updatedVehicle = emergencyService.getVehicleById(vehicle.getId());
            if (updatedVehicle.getLiquidQuantity() >= updatedVehicle.getType().getLiquidCapacity()) {
                LiquidType mostNeededLiquidType = determineMostNeededLiquidType(unhandledFires, vehicle);
                updatedVehicle.setLiquidType(mostNeededLiquidType);
                throw new RuntimeException("Vehicle refilled");
            }

        }, 0, 6, TimeUnit.SECONDS);

        while (true) {
            try {
                future.get();
                break;
            } catch (CancellationException e) {
                break;
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
        double efficiency = 0;
        switch (fire.getType()) {
            case "B_Alcohol":
            case "B_Plastics":
            case "B_Gasoline":
                efficiency = 1.5;
                break;
            case "E_Electrical":
                efficiency = 0.75;
                break;
            default:
                efficiency = 1;
                break;
        }
        return efficiency;
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

    public void updateVehicles() {
        List<VehicleDto> vehicles = emergencyService.getTeamVehicles();
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

    private FireDto findFireById(String id, List<FireDto> unhandledFires) {
        for (FireDto fire : unhandledFires) {
            if (fire.getId().equals(id)) {
                return fire;
            }
        }
        return null; // No fire with such id found
    }
}