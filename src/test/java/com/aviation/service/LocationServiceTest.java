package com.aviation.service;

import com.aviation.dto.LocationDTO;
import com.aviation.entity.Location;
import com.aviation.repository.LocationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {
    
    @Mock
    private LocationRepository locationRepository;
    
    @InjectMocks
    private LocationService locationService;
    
    @Test
    void testGetAllLocations() {
        // Given
        Location location1 = new Location(1L, "Istanbul Airport", "Turkey", "Istanbul", "IST");
        Location location2 = new Location(2L, "London Heathrow", "UK", "London", "LHR");
        when(locationRepository.findAll()).thenReturn(Arrays.asList(location1, location2));
        
        // When
        List<LocationDTO> locations = locationService.getAllLocations();
        
        // Then
        assertEquals(2, locations.size());
        verify(locationRepository, times(1)).findAll();
    }
    
    @Test
    void testGetLocationById() {
        // Given
        Location location = new Location(1L, "Istanbul Airport", "Turkey", "Istanbul", "IST");
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        
        // When
        LocationDTO result = locationService.getLocationById(1L);
        
        // Then
        assertNotNull(result);
        assertEquals("Istanbul Airport", result.getName());
        assertEquals("IST", result.getLocationCode());
    }
    
    @Test
    void testCreateLocation() {
        // Given
        LocationDTO dto = new LocationDTO(null, "Istanbul Airport", "Turkey", "Istanbul", "IST");
        Location location = new Location(1L, "Istanbul Airport", "Turkey", "Istanbul", "IST");
        
        when(locationRepository.existsByLocationCode("IST")).thenReturn(false);
        when(locationRepository.save(any(Location.class))).thenReturn(location);
        
        // When
        LocationDTO result = locationService.createLocation(dto);
        
        // Then
        assertNotNull(result);
        assertEquals("Istanbul Airport", result.getName());
        verify(locationRepository, times(1)).save(any(Location.class));
    }
    
    @Test
    void testCreateLocationWithDuplicateCode() {
        // Given
        LocationDTO dto = new LocationDTO(null, "Istanbul Airport", "Turkey", "Istanbul", "IST");
        when(locationRepository.existsByLocationCode("IST")).thenReturn(true);
        
        // When & Then
        assertThrows(RuntimeException.class, () -> locationService.createLocation(dto));
    }
    
    @Test
    void testDeleteLocation() {
        // Given
        when(locationRepository.existsById(1L)).thenReturn(true);
        
        // When
        locationService.deleteLocation(1L);
        
        // Then
        verify(locationRepository, times(1)).deleteById(1L);
    }
}
