package com.firefighter.emergency.dto;

public class VehicleStatus {
    private VehicleDto vehicle;
    private FireDto currentFire;

    public VehicleStatus(VehicleDto vehicle, FireDto currentFire) {
        this.vehicle = vehicle;
        this.currentFire = currentFire;
    }

    // Getters and setters

    public VehicleDto getVehicle() {
        return this.vehicle;
    }

    public void setVehicle(VehicleDto vehicle) {
        this.vehicle = vehicle;
    }

    public FireDto getCurrentFire() {
        return this.currentFire;
    }

    public void setCurrentFire(FireDto currentFire) {
        this.currentFire = currentFire;
    }

}
