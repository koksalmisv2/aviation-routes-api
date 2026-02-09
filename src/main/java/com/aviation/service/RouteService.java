package com.aviation.service;

import com.aviation.dto.LocationDTO;
import com.aviation.dto.RouteDTO;
import com.aviation.entity.Location;
import com.aviation.entity.SegmentType;
import com.aviation.entity.Transportation;
import com.aviation.entity.TransportationType;
import com.aviation.repository.LocationRepository;
import com.aviation.repository.TransportationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteService {
    
    private final TransportationRepository transportationRepository;
    private final LocationRepository locationRepository;
    
    /**
     * Finds all valid routes between two locations on a specific date.
     *
     * <p>Optimizations applied:
     * <ul>
     *   <li>Single DB query instead of 3 separate queries</li>
     *   <li>HashMap-based indexing for O(1) lookups instead of nested linear scans</li>
     *   <li>In-memory partitioning of flights vs ground transport</li>
     * </ul>
     *
     * <p>Supports 4 route patterns:
     * <ol>
     *   <li>Direct flight (Origin → Destination)</li>
     *   <li>Ground transfer + Flight (Origin → Airport → Destination)</li>
     *   <li>Flight + Ground transfer (Origin → Airport → Destination)</li>
     *   <li>Ground + Flight + Ground (Origin → Airport1 → Airport2 → Destination)</li>
     * </ol>
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "routes", key = "'route:' + #originId + ':' + #destinationId + ':' + #date")
    public List<RouteDTO> findRoutes(Long originId, Long destinationId, LocalDate date) {
        locationRepository.findById(originId)
                .orElseThrow(() -> new RuntimeException("Origin location not found"));
        
        locationRepository.findById(destinationId)
                .orElseThrow(() -> new RuntimeException("Destination location not found"));
        
        var dayOfWeek = date.getDayOfWeek().getValue(); // 1 = Monday, 7 = Sunday
        
        // Single DB round-trip: fetch all relevant transportations for this route search
        var allRelevant = transportationRepository.findAllRelevantForRoute(originId, destinationId, dayOfWeek);
        
        // Partition into flights and ground transport using in-memory classification
        var flights = new ArrayList<Transportation>();
        var nonFlightsFromOrigin = new ArrayList<Transportation>();
        var nonFlightsToDestination = new ArrayList<Transportation>();
        
        for (var transport : allRelevant) {
            if (transport.getTransportationType() == TransportationType.FLIGHT) {
                flights.add(transport);
            } else {
                if (transport.getOriginLocation().getId().equals(originId)) {
                    nonFlightsFromOrigin.add(transport);
                }
                if (transport.getDestinationLocation().getId().equals(destinationId)) {
                    nonFlightsToDestination.add(transport);
                }
            }
        }
        
        // Build HashMap indexes for O(1) lookups (replaces nested linear iteration)
        Map<Long, List<Transportation>> flightsByOrigin = flights.stream()
                .collect(Collectors.groupingBy(t -> t.getOriginLocation().getId()));
        
        Map<Long, List<Transportation>> afterTransfersByAirport = nonFlightsToDestination.stream()
                .collect(Collectors.groupingBy(t -> t.getOriginLocation().getId()));
        
        var allRoutes = new ArrayList<RouteDTO>();
        
        // Pattern 1: Direct flight (Origin -> Destination)
        for (var flight : flightsByOrigin.getOrDefault(originId, List.of())) {
            if (flight.getDestinationLocation().getId().equals(destinationId)) {
                var route = new RouteDTO();
                route.setSegments(List.of(createSegment(flight, SegmentType.FLIGHT)));
                allRoutes.add(route);
            }
        }
        
        // Pattern 2: Before flight transfer + Flight (Origin -> Airport -> Destination)
        for (var transfer : nonFlightsFromOrigin) {
            var airportId = transfer.getDestinationLocation().getId();
            
            for (var flight : flightsByOrigin.getOrDefault(airportId, List.of())) {
                if (flight.getDestinationLocation().getId().equals(destinationId)) {
                    var route = new RouteDTO();
                    route.setSegments(List.of(
                            createSegment(transfer, SegmentType.BEFORE_FLIGHT),
                            createSegment(flight, SegmentType.FLIGHT)
                    ));
                    allRoutes.add(route);
                }
            }
        }
        
        // Pattern 3: Flight + After flight transfer (Origin -> Airport -> Destination)
        for (var flight : flightsByOrigin.getOrDefault(originId, List.of())) {
            var airportId = flight.getDestinationLocation().getId();
            
            for (var transfer : afterTransfersByAirport.getOrDefault(airportId, List.of())) {
                var route = new RouteDTO();
                route.setSegments(List.of(
                        createSegment(flight, SegmentType.FLIGHT),
                        createSegment(transfer, SegmentType.AFTER_FLIGHT)
                ));
                allRoutes.add(route);
            }
        }
        
        // Pattern 4: Before + Flight + After (Origin -> Airport1 -> Airport2 -> Destination)
        for (var beforeTransfer : nonFlightsFromOrigin) {
            var airport1Id = beforeTransfer.getDestinationLocation().getId();
            
            for (var flight : flightsByOrigin.getOrDefault(airport1Id, List.of())) {
                var airport2Id = flight.getDestinationLocation().getId();
                
                for (var afterTransfer : afterTransfersByAirport.getOrDefault(airport2Id, List.of())) {
                    var route = new RouteDTO();
                    route.setSegments(List.of(
                            createSegment(beforeTransfer, SegmentType.BEFORE_FLIGHT),
                            createSegment(flight, SegmentType.FLIGHT),
                            createSegment(afterTransfer, SegmentType.AFTER_FLIGHT)
                    ));
                    allRoutes.add(route);
                }
            }
        }
        
        return allRoutes;
    }
    
    private RouteDTO.TransportationSegmentDTO createSegment(Transportation transportation, SegmentType segmentType) {
        return new RouteDTO.TransportationSegmentDTO(
                transportation.getId(),
                transportation.getTransportationType(),
                LocationDTO.from(transportation.getOriginLocation()),
                LocationDTO.from(transportation.getDestinationLocation()),
                segmentType
        );
    }
}
