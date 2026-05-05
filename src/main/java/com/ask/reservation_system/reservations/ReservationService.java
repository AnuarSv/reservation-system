package com.ask.reservation_system.reservations;

import com.ask.reservation_system.reservations.availability.ReservationAvailabilityService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class ReservationService {
    // 1. Intall Logger
    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    // 2. Connect to DB
    private final ReservationRepository repository;
    private final ReservationMapper mapper;
    private final ReservationAvailabilityService availabilityService;

    public ReservationService(ReservationRepository repository, ReservationMapper mapper, ReservationAvailabilityService availabilityService) {
        this.repository = repository;
        this.mapper = mapper;
        this.availabilityService = availabilityService;
    }


    // 3. Implement business logic
    public Reservation getReservationById(Long id) {

        ReservationEntity reservationEntity =  repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id"));
        return mapper.toDomain(reservationEntity);
    }


    public List<Reservation> searchAllByFilter(
            ReservationSearchFilter filter
    ) {
        int pageSize = filter.pageSize() != null ? filter.pageSize() : 10;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;

        var pageable = Pageable
                .ofSize(pageSize)
                .withPage(pageNumber);

        List<ReservationEntity> allEntities = repository.searchAllByFilter(
                filter.roomId(),
                filter.userId(),
                pageable
        );

        return allEntities.stream()
                .map(mapper::toDomain).toList();
    }

    public Reservation createReservation(Reservation reservationToCreate) {
        if (reservationToCreate.status() != null) { // Status не должен быть пустым
            throw new IllegalArgumentException("Id should be empty");
        }
        if (!reservationToCreate.endDate().isAfter(reservationToCreate.startDate())) {
            throw new IllegalArgumentException("End date should be after start date");
        }

        var entityToSave = mapper.toEntity(reservationToCreate);
        entityToSave.setStatus(ReservationStatus.PENDING);

        var savedEntity = repository.save(entityToSave);
        return mapper.toDomain(savedEntity);
    }

    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {
        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

        if(reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalArgumentException("Cannot modify reservation status=" + reservationEntity.getStatus());
        }
        if (!reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())) {
            throw new IllegalArgumentException("End date should be after start date");
        }

        var reservationToSave = mapper.toEntity(reservationToUpdate);
        reservationToSave.setId(reservationEntity.getId());
        reservationToSave.setStatus(ReservationStatus.PENDING);

        var updatedReservation = repository.save(reservationToSave);
        return mapper.toDomain(reservationToSave);
    }

    @Transactional
    public void cancelReservation(Long id) {
        var reservation = repository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));
        if (reservation.getStatus().equals(ReservationStatus.APPROVED)) {
            throw new IllegalStateException("Cannot cancel approved reservation");
        }
        if (reservation.getStatus().equals(ReservationStatus.CANCELLED)) {
            throw new IllegalStateException("Cannot cancel cancelled reservation");
        }
        repository.setStatus(id, ReservationStatus.CANCELLED);
        log.info("Successfully cancelled reservation: id={}", id);
//        repository.deleteById(id);
    }

    public Reservation approveReservation(Long id) { // Todo: Разобрать ничего не понятно для чего мы делаем isReservationConflict()?
        var reservationEntity = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Not found reservation by id = " + id));

        if(reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalArgumentException("Cannot approve reservation status=" + reservationEntity.getStatus());
        }
        var isAvailableToApprove = availabilityService.isReservationAvailable(reservationEntity.getRoomId(), reservationEntity.getStartDate(), reservationEntity.getEndDate());
        if (!isAvailableToApprove) {
            throw new IllegalArgumentException("Cannot approve reservation status=" + reservationEntity);
        }

        reservationEntity.setStatus(ReservationStatus.APPROVED);
        repository.save(reservationEntity);

        return mapper.toDomain(reservationEntity);
    }

}
