package com.aviation.repository;

import com.aviation.entity.Transportation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransportationRepository extends JpaRepository<Transportation, Long> {
    
    @Query("SELECT DISTINCT t FROM Transportation t LEFT JOIN FETCH t.operatingDays")
    List<Transportation> findAllWithOperatingDays();

    @Query("SELECT t FROM Transportation t LEFT JOIN FETCH t.operatingDays WHERE t.id = :id")
    Optional<Transportation> findByIdWithOperatingDays(@Param("id") Long id);

    /**
     * Single optimized query that fetches all transportations relevant for route calculation:
     * - All FLIGHT transportations operating on the given day
     * - All non-FLIGHT transportations FROM the origin operating on the given day
     * - All non-FLIGHT transportations TO the destination operating on the given day
     *
     * Replaces 3 separate queries (findAvailableFlights, findAvailableNonFlightsFromOrigin,
     * findAvailableNonFlightsToDestination) with a single DB round-trip.
     */
    @Query("""
            SELECT DISTINCT t FROM Transportation t
            LEFT JOIN FETCH t.operatingDays
            LEFT JOIN FETCH t.originLocation
            LEFT JOIN FETCH t.destinationLocation
            WHERE :dayOfWeek MEMBER OF t.operatingDays
            AND (
                t.transportationType = 'FLIGHT'
                OR (t.transportationType <> 'FLIGHT' AND t.originLocation.id = :originId)
                OR (t.transportationType <> 'FLIGHT' AND t.destinationLocation.id = :destinationId)
            )
            """)
    List<Transportation> findAllRelevantForRoute(@Param("originId") Long originId,
                                                  @Param("destinationId") Long destinationId,
                                                  @Param("dayOfWeek") int dayOfWeek);

}
