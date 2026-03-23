package com.gmao.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "task_history")
public class TaskHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    private User technician;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public TaskHistory() {}

    public TaskHistory(Task task, User technician, String notes) {
        this.task = task;
        this.technician = technician;
        this.notes = notes;
        this.completedAt = LocalDateTime.now();
    }

    // -------- Getters --------

    public Long getId() { return id; }

    public Task getTask() { return task; }

    public User getTechnician() { return technician; }

    public String getNotes() { return notes; }

    public LocalDateTime getCompletedAt() { return completedAt; }

    // -------- Setters --------

    public void setId(Long id) { this.id = id; }

    public void setTask(Task task) { this.task = task; }

    public void setTechnician(User technician) { this.technician = technician; }

    public void setNotes(String notes) { this.notes = notes; }

    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    // -------- equals & hashCode --------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskHistory)) return false;
        TaskHistory that = (TaskHistory) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}