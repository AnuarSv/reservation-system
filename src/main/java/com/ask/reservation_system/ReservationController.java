package com.ask.reservation_system;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/reservation")
public class ReservationController {

    // 1. Intall Logger
    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);

    // 2. Connect to Business Logic
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // 3. Implement REST API
    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getReservationById(
            @PathVariable("id") Long id
    ) {
            log.info("Called getReservationById: id=" + id);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(reservationService.getReservationById(id));
    }

    @GetMapping()
    public ResponseEntity<List<Reservation>> getAllReservations() {
        log.info("Called getAllReservations");
        var get = reservationService.findAllReservation();
        return ResponseEntity.ok(get);
    }

    @PostMapping()
    public ResponseEntity<Reservation> createReservation(
            @RequestBody @Valid Reservation reservationToCreate
            ) {
        log.info("createReservation");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(reservationToCreate));
        // return reservationService.createReservation(reservationToCreate);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> updateReservation(
            @PathVariable("id") Long id,
            @RequestBody @Valid Reservation reservationToUpdate
    ) {
        log.info("Called updateReservation id={}, updateReservation={}", id, reservationToUpdate);
        var reservation = reservationService.updateReservation(id, reservationToUpdate);
        return ResponseEntity.ok(reservation);
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable("id") Long id
    ) {
        log.info("Called deleteReservation id={}", id);
        reservationService.cancelReservation(id);
        return ResponseEntity.ok().build();

    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<Reservation> approveReservation(
            @PathVariable("id") Long id
    ) {
        log.info("Called approveReservation: id={}", id);
        var reservation = reservationService.approveReservation(id);
        return ResponseEntity.ok(reservation);
    }

}
