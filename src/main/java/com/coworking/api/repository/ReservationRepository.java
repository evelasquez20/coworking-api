package com.coworking.api.repository;

import com.coworking.api.domain.entity.Reservation;
import com.coworking.api.domain.enums.ReservationStatusEnum;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT COUNT(r) > 0 FROM Reservation r
        WHERE r.space.id = :spaceId
          AND r.status IN (:activeStatuses)
          AND (:startTime < r.endTime AND :endTime > r.startTime)
    """)
    boolean existsOverlappingReservation(
            @Param("spaceId") Long spaceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("activeStatuses") List<ReservationStatusEnum> activeStatuses
    );

    @Query("""
        SELECT r FROM Reservation r
        WHERE r.space.id = :spaceId
          AND r.startTime >= :startDate
          AND r.endTime <= :endDate
          AND r.status = 'CONFIRMED'
    """)
    List<Reservation> findConfirmedReservationsInPeriod(
            @Param("spaceId") Long spaceId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

}
