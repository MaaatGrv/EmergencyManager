package com.firefighter.emergency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.firefighter.emergency.service.EmergencyService;

@SpringBootApplication
@EnableScheduling
public class EmergencyApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(EmergencyApplication.class, args);

		// Get the EmergencyService bean
		EmergencyService emergencyService = context.getBean(EmergencyService.class);

		// Initialize the vehicles in their facilities
		emergencyService.initializeVehiclesInFacilities();
	}
}
