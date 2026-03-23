package com.gmao.controller;

import com.gmao.dto.ApiResponse;
import com.gmao.dto.DashboardStatsDTO;
import com.gmao.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<DashboardStatsDTO>> getDashboardStats() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getDashboardStats()));
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getFullReport() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getFullReport()));
    }

    /**
     * Maintenance alerts — machines due within 3 days or overdue.
     * Accessible by all authenticated users (for navbar notification badge).
     */
    @GetMapping("/machines/maintenance-alerts")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getMaintenanceAlerts() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getMaintenanceAlerts()));
    }
}