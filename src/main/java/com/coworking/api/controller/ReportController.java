package com.coworking.api.controller;

import com.coworking.api.domain.dto.OccupancyReportResponse;
import com.coworking.api.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/occupancy")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OccupancyReportResponse> getOccupancyReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(reportService.getOccupancyReport(startDate, endDate));
    }

}
