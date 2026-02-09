package com.aviation.config;

import com.aviation.entity.*;
import com.aviation.repository.LocationRepository;
import com.aviation.repository.TransportationRepository;
import com.aviation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final TransportationRepository transportationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initUsers();
        initLocationsAndTransportations();
    }

    private void initUsers() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(UserRole.ADMIN);
            userRepository.save(admin);
            log.info("Default admin user created");
        }

        if (userRepository.findByUsername("agency").isEmpty()) {
            User agency = new User();
            agency.setUsername("agency");
            agency.setPassword(passwordEncoder.encode("agency123"));
            agency.setRole(UserRole.AGENCY);
            userRepository.save(agency);
            log.info("Default agency user created");
        }
    }

    private void initLocationsAndTransportations() {
        if (locationRepository.count() > 0) {
            log.info("Sample data already exists, skipping initialization.");
            return;
        }

        // --- Locations ---
        Location taksimSquare = createLocation("Taksim Square", "Turkey", "Istanbul", "CCIST");
        Location istanbulAirport = createLocation("Istanbul Airport", "Turkey", "Istanbul", "IST");
        Location sabihaGokcen = createLocation("Sabiha Gokcen Airport", "Turkey", "Istanbul", "SAW");
        Location londonHeathrow = createLocation("London Heathrow Airport", "United Kingdom", "London", "LHR");
        Location wembleyStadium = createLocation("Wembley Stadium", "United Kingdom", "London", "WEMB");
        Location kabatasPier = createLocation("Kabatas Pier", "Turkey", "Istanbul", "KBTSP");
        Location ankaraEsenboga = createLocation("Ankara Esenboga Airport", "Turkey", "Ankara", "ESB");
        Location ankaraCityCentre = createLocation("Ankara City Centre", "Turkey", "Ankara", "CCANK");

        log.info("Sample locations created: {} locations", locationRepository.count());

        // --- Transportations ---
        List<Integer> everyDay = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
        List<Integer> weekdays = Arrays.asList(1, 2, 3, 4, 5);
        List<Integer> monWedFriSun = Arrays.asList(1, 3, 5, 7);
        List<Integer> tueThuSat = Arrays.asList(2, 4, 6);

        // Before-flight transfers (non-FLIGHT from city to airport)
        createTransportation(taksimSquare, istanbulAirport, TransportationType.BUS, everyDay);
        createTransportation(taksimSquare, istanbulAirport, TransportationType.UBER, everyDay);
        createTransportation(taksimSquare, sabihaGokcen, TransportationType.BUS, weekdays);
        createTransportation(kabatasPier, istanbulAirport, TransportationType.SUBWAY, everyDay);
        createTransportation(kabatasPier, taksimSquare, TransportationType.SUBWAY, everyDay);
        createTransportation(ankaraCityCentre, ankaraEsenboga, TransportationType.BUS, everyDay);
        createTransportation(ankaraCityCentre, ankaraEsenboga, TransportationType.UBER, weekdays);

        // Flights
        createTransportation(istanbulAirport, londonHeathrow, TransportationType.FLIGHT, everyDay);
        createTransportation(sabihaGokcen, londonHeathrow, TransportationType.FLIGHT, monWedFriSun);
        createTransportation(istanbulAirport, ankaraEsenboga, TransportationType.FLIGHT, weekdays);
        createTransportation(ankaraEsenboga, londonHeathrow, TransportationType.FLIGHT, tueThuSat);

        // After-flight transfers (non-FLIGHT from airport to destination)
        createTransportation(londonHeathrow, wembleyStadium, TransportationType.BUS, everyDay);
        createTransportation(londonHeathrow, wembleyStadium, TransportationType.UBER, everyDay);
        createTransportation(ankaraEsenboga, ankaraCityCentre, TransportationType.BUS, everyDay);
        createTransportation(ankaraEsenboga, ankaraCityCentre, TransportationType.UBER, weekdays);

        log.info("Sample transportations created: {} transportations", transportationRepository.count());
    }

    private Location createLocation(String name, String country, String city, String locationCode) {
        Location location = new Location();
        location.setName(name);
        location.setCountry(country);
        location.setCity(city);
        location.setLocationCode(locationCode);
        return locationRepository.save(location);
    }

    private Transportation createTransportation(Location origin, Location destination,
                                                 TransportationType type, List<Integer> operatingDays) {
        Transportation transportation = new Transportation();
        transportation.setOriginLocation(origin);
        transportation.setDestinationLocation(destination);
        transportation.setTransportationType(type);
        transportation.setOperatingDays(operatingDays);
        return transportationRepository.save(transportation);
    }
}
