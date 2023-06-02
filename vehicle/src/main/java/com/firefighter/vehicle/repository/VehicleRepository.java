package com.firefighter.vehicle.repository;

import com.firefighter.vehicle.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {
}
