package com.firefighter.vehicle.model;

import javax.persistence.*;

public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private Integer crewMember;
    private Integer facilityRefID;
    private Integer fuel;
    private Double lat;
    private Integer liquidQuantity;
    private String liquidType;
    private Double lon;
    private String type;

    // Getters and setters

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCrewMember() {
        return this.crewMember;
    }

    public void setCrewMember(Integer crewMember) {
        this.crewMember = crewMember;
    }

    public Integer getFacilityRefID() {
        return this.facilityRefID;
    }

    public void setFacilityRefID(Integer facilityRefID) {
        this.facilityRefID = facilityRefID;
    }

    public Integer getFuel() {
        return this.fuel;
    }

    public void setFuel(Integer fuel) {
        this.fuel = fuel;
    }

    public Double getLat() {
        return this.lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Integer getLiquidQuantity() {
        return this.liquidQuantity;
    }

    public void setLiquidQuantity(Integer liquidQuantity) {
        this.liquidQuantity = liquidQuantity;
    }

    public String getLiquidType() {
        return this.liquidType;
    }

    public void setLiquidType(String liquidType) {
        this.liquidType = liquidType;
    }

    public Double getLon() {
        return this.lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
