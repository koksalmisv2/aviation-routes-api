package com.aviation.controller;

import com.aviation.dto.TransportationDTO;
import com.aviation.service.TransportationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transportations")
@Tag(name = "Transportations", description = "Transportation management APIs")
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor
public class TransportationController {
    
    private final TransportationService transportationService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all transportations", description = "Retrieve all transportations (Admin only)")
    public ResponseEntity<List<TransportationDTO>> getAllTransportations() {
        return ResponseEntity.ok(transportationService.getAllTransportations());
    }

    @GetMapping("/paged")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get transportations with pagination", description = "Retrieve transportations with pagination support (Admin only)")
    public ResponseEntity<Page<TransportationDTO>> getTransportationsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(transportationService.getAllTransportations(pageable));
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get transportation by ID", description = "Retrieve a specific transportation (Admin only)")
    public ResponseEntity<TransportationDTO> getTransportationById(@PathVariable Long id) {
        return ResponseEntity.ok(transportationService.getTransportationById(id));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create transportation", description = "Create a new transportation (Admin only)")
    public ResponseEntity<TransportationDTO> createTransportation(@Valid @RequestBody TransportationDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transportationService.createTransportation(dto));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update transportation", description = "Update an existing transportation (Admin only)")
    public ResponseEntity<TransportationDTO> updateTransportation(
            @PathVariable Long id,
            @Valid @RequestBody TransportationDTO dto) {
        return ResponseEntity.ok(transportationService.updateTransportation(id, dto));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete transportation", description = "Delete a transportation (Admin only)")
    public ResponseEntity<Void> deleteTransportation(@PathVariable Long id) {
        transportationService.deleteTransportation(id);
        return ResponseEntity.noContent().build();
    }
}
