package com.aviation.controller;

import com.aviation.dto.LocationDTO;
import com.aviation.dto.RouteDTO;
import com.aviation.service.LocationService;
import com.aviation.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/routes")
@Tag(name = "Routes", description = "Route search APIs")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class RouteController {
    
    private final RouteService routeService;
    private final LocationService locationService;
    
    @GetMapping("/locations")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENCY')")
    @Operation(summary = "Get locations for route search", description = "Get all locations available for route search dropdowns")
    public ResponseEntity<List<LocationDTO>> getLocationsForRouteSearch() {
        List<LocationDTO> locations = locationService.getAllLocations();
        return ResponseEntity.ok(locations);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENCY')")
    @Operation(summary = "Find routes", description = "Find all valid routes between two locations on a specific date")
    public ResponseEntity<List<RouteDTO>> findRoutes(
            @RequestParam Long originId,
            @RequestParam Long destinationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<RouteDTO> routes = routeService.findRoutes(originId, destinationId, date);
        return ResponseEntity.ok(routes);
    }
}
