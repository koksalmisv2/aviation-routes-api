package com.aviation.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "transportations", indexes = {
    @Index(name = "idx_transport_type", columnList = "transportation_type"),
    @Index(name = "idx_origin_id", columnList = "origin_location_id"),
    @Index(name = "idx_dest_id", columnList = "destination_location_id"),
    @Index(name = "idx_origin_dest_type", columnList = "origin_location_id, destination_location_id, transportation_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"originLocation", "destinationLocation", "operatingDays"})
public class Transportation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Origin location is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_location_id", nullable = false)
    private Location originLocation;
    
    @NotNull(message = "Destination location is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_location_id", nullable = false)
    private Location destinationLocation;
    
    @NotNull(message = "Transportation type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "transportation_type")
    private TransportationType transportationType;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "transportation_operating_days", 
                     joinColumns = @JoinColumn(name = "transportation_id"))
    @Column(name = "day_of_week")
    private List<Integer> operatingDays;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transportation other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
