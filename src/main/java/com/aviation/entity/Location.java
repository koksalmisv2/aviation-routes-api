package com.aviation.entity;

import com.aviation.dto.LocationDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;

@Entity
@Table(name = "locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Location {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Column(nullable = false)
    private String name;
    
    @NotBlank(message = "Country is required")
    @Column(nullable = false)
    private String country;
    
    @NotBlank(message = "City is required")
    @Column(nullable = false)
    private String city;
    
    @NotBlank(message = "Location code is required")
    @Size(min = 3, message = "Location code must be at least 3 characters")
    @Column(nullable = false, unique = true, name = "location_code")
    private String locationCode;

    public static Location toEntity(LocationDTO dto) {
        Location location = new Location();
        location.setName(dto.getName());
        location.setCountry(dto.getCountry());
        location.setCity(dto.getCity());
        location.setLocationCode(dto.getLocationCode());
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
