package com.firefighter.emergency.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import com.firefighter.emergency.dto.*;

public class FireManagerTest {

    @InjectMocks
    private FireManager fireManager;

    @Mock
    private EmergencyService emergencyService;

    @Mock
    private MapBoxService mapboxService;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void handleFiresTest() {
        // Créer des données factices
        List<FireDto> fires = Arrays.asList(new FireDto());
        List<VehicleDto> vehicles = Arrays.asList(new VehicleDto());
        List<FacilityDto> facilities = Arrays.asList(new FacilityDto());

        // Mock les méthodes du service d'urgence
        when(emergencyService.getAllFires()).thenReturn(fires);
        when(emergencyService.getTeamVehicles()).thenReturn(vehicles);
        when(emergencyService.getTeamFacilities()).thenReturn(facilities);

        // Appeler la méthode à tester
        fireManager.handleFires();

        // Vérifiez que les méthodes attendues ont été appelées
        verify(emergencyService, times(1)).getAllFires();
        verify(emergencyService, times(1)).getTeamVehicles();
        verify(emergencyService, times(1)).getTeamFacilities();
    }
}
