package com.coworking.api.domain.entity;

import com.coworking.api.domain.enums.ReservationStatusEnum;
import com.coworking.api.domain.state.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations", indexes = {
        @Index(name = "idx_reservation_space_dates", columnList = "space_id, start_time, end_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "space_id", nullable = false)
    private Space space;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ReservationStatusEnum status = ReservationStatusEnum.PENDING_PAYMENT;

    @Transient
    private ReservationState state;

    // --- MÉTODOS DEL PATRÓN STATE ---

    @PostLoad
    public void initState() {
        if (this.status == null) {
            this.status = ReservationStatusEnum.PENDING_PAYMENT;
        }
        this.state = switch (this.status) {
            case CONFIRMED -> new ConfirmedState();
            case CANCELLED -> new CancelledState();
            case COMPLETED -> new CompletedState();
            default -> new PendingPaymentState();
        };
    }

    /**
     * Permite a las clases concretas de Estado cambiar el estado actual
     * e igualar la propiedad status que se guarda en la base de datos.
     */
    public void setState(ReservationState state) {
        this.state = state;
        if (state != null) {
            this.status = ReservationStatusEnum.valueOf(state.getStatusName());
        }
    }

    public void confirm() {
        if (this.state == null) {
            initState();
        }
        this.state.confirm(this);
    }

    public void cancel() {
        if (this.state == null) {
            initState();
        }
        this.state.cancel(this);
    }

}
