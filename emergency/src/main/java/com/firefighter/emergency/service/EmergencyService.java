package com.firefighter.emergency.service;

import com.firefighter.emergency.client.EmergencyClient;
import com.firefighter.emergency.dto.Compteur;
import com.firefighter.emergency.dto.Coord;
import com.firefighter.emergency.dto.FacilityDto;
import com.firefighter.emergency.dto.FireDto;
import com.firefighter.emergency.dto.LiquidType;
import com.firefighter.emergency.dto.VehicleDto;

import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
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
    private double speed = 100;

    public EmergencyService(EmergencyClient emergencyClient) {
        this.emergencyClient = emergencyClient;
    }

    public List<FireDto> getAllFires() {
        return emergencyClient.getAllFires();
    }

    public List<VehicleDto> getTeamVehicles() {
        return emergencyClient.getTeamVehicles();
    }

    public VehicleDto moveVehicle(Integer vehicleId, Coord coord) {
        return emergencyClient.moveVehicle(vehicleId, coord);
    }

    public void moveVehicleUniformly(Integer vehicleId, Coord targetCoord) {
        System.out.println("vehcle id : " + vehicleId + "target coord : " + targetCoord);
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

    public void moveVehicleReal(Integer vehicleId, Coord targetCoord) {
        // Get the current position of the vehicle
        VehicleDto vehicle = getVehicleById(vehicleId);

        Coord currentCoord = new Coord();
        currentCoord.setLat(vehicle.getLat());
        currentCoord.setLon(vehicle.getLon());

        // Calculate the way
        List<Coord> chemin = mapboxService.getRoute(currentCoord, targetCoord);
        chemin.add(targetCoord);

        // Calculate all step
        double distancestep = (vehicle.getType().getMaxSpeed()) * 10000 / 36000;

        List<Coord> etapes = divideRouteIntoSegments2(chemin, distancestep);
        int taille = etapes.size();
        Compteur i = new Compteur(0);
        executorService.scheduleAtFixedRate(() -> {
            // Update the current position

            int indice = i.getValeur();
            // Move the vehicle to the new position
            emergencyClient.moveVehicle(vehicleId, etapes.get(indice));

            indice += 1;
            i.setValeur(indice);

            // Check if the vehicle has reached its destination
            if (indice > taille) {
                // If the vehicle has reached its destination, cancel the task
                throw new RuntimeException("Destination reached");
            }
        }, 0, MOVE_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    public List<Coord> divideRouteIntoSegments2(List<Coord> route, double distbystep) {

        List<Coord> segments = new ArrayList<>();

        for (int i = 0; i < route.size() - 1; i++) {
            Coord start = route.get(i);
            Coord end = route.get(i + 1);

            double remainingLength = mapboxService.getDistance(start, end);

            int steps = (int) Math.floor(remainingLength / distbystep);

            double deltalat = (end.getLat() - start.getLat()) / steps;
            double deltalon = (end.getLon() - start.getLon()) / steps;

            for (int i1 = 0; i1 <= steps; i1++) {

                Coord newCoord = new Coord();
                newCoord.setLat(start.getLat() + deltalat);
                newCoord.setLon(start.getLon() + deltalon);
                segments.add(newCoord);
                start = newCoord;
            }
            segments.add(end);
        }
        return segments;
    }

    public List<FacilityDto> getTeamFacilities() {
        List<FacilityDto> facilities = emergencyClient.getAllFacilities();
        // Get facilities with the id 35 or 3918
        facilities.removeIf(facility -> facility.getId() != 35 && facility.getId() != 3918);
        return facilities;
    }

    public List<FacilityDto> getAllFacilities() {
        return emergencyClient.getAllFacilities();
    }

    public List<VehicleDto> getAllVehicles() {
        List<VehicleDto> vehicles = emergencyClient.getAllVehicles();
        return vehicles;
    }

    public void updateVehicleLiquidType(Integer id, LiquidType bestLiquid) {
        emergencyClient.updateVehicleLiquidType(id, bestLiquid);
    }

    public VehicleDto getVehicleById(Integer id) {
        return emergencyClient.getVehicleById(id);
    }

    @PostConstruct
    public void initializeVehiclesInFacilities() {
        List<VehicleDto> vehicles = getTeamVehicles();
        List<FacilityDto> facilities = getTeamFacilities();

        if (facilities.isEmpty()) {
            throw new IllegalStateException("No facilities available for initialization");
        }

        for (int i = 0; i < vehicles.size(); i++) {
            VehicleDto vehicle = vehicles.get(i);
            // Get the facility :
            FacilityDto facility = getFacilityById(vehicle.getFacilityRefID());
            // Move vehicle to the facility location
            moveVehicle(vehicle.getId(), facility.getCoord());
        }
    }

    public FacilityDto getFacilityById(Integer id) {
        return emergencyClient.getFacilityById(id);
    }

    public void deleteVehicle(Integer id) {
        emergencyClient.deleteVehicle(id);
    }

    public VehicleDto createVehicle(VehicleDto vehicleDto) {
        return emergencyClient.createVehicle(vehicleDto);
    }

}
