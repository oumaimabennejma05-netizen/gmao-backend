package com.gmao.service;

import com.gmao.dto.DashboardStatsDTO;
import com.gmao.entity.Machine;
import com.gmao.enums.MachineStatus;
import com.gmao.enums.TaskStatus;
import com.gmao.repository.MachineRepository;
import com.gmao.repository.TaskRepository;
import com.gmao.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final MachineRepository machineRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public ReportService(MachineRepository machineRepository,
                         TaskRepository taskRepository,
                         UserRepository userRepository) {
        this.machineRepository = machineRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        stats.setTotalMachines(machineRepository.count());
        stats.setOperationalMachines(machineRepository.countByStatus(MachineStatus.OPERATIONAL));
        stats.setMaintenanceMachines(machineRepository.countByStatus(MachineStatus.MAINTENANCE));
        stats.setBrokenMachines(machineRepository.countByStatus(MachineStatus.BROKEN));

        stats.setTotalTasks(taskRepository.count());
        stats.setPendingTasks(taskRepository.countByStatus(TaskStatus.PENDING));
        stats.setInProgressTasks(taskRepository.countByStatus(TaskStatus.IN_PROGRESS));
        stats.setCompletedTasks(taskRepository.countByStatus(TaskStatus.COMPLETED)
                + taskRepository.countByStatus(TaskStatus.APPROVED));

        stats.setTotalUsers(userRepository.count());

        return stats;
    }

    public Map<String, Object> getFullReport() {
        Map<String, Object> report = new HashMap<>();
        DashboardStatsDTO stats = getDashboardStats();

        long pendingApprovalCount = taskRepository.countByStatus(TaskStatus.PENDING_APPROVAL);

        report.put("stats", stats);
        report.put("taskCompletionRate", calculateCompletionRate(stats));
        report.put("machineHealthRate", calculateMachineHealthRate(stats));
        report.put("pendingApprovalTasks", pendingApprovalCount);
        report.put("generatedAt", java.time.LocalDateTime.now().toString());

        return report;
    }

    /**
     * Returns machines with maintenance due within 3 days or overdue.
     */
    public List<Map<String, Object>> getMaintenanceAlerts() {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(3);

        return machineRepository.findAll().stream()
                .filter(m -> m.getMaintenanceDate() != null
                        && !m.getMaintenanceDate().isAfter(threshold))
                .map(m -> {
                    Map<String, Object> alert = new HashMap<>();
                    alert.put("machineId", m.getId());
                    alert.put("machineName", m.getName());
                    alert.put("maintenanceDate", m.getMaintenanceDate().toString());
                    alert.put("status", m.getStatus().name());
                    boolean overdue = m.getMaintenanceDate().isBefore(today);
                    alert.put("overdue", overdue);
                    alert.put("daysUntil", today.until(m.getMaintenanceDate()).getDays());
                    return alert;
                })
                .collect(Collectors.toList());
    }

    private double calculateCompletionRate(DashboardStatsDTO stats) {
        if (stats.getTotalTasks() == 0) return 0.0;
        return (double) stats.getCompletedTasks() / stats.getTotalTasks() * 100;
    }

    private double calculateMachineHealthRate(DashboardStatsDTO stats) {
        if (stats.getTotalMachines() == 0) return 0.0;
        return (double) stats.getOperationalMachines() / stats.getTotalMachines() * 100;
    }
}