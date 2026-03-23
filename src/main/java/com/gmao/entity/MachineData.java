package com.gmao.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "machine_data")
public class MachineData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;

    private Double temperature;   // in Celsius
    private Double vibration;     // in mm/s
    private Double runtime;       // in hours

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public MachineData() {}

    public MachineData(Machine machine, Double temperature, Double vibration, Double runtime) {
        this.machine = machine;
        this.temperature = temperature;
        this.vibration = vibration;
        this.runtime = runtime;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // --- Getters ---
    public Long getId() { return id; }
    public Machine getMachine() { return machine; }
    public Double getTemperature() { return temperature; }
    public Double getVibration() { return vibration; }
    public Double getRuntime() { return runtime; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // --- Setters ---
    public void setId(Long id) { this.id = id; }
    public void setMachine(Machine machine) { this.machine = machine; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }
    public void setVibration(Double vibration) { this.vibration = vibration; }
    public void setRuntime(Double runtime) { this.runtime = runtime; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MachineData)) return false;
        MachineData that = (MachineData) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
