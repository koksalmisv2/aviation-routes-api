// ---- Auth ----
export interface AuthRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  role: string;
}

// ---- Location ----
export interface LocationDTO {
  id?: number;
  name: string;
  country: string;
  city: string;
  locationCode: string;
}

// ---- Transportation ----
export type TransportationType = 'FLIGHT' | 'BUS' | 'SUBWAY' | 'UBER';

export interface TransportationDTO {
  id?: number;
  originLocationId: number;
  destinationLocationId: number;
  transportationType: TransportationType;
  operatingDays: number[];
  // Populated in responses
  originLocation?: LocationDTO;
  destinationLocation?: LocationDTO;
}

// ---- Route ----
export interface TransportationSegmentDTO {
  transportationId: number;
  type: TransportationType;
  from: LocationDTO;
  to: LocationDTO;
  segmentType: 'BEFORE_FLIGHT' | 'FLIGHT' | 'AFTER_FLIGHT';
}

export interface RouteDTO {
  segments: TransportationSegmentDTO[];
}
