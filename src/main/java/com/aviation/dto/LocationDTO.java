package com.aviation.dto;

import com.aviation.entity.Location;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Location code is required")
    @Size(min = 3, message = "Location code must be at least 3 characters")
    private String locationCode;

    public static LocationDTO from(Location location) {
        LocationDTO dto = new LocationDTO();
        dto.setId(location.getId());
        dto.setName(location.getName());
        dto.setCountry(location.getCountry());
        dto.setCity(location.getCity());
        dto.setLocationCode(location.getLocationCode());
        return dto;
    }
}
