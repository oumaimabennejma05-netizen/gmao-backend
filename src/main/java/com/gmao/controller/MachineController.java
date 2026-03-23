package com.gmao.controller;

import com.gmao.dto.ApiResponse;
import com.gmao.dto.MachineDTO;
import com.gmao.dto.MachineDataDTO;
import com.gmao.service.MachineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/machines")
public class MachineController {

    private final MachineService machineService;

    public MachineController(MachineService machineService) {
        this.machineService = machineService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MachineDTO>>> getAllMachines() {
        return ResponseEntity.ok(ApiResponse.ok(machineService.getAllMachines()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MachineDTO>> getMachineById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(machineService.getMachineById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MachineDTO>> createMachine(@Valid @RequestBody MachineDTO dto) {
        MachineDTO created = machineService.createMachine(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Machine created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MachineDTO>> updateMachine(@PathVariable Long id, @Valid @RequestBody MachineDTO dto) {
        return ResponseEntity.ok(ApiResponse.ok("Machine updated", machineService.updateMachine(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteMachine(@PathVariable Long id) {
        machineService.deleteMachine(id);
        return ResponseEntity.ok(ApiResponse.ok("Machine deleted", null));
    }

    @PostMapping("/{id}/data")
    public ResponseEntity<ApiResponse<MachineDataDTO>> addMachineData(
            @PathVariable Long id,
            @RequestBody MachineDataDTO dto) {
        MachineDataDTO saved = machineService.addMachineData(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Telemetry data saved", saved));
    }

    @GetMapping("/{id}/data")
    public ResponseEntity<ApiResponse<List<MachineDataDTO>>> getMachineData(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(machineService.getMachineData(id)));
    }
}
