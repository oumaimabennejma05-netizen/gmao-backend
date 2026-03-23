package com.gmao.entity;

import com.gmao.enums.MachineStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "machines")
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    private String model;
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MachineStatus status = MachineStatus.OPERATIONAL;

    @Column(name = "maintenance_date")
    private LocalDate maintenanceDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Machine() {}

    public Machine(String name, String model, String location, MachineStatus status) {
        this.name = name;
        this.model = model;
        this.location = location;
        this.status = status;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // --- Getters ---
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getModel() { return model; }
    public String getLocation() { return location; }
    public MachineStatus getStatus() { return status; }
    public LocalDate getMaintenanceDate() { return maintenanceDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // --- Setters ---
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setModel(String model) { this.model = model; }
    public void setLocation(String location) { this.location = location; }
    public void setStatus(MachineStatus status) { this.status = status; }
    public void setMaintenanceDate(LocalDate maintenanceDate) { this.maintenanceDate = maintenanceDate; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Machine)) return false;
        Machine machine = (Machine) o;
        return Objects.equals(id, machine.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
