package com.gmao.controller;

import com.gmao.dto.AiChatRequest;
import com.gmao.dto.AiChatResponse;
import com.gmao.dto.AiPredictionDTO;
import com.gmao.dto.ApiResponse;
import com.gmao.service.AiService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/predictions")
    public ResponseEntity<ApiResponse<List<AiPredictionDTO>>> getAllPredictions() {
        return ResponseEntity.ok(ApiResponse.ok(aiService.getAllPredictions()));
    }

    @GetMapping("/predictions/{machineId}")
    public ResponseEntity<ApiResponse<AiPredictionDTO>> getPredictionForMachine(@PathVariable Long machineId) {
        return ResponseEntity.ok(ApiResponse.ok(aiService.getPredictionForMachine(machineId)));
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResponse<AiChatResponse>> chat(@Valid @RequestBody AiChatRequest request) {
        AiChatResponse response = aiService.chat(request.getMessage(), request.getMachineId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
