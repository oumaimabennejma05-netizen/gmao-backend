package com.gmao.dto;

import java.time.LocalDateTime;

public class MachineDataDTO {
    private Long id;
    private Long machineId;
    private String machineName;
    private Double temperature;
    private Double vibration;
    private Double runtime;
    private LocalDateTime createdAt;

    public MachineDataDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMachineId() { return machineId; }
    public void setMachineId(Long machineId) { this.machineId = machineId; }
    public String getMachineName() { return machineName; }
    public void setMachineName(String machineName) { this.machineName = machineName; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public Double getVibration() { return vibration; }
    public void setVibration(Double vibration) { this.vibration = vibration; }
    public Double getRuntime() { return runtime; }
    public void setRuntime(Double runtime) { this.runtime = runtime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
