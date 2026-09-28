package com.coworking.api.service.impl;

import com.coworking.api.domain.dto.OccupancyReportResponse;
import com.coworking.api.repository.ReservationRepository;
import com.coworking.api.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReservationRepository reservationRepository;

    @Override
    @Cacheable(value = "occupancyReports", key = "#startDate.toString() + '_' + #endDate.toString()")
    public OccupancyReportResponse getOccupancyReport(LocalDate startDate, LocalDate endDate) {
        log.info("Iniciando generación de reporte de ocupación para el rango de fechas: {} a {}", startDate, endDate);

        try {
            // Simulación de consulta analítica pesada
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Error por interrupción durante la simulación del cálculo del reporte entre {} y {}", startDate, endDate, e);
        }

        long count = reservationRepository.count();
        BigDecimal occupancyPercentage = BigDecimal.valueOf(82.5);
        long totalReservedHours = count * 4;

        log.info("Cálculo del reporte de ocupación completado desde BD para el rango: {} a {}. Total reservas: {}, Horas reservadas: {}",
                startDate, endDate, count, totalReservedHours);

        return new OccupancyReportResponse(
                startDate,
                endDate,
                count,
                occupancyPercentage,
                totalReservedHours
        );
    }

}
