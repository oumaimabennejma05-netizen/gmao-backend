package com.gmao.dto;

import com.gmao.enums.MachineStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MachineDTO {

    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    private String model;
    private String location;

    @NotNull(message = "Status is required")
    private MachineStatus status;

    private LocalDate maintenanceDate;
    private LocalDateTime createdAt;

    public MachineDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public MachineStatus getStatus() { return status; }
    public void setStatus(MachineStatus status) { this.status = status; }
    public LocalDate getMaintenanceDate() { return maintenanceDate; }
    public void setMaintenanceDate(LocalDate maintenanceDate) { this.maintenanceDate = maintenanceDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
