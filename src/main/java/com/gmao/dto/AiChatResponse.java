package com.gmao.dto;

import java.time.LocalDateTime;

public class AiChatResponse {
    private String message;
    private String severity;   // INFO, WARNING, CRITICAL
    private LocalDateTime timestamp;

    public AiChatResponse() {}

    public AiChatResponse(String message, String severity) {
        this.message = message;
        this.severity = severity;
        this.timestamp = LocalDateTime.now();
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
