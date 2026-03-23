package com.gmao.repository;

import com.gmao.entity.MachineData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface MachineDataRepository extends JpaRepository<MachineData, Long> {

    List<MachineData> findByMachineIdOrderByCreatedAtDesc(Long machineId);

    Optional<MachineData> findFirstByMachineIdOrderByCreatedAtDesc(Long machineId);

    @Query("SELECT md FROM MachineData md WHERE md.machine.id = :machineId ORDER BY md.createdAt DESC")
    List<MachineData> findLatestByMachineId(Long machineId);

    @Query("SELECT md FROM MachineData md WHERE md.temperature > :threshold ORDER BY md.createdAt DESC")
    List<MachineData> findByTemperatureGreaterThan(Double threshold);

    @Query("SELECT md FROM MachineData md WHERE md.vibration > :threshold ORDER BY md.createdAt DESC")
    List<MachineData> findByVibrationGreaterThan(Double threshold);

    // ✅ ADD THIS METHOD
    @Transactional
    void deleteByMachineId(Long machineId);
}