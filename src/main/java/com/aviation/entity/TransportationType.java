package com.aviation.entity;

public enum TransportationType {
    FLIGHT,
    BUS,
    SUBWAY,
    UBER;

    public boolean isGroundTransport() {
        return switch (this) {
            case FLIGHT -> false;
            case BUS, SUBWAY, UBER -> true;
        };
    }
}
