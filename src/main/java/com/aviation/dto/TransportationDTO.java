package com.aviation.dto;

import com.aviation.entity.TransportationType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransportationDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    
    @NotNull(message = "Origin location ID is required")
    private Long originLocationId;
    
    @NotNull(message = "Destination location ID is required")
    private Long destinationLocationId;
    
    @NotNull(message = "Transportation type is required")
    private TransportationType transportationType;
    
    @NotEmpty(message = "Operating days are required")
    private List<Integer> operatingDays;
    
    // For response
    private LocationDTO originLocation;
    private LocationDTO destinationLocation;
}
