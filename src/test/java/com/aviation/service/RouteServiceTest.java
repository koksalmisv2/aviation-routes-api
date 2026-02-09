package com.aviation.service;

import com.aviation.dto.RouteDTO;
import com.aviation.entity.Location;
import com.aviation.entity.SegmentType;
import com.aviation.entity.Transportation;
import com.aviation.entity.TransportationType;
import com.aviation.repository.LocationRepository;
import com.aviation.repository.TransportationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {
    
    @Mock
    private TransportationRepository transportationRepository;
    
    @Mock
    private LocationRepository locationRepository;
    
    @InjectMocks
    private RouteService routeService;
    
    private Location taksimSquare;
    private Location istanbulAirport;
    private Location londonHeathrow;
    private Location wembleyStadium;
    
    @BeforeEach
    void setUp() {
        taksimSquare = new Location(1L, "Taksim Square", "Turkey", "Istanbul", "CCIST");
        istanbulAirport = new Location(2L, "Istanbul Airport", "Turkey", "Istanbul", "IST");
        londonHeathrow = new Location(3L, "London Heathrow", "UK", "London", "LHR");
        wembleyStadium = new Location(4L, "Wembley Stadium", "UK", "London", "WEMB");
    }
    
    @Test
    void testFindDirectFlightRoute() {
        // Given
        var flight = new Transportation(
                1L, istanbulAirport, londonHeathrow, 
                TransportationType.FLIGHT, Arrays.asList(1, 2, 3, 4, 5, 6, 7)
        );
        
        when(locationRepository.findById(2L)).thenReturn(Optional.of(istanbulAirport));
        when(locationRepository.findById(3L)).thenReturn(Optional.of(londonHeathrow));
        when(transportationRepository.findAllRelevantForRoute(2L, 3L, 1)).thenReturn(List.of(flight));
        
        // When
        var monday = LocalDate.of(2025, 3, 10); // Monday
        List<RouteDTO> routes = routeService.findRoutes(2L, 3L, monday);
        
        // Then
        assertNotNull(routes);
        assertEquals(1, routes.size());
        assertEquals(1, routes.get(0).getSegments().size());
        assertEquals(TransportationType.FLIGHT, routes.get(0).getSegments().get(0).type());
        assertEquals(SegmentType.FLIGHT, routes.get(0).getSegments().get(0).segmentType());
    }
    
    @Test
    void testFindRouteWithBeforeFlightTransfer() {
        // Given
        var bus = new Transportation(
                1L, taksimSquare, istanbulAirport,
                TransportationType.BUS, Arrays.asList(1, 2, 3, 4, 5, 6, 7)
        );
        
        var flight = new Transportation(
                2L, istanbulAirport, londonHeathrow,
                TransportationType.FLIGHT, Arrays.asList(1, 2, 3, 4, 5, 6, 7)
        );
        
        when(locationRepository.findById(1L)).thenReturn(Optional.of(taksimSquare));
        when(locationRepository.findById(3L)).thenReturn(Optional.of(londonHeathrow));
        when(transportationRepository.findAllRelevantForRoute(1L, 3L, 1)).thenReturn(List.of(bus, flight));
        
        // When
        var monday = LocalDate.of(2025, 3, 10);
        List<RouteDTO> routes = routeService.findRoutes(1L, 3L, monday);
        
        // Then
        assertNotNull(routes);
        assertEquals(1, routes.size());
        assertEquals(2, routes.get(0).getSegments().size());
        assertEquals(SegmentType.BEFORE_FLIGHT, routes.get(0).getSegments().get(0).segmentType());
        assertEquals(SegmentType.FLIGHT, routes.get(0).getSegments().get(1).segmentType());
    }
    
    @Test
    void testFindCompleteRoute() {
        // Given
        var bus = new Transportation(
                1L, taksimSquare, istanbulAirport,
                TransportationType.BUS, Arrays.asList(1, 2, 3, 4, 5, 6, 7)
        );
        
        var flight = new Transportation(
                2L, istanbulAirport, londonHeathrow,
                TransportationType.FLIGHT, Arrays.asList(1, 2, 3, 4, 5, 6, 7)
        );
        
        var uber = new Transportation(
                3L, londonHeathrow, wembleyStadium,
                TransportationType.UBER, Arrays.asList(1, 2, 3, 4, 5, 6, 7)
        );
        
        when(locationRepository.findById(1L)).thenReturn(Optional.of(taksimSquare));
        when(locationRepository.findById(4L)).thenReturn(Optional.of(wembleyStadium));
        when(transportationRepository.findAllRelevantForRoute(1L, 4L, 1)).thenReturn(List.of(bus, flight, uber));
        
        // When
        var monday = LocalDate.of(2025, 3, 10);
        List<RouteDTO> routes = routeService.findRoutes(1L, 4L, monday);
        
        // Then
        assertNotNull(routes);
        assertEquals(1, routes.size());
        assertEquals(3, routes.get(0).getSegments().size());
        assertEquals(SegmentType.BEFORE_FLIGHT, routes.get(0).getSegments().get(0).segmentType());
        assertEquals(SegmentType.FLIGHT, routes.get(0).getSegments().get(1).segmentType());
        assertEquals(SegmentType.AFTER_FLIGHT, routes.get(0).getSegments().get(2).segmentType());
    }
    
    @Test
    void testNoRoutesForUnavailableDate() {
        // Given - Tuesday (day 2) but no transportations operate on Tuesday
        when(locationRepository.findById(2L)).thenReturn(Optional.of(istanbulAirport));
        when(locationRepository.findById(3L)).thenReturn(Optional.of(londonHeathrow));
        when(transportationRepository.findAllRelevantForRoute(2L, 3L, 2)).thenReturn(Collections.emptyList());
        
        // When
        var tuesday = LocalDate.of(2025, 3, 11); // Tuesday
        List<RouteDTO> routes = routeService.findRoutes(2L, 3L, tuesday);
        
        // Then
        assertNotNull(routes);
        assertEquals(0, routes.size());
    }
}
