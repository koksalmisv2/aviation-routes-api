package com.aviation.service;

import com.aviation.dto.LocationDTO;
import com.aviation.entity.Location;
import com.aviation.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationService {
    
    private final LocationRepository locationRepository;
    
    @Transactional(readOnly = true)
    @Cacheable(value = "locations", key = "'all'")
    public List<LocationDTO> getAllLocations() {
        return locationRepository.findAll().stream()
                .map(LocationDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<LocationDTO> getAllLocations(Pageable pageable) {
        return locationRepository.findAll(pageable)
                .map(LocationDTO::from);
    }
    
    @Transactional(readOnly = true)
    @Cacheable(value = "locations", key = "#id")
    public LocationDTO getLocationById(Long id) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + id));
        return LocationDTO.from(location);
    }
    
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "routes", allEntries = true),
            @CacheEvict(value = "locations", allEntries = true)
    })
    public LocationDTO createLocation(LocationDTO locationDTO) {
        if (locationRepository.existsByLocationCode(locationDTO.getLocationCode())) {
            throw new RuntimeException("Location with code " + locationDTO.getLocationCode() + " already exists");
        }
        
        Location location = Location.toEntity(locationDTO);
        Location savedLocation = locationRepository.save(location);
        return LocationDTO.from(savedLocation);
    }
    
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "routes", allEntries = true),
            @CacheEvict(value = "locations", allEntries = true)
    })
    public LocationDTO updateLocation(Long id, LocationDTO locationDTO) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found with id: " + id));
        
        // Check if location code is being changed and if it already exists
        if (!location.getLocationCode().equals(locationDTO.getLocationCode()) &&
            locationRepository.existsByLocationCode(locationDTO.getLocationCode())) {
            throw new RuntimeException("Location with code " + locationDTO.getLocationCode() + " already exists");
        }
        
        location.setName(locationDTO.getName());
        location.setCountry(locationDTO.getCountry());
        location.setCity(locationDTO.getCity());
        location.setLocationCode(locationDTO.getLocationCode());
        
        Location updatedLocation = locationRepository.save(location);
        return LocationDTO.from(updatedLocation);
    }
    
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "routes", allEntries = true),
            @CacheEvict(value = "locations", allEntries = true)
    })
    public void deleteLocation(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new RuntimeException("Location not found with id: " + id);
        }
        locationRepository.deleteById(id);
    }
}
