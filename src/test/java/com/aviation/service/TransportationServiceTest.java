package com.aviation.service;

import com.aviation.dto.TransportationDTO;
import com.aviation.entity.Location;
import com.aviation.entity.Transportation;
import com.aviation.entity.TransportationType;
import com.aviation.repository.LocationRepository;
import com.aviation.repository.TransportationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransportationServiceTest {

    @Mock
    private TransportationRepository transportationRepository;

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private TransportationService transportationService;

    private Location istanbulAirport;
    private Location londonHeathrow;
    private Transportation flight;

    @BeforeEach
    void setUp() {
        istanbulAirport = new Location(1L, "Istanbul Airport", "Turkey", "Istanbul", "IST");
        londonHeathrow = new Location(2L, "London Heathrow", "UK", "London", "LHR");

        flight = new Transportation(
                1L, istanbulAirport, londonHeathrow,
                TransportationType.FLIGHT, Arrays.asList(1, 2, 3, 4, 5, 6, 7)
        );
    }

    @Test
    void testGetAllTransportations() {
        Transportation bus = new Transportation(
                2L, istanbulAirport, londonHeathrow,
                TransportationType.BUS, Arrays.asList(1, 3, 5)
        );

        when(transportationRepository.findAllWithOperatingDays()).thenReturn(Arrays.asList(flight, bus));

        List<TransportationDTO> result = transportationService.getAllTransportations();

        assertEquals(2, result.size());
        verify(transportationRepository, times(1)).findAllWithOperatingDays();
    }

    @Test
    void testGetTransportationById() {
        when(transportationRepository.findByIdWithOperatingDays(1L)).thenReturn(Optional.of(flight));

        TransportationDTO result = transportationService.getTransportationById(1L);

        assertNotNull(result);
        assertEquals(TransportationType.FLIGHT, result.getTransportationType());
        assertEquals(1L, result.getOriginLocationId());
        assertEquals(2L, result.getDestinationLocationId());
        assertEquals(7, result.getOperatingDays().size());
    }

    @Test
    void testGetTransportationByIdNotFound() {
        when(transportationRepository.findByIdWithOperatingDays(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> transportationService.getTransportationById(99L));
    }

    @Test
    void testCreateTransportation() {
        TransportationDTO dto = new TransportationDTO();
        dto.setOriginLocationId(1L);
        dto.setDestinationLocationId(2L);
        dto.setTransportationType(TransportationType.FLIGHT);
        dto.setOperatingDays(Arrays.asList(1, 2, 3, 4, 5, 6, 7));

        when(locationRepository.findById(1L)).thenReturn(Optional.of(istanbulAirport));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(londonHeathrow));
        when(transportationRepository.save(any(Transportation.class))).thenReturn(flight);

        TransportationDTO result = transportationService.createTransportation(dto);

        assertNotNull(result);
        assertEquals(TransportationType.FLIGHT, result.getTransportationType());
        verify(transportationRepository, times(1)).save(any(Transportation.class));
    }

    @Test
    void testCreateTransportationOriginNotFound() {
        TransportationDTO dto = new TransportationDTO();
        dto.setOriginLocationId(99L);
        dto.setDestinationLocationId(2L);
        dto.setTransportationType(TransportationType.BUS);
        dto.setOperatingDays(Arrays.asList(1, 2, 3));

        when(locationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> transportationService.createTransportation(dto));
    }

    @Test
    void testCreateTransportationSameOriginAndDestination() {
        TransportationDTO dto = new TransportationDTO();
        dto.setOriginLocationId(1L);
        dto.setDestinationLocationId(1L);
        dto.setTransportationType(TransportationType.BUS);
        dto.setOperatingDays(Arrays.asList(1, 2, 3));

        assertThrows(RuntimeException.class, () -> transportationService.createTransportation(dto));
    }

    @Test
    void testCreateTransportationInvalidOperatingDays() {
        TransportationDTO dto = new TransportationDTO();
        dto.setOriginLocationId(1L);
        dto.setDestinationLocationId(2L);
        dto.setTransportationType(TransportationType.BUS);
        dto.setOperatingDays(Arrays.asList(0, 8));

        assertThrows(RuntimeException.class, () -> transportationService.createTransportation(dto));
    }

    @Test
    void testUpdateTransportation() {
        TransportationDTO dto = new TransportationDTO();
        dto.setOriginLocationId(1L);
        dto.setDestinationLocationId(2L);
        dto.setTransportationType(TransportationType.BUS);
        dto.setOperatingDays(Arrays.asList(1, 3, 5));

        Transportation updatedTransportation = new Transportation(
                1L, istanbulAirport, londonHeathrow,
                TransportationType.BUS, Arrays.asList(1, 3, 5)
        );

        when(transportationRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(locationRepository.findById(1L)).thenReturn(Optional.of(istanbulAirport));
        when(locationRepository.findById(2L)).thenReturn(Optional.of(londonHeathrow));
        when(transportationRepository.save(any(Transportation.class))).thenReturn(updatedTransportation);

        TransportationDTO result = transportationService.updateTransportation(1L, dto);

        assertNotNull(result);
        assertEquals(TransportationType.BUS, result.getTransportationType());
        assertEquals(3, result.getOperatingDays().size());
    }

    @Test
    void testDeleteTransportation() {
        when(transportationRepository.existsById(1L)).thenReturn(true);

        transportationService.deleteTransportation(1L);

        verify(transportationRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteTransportationNotFound() {
        when(transportationRepository.existsById(99L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> transportationService.deleteTransportation(99L));
    }
}
