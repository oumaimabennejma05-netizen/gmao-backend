package com.gmao.service;

import com.gmao.dto.TaskDTO;
import com.gmao.dto.TaskHistoryDTO;
import com.gmao.entity.Machine;
import com.gmao.entity.Task;
import com.gmao.entity.TaskHistory;
import com.gmao.entity.User;
import com.gmao.enums.MachineStatus;
import com.gmao.enums.TaskPriority;
import com.gmao.enums.TaskStatus;
import com.gmao.exception.BusinessException;
import com.gmao.exception.ResourceNotFoundException;
import com.gmao.repository.MachineRepository;
import com.gmao.repository.TaskHistoryRepository;
import com.gmao.repository.TaskRepository;
import com.gmao.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TaskService {

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final TaskHistoryRepository taskHistoryRepository;
    private final MachineRepository machineRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository,
                       TaskHistoryRepository taskHistoryRepository,
                       MachineRepository machineRepository,
                       UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.taskHistoryRepository = taskHistoryRepository;
        this.machineRepository = machineRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
        return toDTO(task);
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByTechnician(Long technicianId) {
        return taskRepository.findByTechnicianId(technicianId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Create a task. If the task has a machine and priority is MAINTENANCE or CRITICAL,
     * automatically set the machine status to MAINTENANCE.
     */
    public TaskDTO createTask(TaskDTO dto) {
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setPriority(dto.getPriority() != null ? dto.getPriority() : TaskPriority.MEDIUM);
        task.setStatus(TaskStatus.PENDING);
        task.setDueDate(dto.getDueDate());

        Machine machine = null;
        if (dto.getMachineId() != null) {
            machine = machineRepository.findById(dto.getMachineId())
                    .orElseThrow(() -> new ResourceNotFoundException("Machine", dto.getMachineId()));
            task.setMachine(machine);
        }

        if (dto.getTechnicianId() != null) {
            User technician = userRepository.findById(dto.getTechnicianId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", dto.getTechnicianId()));
            task.setTechnician(technician);
        }

        Task saved = taskRepository.save(task);

        // Auto-update machine status when a high/critical task is created
        if (machine != null) {
            TaskPriority priority = saved.getPriority();
            if ((priority == TaskPriority.HIGH || priority == TaskPriority.CRITICAL)
                    && machine.getStatus() == MachineStatus.OPERATIONAL) {
                machine.setStatus(MachineStatus.MAINTENANCE);
                machineRepository.save(machine);
                logger.info("Machine '{}' status changed to MAINTENANCE due to task '{}'",
                        machine.getName(), saved.getTitle());
            }
        }

        logger.info("Created task: {}", saved.getTitle());
        return toDTO(saved);
    }

    /**
     * Update a task.
     * - If technician sets status to COMPLETED → redirect to PENDING_APPROVAL
     * - History is recorded when COMPLETED → PENDING_APPROVAL
     */
    public TaskDTO updateTask(Long id, TaskDTO dto) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));

        TaskStatus previousStatus = task.getStatus();

        if (dto.getTitle() != null) task.setTitle(dto.getTitle());
        if (dto.getDescription() != null) task.setDescription(dto.getDescription());
        if (dto.getPriority() != null) task.setPriority(dto.getPriority());
        if (dto.getDueDate() != null) task.setDueDate(dto.getDueDate());

        // Handle machine assignment change
        if (dto.getMachineId() != null && (task.getMachine() == null ||
                !task.getMachine().getId().equals(dto.getMachineId()))) {
            Machine machine = machineRepository.findById(dto.getMachineId())
                    .orElseThrow(() -> new ResourceNotFoundException("Machine", dto.getMachineId()));
            task.setMachine(machine);
        }

        // Handle technician assignment
        if (dto.getTechnicianId() != null && (task.getTechnician() == null ||
                !task.getTechnician().getId().equals(dto.getTechnicianId()))) {
            User technician = userRepository.findById(dto.getTechnicianId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", dto.getTechnicianId()));
            task.setTechnician(technician);
        }

        // Status transition logic
        if (dto.getStatus() != null) {
            TaskStatus requestedStatus = dto.getStatus();

            if (requestedStatus == TaskStatus.COMPLETED
                    && previousStatus != TaskStatus.COMPLETED
                    && previousStatus != TaskStatus.PENDING_APPROVAL
                    && previousStatus != TaskStatus.APPROVED) {
                // Technician marks complete → redirect to PENDING_APPROVAL
                task.setStatus(TaskStatus.PENDING_APPROVAL);
                logger.info("Task '{}' moved to PENDING_APPROVAL (awaiting responsable review)", task.getTitle());

                // Record in history
                TaskHistory history = new TaskHistory(task, task.getTechnician(), "Task submitted for approval");
                taskHistoryRepository.save(history);

            } else if (requestedStatus == TaskStatus.IN_PROGRESS
                    || requestedStatus == TaskStatus.PENDING
                    || requestedStatus == TaskStatus.CANCELLED) {
                task.setStatus(requestedStatus);
            } else {
                task.setStatus(requestedStatus);
            }
        }

        return toDTO(taskRepository.save(task));
    }

    /**
     * Approve a task (RESPONSABLE only).
     * Sets task to APPROVED and returns the associated machine to OPERATIONAL.
     */
    public TaskDTO approveTask(Long id, String notes, User approver) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));

        if (task.getStatus() != TaskStatus.PENDING_APPROVAL) {
            throw new BusinessException("Task is not in PENDING_APPROVAL state. Current status: " + task.getStatus());
        }

        task.setStatus(TaskStatus.APPROVED);

        // Return machine to OPERATIONAL
        if (task.getMachine() != null) {
            Machine machine = task.getMachine();
            machine.setStatus(MachineStatus.OPERATIONAL);
            machineRepository.save(machine);
            logger.info("Machine '{}' returned to OPERATIONAL after task approval", machine.getName());
        }

        // Record approval in history
        String approvalNotes = (notes != null && !notes.isBlank()) ? notes : "Task approved by responsable";
        TaskHistory history = new TaskHistory(task, approver, approvalNotes);
        history.setCompletedAt(LocalDateTime.now());
        taskHistoryRepository.save(history);

        logger.info("Task '{}' approved by {}", task.getTitle(), approver.getEmail());
        return toDTO(taskRepository.save(task));
    }

    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task", id);
        }
        taskRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<TaskHistoryDTO> getTaskHistory(Long taskId) {
        return taskHistoryRepository.findByTaskId(taskId).stream()
                .map(this::toHistoryDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskHistoryDTO> getTechnicianHistory(Long technicianId) {
        return taskHistoryRepository.findByTechnicianIdOrderByCompletedAtDesc(technicianId).stream()
                .map(this::toHistoryDTO)
                .collect(Collectors.toList());
    }

    public TaskHistoryDTO addTaskHistory(Long taskId, Long technicianId, String notes) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
        User technician = userRepository.findById(technicianId)
                .orElseThrow(() -> new ResourceNotFoundException("User", technicianId));

        TaskHistory history = new TaskHistory(task, technician, notes);
        history.setCompletedAt(LocalDateTime.now());
        return toHistoryDTO(taskHistoryRepository.save(history));
    }

    public TaskDTO toDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setPriority(task.getPriority());
        dto.setStatus(task.getStatus());
        dto.setDueDate(task.getDueDate());
        dto.setCreatedAt(task.getCreatedAt());

        if (task.getMachine() != null) {
            dto.setMachineId(task.getMachine().getId());
            dto.setMachineName(task.getMachine().getName());
        }
        if (task.getTechnician() != null) {
            dto.setTechnicianId(task.getTechnician().getId());
            dto.setTechnicianName(task.getTechnician().getName());
        }
        return dto;
    }

    public TaskHistoryDTO toHistoryDTO(TaskHistory h) {
        TaskHistoryDTO dto = new TaskHistoryDTO();
        dto.setId(h.getId());
        dto.setNotes(h.getNotes());
        dto.setCompletedAt(h.getCompletedAt());
        if (h.getTask() != null) {
            dto.setTaskId(h.getTask().getId());
            dto.setTaskTitle(h.getTask().getTitle());
        }
        if (h.getTechnician() != null) {
            dto.setTechnicianId(h.getTechnician().getId());
            dto.setTechnicianName(h.getTechnician().getName());
        }
        return dto;
    }
}