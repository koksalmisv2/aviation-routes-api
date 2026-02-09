package com.aviation.service;

import com.aviation.dto.LocationDTO;
import com.aviation.dto.TransportationDTO;
import com.aviation.entity.Location;
import com.aviation.entity.Transportation;
import com.aviation.repository.LocationRepository;
import com.aviation.repository.TransportationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransportationService {
    
    private final TransportationRepository transportationRepository;
    private final LocationRepository locationRepository;
    
    @Transactional(readOnly = true)
    @Cacheable(value = "transportations", key = "'all'")
    public List<TransportationDTO> getAllTransportations() {
        return transportationRepository.findAllWithOperatingDays().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TransportationDTO> getAllTransportations(Pageable pageable) {
        return transportationRepository.findAll(pageable)
                .map(this::convertToDTO);
    }
    
    @Transactional(readOnly = true)
    @Cacheable(value = "transportations", key = "#id")
    public TransportationDTO getTransportationById(Long id) {
        Transportation transportation = transportationRepository.findByIdWithOperatingDays(id)
                .orElseThrow(() -> new RuntimeException("Transportation not found with id: " + id));
        return convertToDTO(transportation);
    }
    
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "routes", allEntries = true),
            @CacheEvict(value = "transportations", allEntries = true)
    })
    public TransportationDTO createTransportation(TransportationDTO dto) {
        validateOriginNotEqualToDestination(dto);
        validateOperatingDays(dto.getOperatingDays());

        Transportation transportation = convertToEntity(dto);
        Transportation savedTransportation = transportationRepository.save(transportation);
        return convertToDTO(savedTransportation);
    }
    
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "routes", allEntries = true),
            @CacheEvict(value = "transportations", allEntries = true)
    })
    public TransportationDTO updateTransportation(Long id, TransportationDTO dto) {
        validateOriginNotEqualToDestination(dto);
        validateOperatingDays(dto.getOperatingDays());

        Transportation transportation = transportationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transportation not found with id: " + id));
        
        Location originLocation = locationRepository.findById(dto.getOriginLocationId())
                .orElseThrow(() -> new RuntimeException("Origin location not found"));
        
        Location destinationLocation = locationRepository.findById(dto.getDestinationLocationId())
                .orElseThrow(() -> new RuntimeException("Destination location not found"));
        
        transportation.setOriginLocation(originLocation);
        transportation.setDestinationLocation(destinationLocation);
        transportation.setTransportationType(dto.getTransportationType());
        transportation.setOperatingDays(dto.getOperatingDays());
        
        Transportation updatedTransportation = transportationRepository.save(transportation);
        return convertToDTO(updatedTransportation);
    }
    
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "routes", allEntries = true),
            @CacheEvict(value = "transportations", allEntries = true)
    })
    public void deleteTransportation(Long id) {
        if (!transportationRepository.existsById(id)) {
            throw new RuntimeException("Transportation not found with id: " + id);
        }
        transportationRepository.deleteById(id);
    }

    private void validateOriginNotEqualToDestination(TransportationDTO dto) {
        if (dto.getOriginLocationId().equals(dto.getDestinationLocationId())) {
            throw new RuntimeException("Origin and destination locations must be different");
        }
    }

    private void validateOperatingDays(List<Integer> operatingDays) {
        if (operatingDays != null) {
            boolean allValid = operatingDays.stream().allMatch(day -> day >= 1 && day <= 7);
            if (!allValid) {
                throw new RuntimeException("Operating days must be between 1 (Monday) and 7 (Sunday)");
            }
        }
    }
    
    private TransportationDTO convertToDTO(Transportation transportation) {
        TransportationDTO dto = new TransportationDTO();
        dto.setId(transportation.getId());
        dto.setOriginLocationId(transportation.getOriginLocation().getId());
        dto.setDestinationLocationId(transportation.getDestinationLocation().getId());
        dto.setTransportationType(transportation.getTransportationType());
        dto.setOperatingDays(transportation.getOperatingDays() != null
                ? new ArrayList<>(transportation.getOperatingDays()) : null);
        
        dto.setOriginLocation(LocationDTO.from(transportation.getOriginLocation()));
        dto.setDestinationLocation(LocationDTO.from(transportation.getDestinationLocation()));
        
        return dto;
    }
    
    private Transportation convertToEntity(TransportationDTO dto) {
        Transportation transportation = new Transportation();
        
        Location originLocation = locationRepository.findById(dto.getOriginLocationId())
                .orElseThrow(() -> new RuntimeException("Origin location not found"));
        
        Location destinationLocation = locationRepository.findById(dto.getDestinationLocationId())
                .orElseThrow(() -> new RuntimeException("Destination location not found"));
        
        transportation.setOriginLocation(originLocation);
        transportation.setDestinationLocation(destinationLocation);
        transportation.setTransportationType(dto.getTransportationType());
        transportation.setOperatingDays(dto.getOperatingDays());
        
        return transportation;
    }
}
