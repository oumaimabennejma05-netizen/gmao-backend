package com.gmao.repository;

import com.gmao.entity.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {
    List<TaskHistory> findByTaskId(Long taskId);
    List<TaskHistory> findByTechnicianId(Long technicianId);
    List<TaskHistory> findByTechnicianIdOrderByCompletedAtDesc(Long technicianId);
}
