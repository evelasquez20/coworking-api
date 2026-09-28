package com.coworking.api.service;

import com.coworking.api.domain.dto.ReservationRequest;
import com.coworking.api.domain.dto.ReservationResponse;
import com.coworking.api.domain.entity.Reservation;
import com.coworking.api.domain.entity.Space;
import com.coworking.api.domain.entity.User;
import com.coworking.api.domain.enums.ReservationStatusEnum;
import com.coworking.api.domain.enums.SpaceType;
import com.coworking.api.exception.BusinessException;
import com.coworking.api.exception.ErrorCode;
import com.coworking.api.repository.ReservationRepository;
import com.coworking.api.repository.SpaceRepository;
import com.coworking.api.repository.UserRepository;
import com.coworking.api.service.impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private SpaceRepository spaceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private User mockUser;
    private Space mockSpace;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .email("usuario@coworking.com")
                .password("password123")
                .build();

        mockSpace = Space.builder()
                .id(1L)
                .name("Sala Conferencia A")
                .type(SpaceType.SALA_REUNIONES) // Corregido acorde a los enums en español usados previamente
                .capacity(10)
                .hourlyRate(new BigDecimal("25.00"))
                .build();
    }

    @Test
    @DisplayName("Debe crear una reserva exitosamente cuando los datos son válidos")
    void createReservation_Success() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime endTime = startTime.plusHours(2);

        // Ajustado a los 4 argumentos que requiere tu ReservationRequest (ej: spaceId, startTime, endTime, notes/etc. o según tu DTO)
        // Asegúrate de pasar los 4 argumentos exactos que tiene tu record ReservationRequest
        ReservationRequest request = new ReservationRequest(1L, startTime, endTime, "Reserva de prueba");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(spaceRepository.findById(anyLong())).thenReturn(Optional.of(mockSpace));
        when(reservationRepository.existsOverlappingReservation(anyLong(), any(), any(), anyList())).thenReturn(false);

        Reservation savedReservation = Reservation.builder()
                .id(100L)
                .user(mockUser)
                .space(mockSpace)
                .startTime(startTime)
                .endTime(endTime)
                .totalCost(new BigDecimal("50.00"))
                .status(ReservationStatusEnum.PENDING_PAYMENT)
                .build();

        when(reservationRepository.save(any(Reservation.class))).thenReturn(savedReservation);

        ReservationResponse response = reservationService.createReservation(request, "usuario@coworking.com");

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("Sala Conferencia A", response.spaceName());
        assertEquals(new BigDecimal("50.00"), response.totalPrice());
        assertEquals(ReservationStatusEnum.PENDING_PAYMENT, response.status());

        verify(reservationRepository, times(1)).save(any(Reservation.class));
    }

    @Test
    @DisplayName("Debe lanzar BusinessException cuando hay solapamiento de horarios")
    void createReservation_ThrowsOverlapException() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        LocalDateTime endTime = startTime.plusHours(2);

        ReservationRequest request = new ReservationRequest(1L, startTime, endTime, "Reserva solapada");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(spaceRepository.findById(anyLong())).thenReturn(Optional.of(mockSpace));
        when(reservationRepository.existsOverlappingReservation(anyLong(), any(), any(), anyList())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () ->
                reservationService.createReservation(request, "usuario@coworking.com")
        );

        assertEquals(ErrorCode.RESERVATION_OVERLAP, exception.getErrorCode());
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

}
