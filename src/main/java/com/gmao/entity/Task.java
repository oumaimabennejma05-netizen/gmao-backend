package com.gmao.entity;

import com.gmao.enums.TaskPriority;
import com.gmao.enums.TaskStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id")
    private Machine machine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    private User technician;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(
            mappedBy = "task",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<TaskHistory> history = new ArrayList<>();

    public Task() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // -------- Relationship helpers --------

    public void addHistory(TaskHistory taskHistory) {
        history.add(taskHistory);
        taskHistory.setTask(this);
    }

    public void removeHistory(TaskHistory taskHistory) {
        history.remove(taskHistory);
        taskHistory.setTask(null);
    }

    // -------- Getters --------

    public Long getId() { return id; }

    public String getTitle() { return title; }

    public String getDescription() { return description; }

    public Machine getMachine() { return machine; }

    public User getTechnician() { return technician; }

    public TaskPriority getPriority() { return priority; }

    public TaskStatus getStatus() { return status; }

    public LocalDate getDueDate() { return dueDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public List<TaskHistory> getHistory() { return history; }

    // -------- Setters --------

    public void setId(Long id) { this.id = id; }

    public void setTitle(String title) { this.title = title; }

    public void setDescription(String description) { this.description = description; }

    public void setMachine(Machine machine) { this.machine = machine; }

    public void setTechnician(User technician) { this.technician = technician; }

    public void setPriority(TaskPriority priority) { this.priority = priority; }

    public void setStatus(TaskStatus status) { this.status = status; }

    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public void setHistory(List<TaskHistory> history) { this.history = history; }

    // -------- equals & hashCode --------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}