package com.coworking.api.service;

import com.coworking.api.domain.dto.OccupancyReportResponse;
import java.time.LocalDate;

public interface ReportService {

    OccupancyReportResponse getOccupancyReport(LocalDate startDate, LocalDate endDate);

}
