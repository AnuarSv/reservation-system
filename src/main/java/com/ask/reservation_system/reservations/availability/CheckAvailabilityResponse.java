package com.ask.reservation_system.reservations.availability;

public record CheckAvailabilityResponse(
    String message,
    AvailibilityStatus status
) {
}
