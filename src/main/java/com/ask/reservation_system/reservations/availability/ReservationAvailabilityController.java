package com.ask.reservation_system.reservations.availability;


import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservation/availability")
public class ReservationAvailabilityController {

    private static final Logger log = LoggerFactory.getLogger(ReservationAvailabilityController.class);

    private final ReservationAvailabilityService service;
    public ReservationAvailabilityController(ReservationAvailabilityService service) {
        this.service = service;
    }

    @PostMapping("/check")
    public ResponseEntity<CheckAvailabilityResponse> checkAvailability(
            @Valid CheckAvailabilityRequest request
    ) {
        log.info("Called method checkAvailability: request={}", request);
        boolean isAvailable = service.isReservationAvailable(
                request.roomId(), request.startDate(), request.endDate()
        );

        var message = isAvailable
                ? "Room is available to reservation"
                : "Room is not available to reservation";
        var status = isAvailable
                ? AvailibilityStatus.AVAILABLE
                : AvailibilityStatus.RESERVED;
        return ResponseEntity.ok(new CheckAvailabilityResponse(message, status));
    }
}
