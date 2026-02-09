-- Sample data for testing the Aviation Routes API

-- Locations
-- Note: IDs are auto-generated, these are just examples

-- INSERT INTO locations (name, country, city, location_code) VALUES
-- ('Taksim Square', 'Turkey', 'Istanbul', 'CCIST'),
-- ('Istanbul Airport', 'Turkey', 'Istanbul', 'IST'),
-- ('Sabiha Gokcen Airport', 'Turkey', 'Istanbul', 'SAW'),
-- ('London Heathrow Airport', 'UK', 'London', 'LHR'),
-- ('Wembley Stadium', 'UK', 'London', 'WEMB'),
-- ('Kabatas Pier', 'Turkey', 'Istanbul', 'KABTS');

-- Transportations
-- Note: Operating days: 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun

-- Taksim to Istanbul Airport (BUS) - operates every day
-- INSERT INTO transportations (origin_location_id, destination_location_id, transportation_type) 
-- VALUES (1, 2, 'BUS');
-- INSERT INTO transportation_operating_days (transportation_id, day_of_week) VALUES 
-- (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7);

-- Taksim to Istanbul Airport (SUBWAY) - operates every day
-- INSERT INTO transportations (origin_location_id, destination_location_id, transportation_type) 
-- VALUES (1, 2, 'SUBWAY');
-- INSERT INTO transportation_operating_days (transportation_id, day_of_week) VALUES 
-- (2, 1), (2, 2), (2, 3), (2, 4), (2, 5), (2, 6), (2, 7);

-- Taksim to Sabiha Gokcen (BUS) - operates every day
-- INSERT INTO transportations (origin_location_id, destination_location_id, transportation_type) 
-- VALUES (1, 3, 'BUS');
-- INSERT INTO transportation_operating_days (transportation_id, day_of_week) VALUES 
-- (3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 7);

-- Istanbul Airport to London Heathrow (FLIGHT) - operates Mon, Wed, Fri, Sat
-- INSERT INTO transportations (origin_location_id, destination_location_id, transportation_type) 
-- VALUES (2, 4, 'FLIGHT');
-- INSERT INTO transportation_operating_days (transportation_id, day_of_week) VALUES 
-- (4, 1), (4, 3), (4, 5), (4, 6);

-- Sabiha Gokcen to London Heathrow (FLIGHT) - operates every day
-- INSERT INTO transportations (origin_location_id, destination_location_id, transportation_type) 
-- VALUES (3, 4, 'FLIGHT');
-- INSERT INTO transportation_operating_days (transportation_id, day_of_week) VALUES 
-- (5, 1), (5, 2), (5, 3), (5, 4), (5, 5), (5, 6), (5, 7);

-- London Heathrow to Wembley (BUS) - operates every day
-- INSERT INTO transportations (origin_location_id, destination_location_id, transportation_type) 
-- VALUES (4, 5, 'BUS');
-- INSERT INTO transportation_operating_days (transportation_id, day_of_week) VALUES 
-- (6, 1), (6, 2), (6, 3), (6, 4), (6, 5), (6, 6), (6, 7);

-- London Heathrow to Wembley (UBER) - operates every day
-- INSERT INTO transportations (origin_location_id, destination_location_id, transportation_type) 
-- VALUES (4, 5, 'UBER');
-- INSERT INTO transportation_operating_days (transportation_id, day_of_week) VALUES 
-- (7, 1), (7, 2), (7, 3), (7, 4), (7, 5), (7, 6), (7, 7);
