package com.gmao.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AiPredictionDTO {
    private Long machineId;
    private String machineName;
    private String machineLocation;
    private double riskScore;          // 0.0 - 100.0
    private String riskLevel;          // LOW, MEDIUM, HIGH, CRITICAL
    private List<String> alerts;
    private List<String> recommendations;
    private Double temperature;
    private Double vibration;
    private Double runtime;
    private LocalDateTime analyzedAt;

    public AiPredictionDTO() {}

    public Long getMachineId() { return machineId; }
    public void setMachineId(Long machineId) { this.machineId = machineId; }
    public String getMachineName() { return machineName; }
    public void setMachineName(String machineName) { this.machineName = machineName; }
    public String getMachineLocation() { return machineLocation; }
    public void setMachineLocation(String machineLocation) { this.machineLocation = machineLocation; }
    public double getRiskScore() { return riskScore; }
    public void setRiskScore(double riskScore) { this.riskScore = riskScore; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public List<String> getAlerts() { return alerts; }
    public void setAlerts(List<String> alerts) { this.alerts = alerts; }
    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }
    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public Double getVibration() { return vibration; }
    public void setVibration(Double vibration) { this.vibration = vibration; }
    public Double getRuntime() { return runtime; }
    public void setRuntime(Double runtime) { this.runtime = runtime; }
    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }
}
