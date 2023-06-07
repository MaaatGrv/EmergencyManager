package com.firefighter.emergency.client;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import com.firefighter.emergency.dto.Coord;
import com.firefighter.emergency.dto.FacilityDto;
import com.firefighter.emergency.dto.FireDto;
import com.firefighter.emergency.dto.LiquidType;
import com.firefighter.emergency.dto.VehicleDto;

@Repository
public class EmergencyClient {

    private final RestTemplate restTemplate;
    private static final String API_URL = "http://vps.cpe-sn.fr:8081/";
    private String TeamKey = "f673d1cb-926d-4e80-a4f9-4ba321a48e44";

    public EmergencyClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<FireDto> getAllFires() {
        ResponseEntity<List<FireDto>> response = restTemplate.exchange(
                API_URL + "/fires",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<FireDto>>() {
                });
        return response.getBody();
    }

    public VehicleDto moveVehicle(Integer vehicleId, Coord coord) {
        HttpEntity<Coord> request = new HttpEntity<>(coord);
        ResponseEntity<VehicleDto> response = restTemplate.exchange(
                API_URL + "/vehicle/move/" + TeamKey + "/" + vehicleId,
                HttpMethod.PUT,
                request,
                new ParameterizedTypeReference<VehicleDto>() {
                });
        return response.getBody();
    }

    public List<VehicleDto> getTeamVehicles() {
        ResponseEntity<List<VehicleDto>> response = restTemplate.exchange(
                API_URL + "/vehiclebyteam/" + TeamKey,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<VehicleDto>>() {
                });
        return response.getBody();
    }

    public List<VehicleDto> getAllVehicles() {
        ResponseEntity<List<VehicleDto>> response = restTemplate.exchange(
                API_URL + "/vehicles",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<VehicleDto>>() {
                });
        return response.getBody();
    }

    public List<FacilityDto> getAllFacilities() {
        ResponseEntity<List<FacilityDto>> response = restTemplate.exchange(
                API_URL + "/facility",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<FacilityDto>>() {
                });
        return response.getBody();
    }

    public VehicleDto updateVehicleLiquidType(Integer vehicleId, LiquidType liquidType) {
        VehicleDto vehicleDto = this.getVehicleById(vehicleId);
        if (vehicleDto != null) {
            FacilityDto facilityDto = this.getFacilityById(vehicleDto.getFacilityRefID());
            if (facilityDto != null && isVehicleInFacility(vehicleDto, facilityDto)) {
                vehicleDto.setLiquidType(liquidType);
                HttpEntity<VehicleDto> request = new HttpEntity<>(vehicleDto);
                ResponseEntity<VehicleDto> response = restTemplate.exchange(
                        API_URL + "/vehicle/" + TeamKey + "/" + vehicleId,
                        HttpMethod.PUT,
                        request,
                        new ParameterizedTypeReference<VehicleDto>() {
                        });
                return response.getBody();
            }
        }
        return null;
    }

    public FacilityDto getFacilityById(Integer facilityId) {
        List<FacilityDto> facilities = this.getAllFacilities();
        for (FacilityDto facility : facilities) {
            if (facility.getId().equals(facilityId)) {
                return facility;
            }
        }
        return null;
    }

    private boolean isVehicleInFacility(VehicleDto vehicle, FacilityDto facility) {
        return facility.getVehicleIdSet().contains(vehicle.getId()) &&
                vehicle.getLon() == facility.getLon() &&
                vehicle.getLat() == facility.getLat();
    }

    public VehicleDto getVehicleById(Integer vehicleId) {
        List<VehicleDto> vehicles = this.getAllVehicles();
        for (VehicleDto vehicle : vehicles) {
            if (vehicle.getId().equals(vehicleId)) {
                return vehicle;
            }
        }
        return null;
    }

}
