package com.aviation.entity;

public enum SegmentType {
    BEFORE_FLIGHT,
    FLIGHT,
    AFTER_FLIGHT;

    public String getDisplayName() {
        return switch (this) {
            case BEFORE_FLIGHT -> "Before Flight Transfer";
            case FLIGHT -> "Flight";
            case AFTER_FLIGHT -> "After Flight Transfer";
        };
    }
}
