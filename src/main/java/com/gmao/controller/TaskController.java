package com.gmao.controller;

import com.gmao.dto.ApiResponse;
import com.gmao.dto.TaskDTO;
import com.gmao.dto.TaskHistoryDTO;
import com.gmao.entity.User;
import com.gmao.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskDTO>>> getAllTasks(@AuthenticationPrincipal User currentUser) {
        List<TaskDTO> tasks;
        // Technicians only see their own tasks
        if (currentUser.getRole().name().equals("TECHNICIAN")) {
            tasks = taskService.getTasksByTechnician(currentUser.getId());
        } else {
            tasks = taskService.getAllTasks();
        }
        return ResponseEntity.ok(ApiResponse.ok(tasks));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskDTO>> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(taskService.getTaskById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RESPONSABLE','ADMIN')")
    public ResponseEntity<ApiResponse<TaskDTO>> createTask(@Valid @RequestBody TaskDTO dto) {
        TaskDTO created = taskService.createTask(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Task created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskDTO>> updateTask(@PathVariable Long id, @RequestBody TaskDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok("Task updated", taskService.updateTask(id, dto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.ok("Task deleted", null));
    }

    @GetMapping("/technician/{id}")
    public ResponseEntity<ApiResponse<List<TaskDTO>>> getTasksByTechnician(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(taskService.getTasksByTechnician(id)));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<TaskHistoryDTO>>> getTaskHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(taskService.getTaskHistory(id)));
    }

    @PostMapping("/{id}/history")
    public ResponseEntity<ApiResponse<TaskHistoryDTO>> addHistory(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal User currentUser) {
        String notes = (String) body.getOrDefault("notes", "");
        Long techId = currentUser.getId();
        TaskHistoryDTO saved = taskService.addTaskHistory(id, techId, notes);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("History recorded", saved));
    }

    @GetMapping("/history/technician/{technicianId}")
    public ResponseEntity<ApiResponse<List<TaskHistoryDTO>>> getTechnicianHistory(@PathVariable Long technicianId) {
        return ResponseEntity.ok(ApiResponse.ok(taskService.getTechnicianHistory(technicianId)));
    }

    /**
     * Approve a task — RESPONSABLE only.
     * Moves task from PENDING_APPROVAL → APPROVED and restores machine to OPERATIONAL.
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('RESPONSABLE')")
    public ResponseEntity<ApiResponse<TaskDTO>> approveTask(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal User currentUser) {
        String notes = body != null ? body.getOrDefault("notes", "") : "";
        TaskDTO approved = taskService.approveTask(id, notes, currentUser);
        return ResponseEntity.ok(ApiResponse.ok("Task approved successfully", approved));
    }
}