package com.gmao.repository;

import com.gmao.entity.Task;
import com.gmao.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByTechnicianId(Long technicianId);

    List<Task> findByMachineId(Long machineId);

    List<Task> findByStatus(TaskStatus status);

    long countByStatus(TaskStatus status);

    @Query("SELECT t FROM Task t WHERE t.technician.id = :technicianId AND t.status = :status")
    List<Task> findByTechnicianIdAndStatus(Long technicianId, TaskStatus status);

    // ✅ ADD THIS METHOD
    @Transactional
    void deleteByMachineId(Long machineId);
}