package com.gmao.service;

import com.gmao.dto.MachineDTO;
import com.gmao.dto.MachineDataDTO;
import com.gmao.entity.Machine;
import com.gmao.entity.MachineData;
import com.gmao.exception.ResourceNotFoundException;
import com.gmao.repository.MachineDataRepository;
import com.gmao.repository.MachineRepository;
import com.gmao.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MachineService {

    private static final Logger logger = LoggerFactory.getLogger(MachineService.class);

    private final MachineRepository machineRepository;
    private final MachineDataRepository machineDataRepository;
    private final TaskRepository taskRepository;

    public MachineService(MachineRepository machineRepository,
                          MachineDataRepository machineDataRepository,
                          TaskRepository taskRepository) {
        this.machineRepository = machineRepository;
        this.machineDataRepository = machineDataRepository;
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public List<MachineDTO> getAllMachines() {
        return machineRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MachineDTO getMachineById(Long id) {
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Machine", id));
        return toDTO(machine);
    }

    public MachineDTO createMachine(MachineDTO dto) {
        Machine machine = new Machine();
        mapToEntity(dto, machine);
        Machine saved = machineRepository.save(machine);
        logger.info("Created machine: {}", saved.getName());
        return toDTO(saved);
    }

    public MachineDTO updateMachine(Long id, MachineDTO dto) {
        Machine machine = machineRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Machine", id));
        mapToEntity(dto, machine);
        return toDTO(machineRepository.save(machine));
    }

    public void deleteMachine(Long id) {
        if (!machineRepository.existsById(id)) {
            throw new ResourceNotFoundException("Machine", id);
        }

        // delete tasks linked to machine
        taskRepository.deleteByMachineId(id);

        // delete telemetry data
        machineDataRepository.deleteByMachineId(id);

        // delete machine
        machineRepository.deleteById(id);

        logger.info("Deleted machine with id: {}", id);
    }

    public MachineDataDTO addMachineData(Long machineId, MachineDataDTO dto) {
        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new ResourceNotFoundException("Machine", machineId));

        MachineData data = new MachineData(machine, dto.getTemperature(), dto.getVibration(), dto.getRuntime());
        MachineData saved = machineDataRepository.save(data);
        return toDataDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<MachineDataDTO> getMachineData(Long machineId) {
        return machineDataRepository.findByMachineIdOrderByCreatedAtDesc(machineId).stream()
                .limit(50)
                .map(this::toDataDTO)
                .collect(Collectors.toList());
    }

    private void mapToEntity(MachineDTO dto, Machine machine) {
        machine.setName(dto.getName());
        machine.setModel(dto.getModel());
        machine.setLocation(dto.getLocation());
        machine.setStatus(dto.getStatus());
        machine.setMaintenanceDate(dto.getMaintenanceDate());
    }

    public MachineDTO toDTO(Machine machine) {
        MachineDTO dto = new MachineDTO();
        dto.setId(machine.getId());
        dto.setName(machine.getName());
        dto.setModel(machine.getModel());
        dto.setLocation(machine.getLocation());
        dto.setStatus(machine.getStatus());
        dto.setMaintenanceDate(machine.getMaintenanceDate());
        dto.setCreatedAt(machine.getCreatedAt());
        return dto;
    }

    public MachineDataDTO toDataDTO(MachineData data) {
        MachineDataDTO dto = new MachineDataDTO();
        dto.setId(data.getId());
        dto.setMachineId(data.getMachine().getId());
        dto.setMachineName(data.getMachine().getName());
        dto.setTemperature(data.getTemperature());
        dto.setVibration(data.getVibration());
        dto.setRuntime(data.getRuntime());
        dto.setCreatedAt(data.getCreatedAt());
        return dto;
    }
}