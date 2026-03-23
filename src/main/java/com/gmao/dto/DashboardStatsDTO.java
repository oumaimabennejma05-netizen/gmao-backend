package com.gmao.dto;

public class DashboardStatsDTO {
    private long totalMachines;
    private long operationalMachines;
    private long maintenanceMachines;
    private long brokenMachines;
    private long totalTasks;
    private long pendingTasks;
    private long inProgressTasks;
    private long completedTasks;
    private long totalUsers;
    private long criticalAlerts;

    public DashboardStatsDTO() {}

    public long getTotalMachines() { return totalMachines; }
    public void setTotalMachines(long totalMachines) { this.totalMachines = totalMachines; }
    public long getOperationalMachines() { return operationalMachines; }
    public void setOperationalMachines(long operationalMachines) { this.operationalMachines = operationalMachines; }
    public long getMaintenanceMachines() { return maintenanceMachines; }
    public void setMaintenanceMachines(long maintenanceMachines) { this.maintenanceMachines = maintenanceMachines; }
    public long getBrokenMachines() { return brokenMachines; }
    public void setBrokenMachines(long brokenMachines) { this.brokenMachines = brokenMachines; }
    public long getTotalTasks() { return totalTasks; }
    public void setTotalTasks(long totalTasks) { this.totalTasks = totalTasks; }
    public long getPendingTasks() { return pendingTasks; }
    public void setPendingTasks(long pendingTasks) { this.pendingTasks = pendingTasks; }
    public long getInProgressTasks() { return inProgressTasks; }
    public void setInProgressTasks(long inProgressTasks) { this.inProgressTasks = inProgressTasks; }
    public long getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(long completedTasks) { this.completedTasks = completedTasks; }
    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    public long getCriticalAlerts() { return criticalAlerts; }
    public void setCriticalAlerts(long criticalAlerts) { this.criticalAlerts = criticalAlerts; }
}
