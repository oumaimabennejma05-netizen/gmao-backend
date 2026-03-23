package com.gmao.repository;

import com.gmao.entity.Machine;
import com.gmao.enums.MachineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MachineRepository extends JpaRepository<Machine, Long> {
    List<Machine> findByStatus(MachineStatus status);
    List<Machine> findByLocationContainingIgnoreCase(String location);
    long countByStatus(MachineStatus status);
}
