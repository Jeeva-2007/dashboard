package com.monitoring.master.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "task_logs")
public class TaskLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    private String nodeName;
    private String action; // e.g. "POLL /telemetry", "TEST_CONN", "REGISTER"
    private String status; // "OK", "ERROR", "TIMEOUT"
    private Integer httpStatus; // 200, 500, etc.
    private Long latencyMs;
    private String message;

    public TaskLogEntry() {}

    public TaskLogEntry(String nodeName, String action, String status, Integer httpStatus, Long latencyMs, String message) {
        this.timestamp = LocalDateTime.now();
        this.nodeName = nodeName;
        this.action = action;
        this.status = status;
        this.httpStatus = httpStatus;
        this.latencyMs = latencyMs;
        this.message = message;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getNodeName() { return nodeName; }
    public void setNodeName(String nodeName) { this.nodeName = nodeName; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }

    public Long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Long latencyMs) { this.latencyMs = latencyMs; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
