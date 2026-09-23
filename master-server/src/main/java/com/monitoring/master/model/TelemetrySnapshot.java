package com.monitoring.master.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "telemetry_snapshots", indexes = {
    @Index(name = "idx_agent_recorded", columnList = "agent_node_id, recorded_at")
})
public class TelemetrySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_node_id", nullable = false)
    private Long agentNodeId;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    private Double cpuUsagePercent;
    private Double memoryUsagePercent;
    private Long memoryUsedBytes;
    private Long memoryTotalBytes;

    private Double diskUsagePercent;
    private Long diskUsedBytes;
    private Long diskTotalBytes;

    private Long networkBytesSent;
    private Long networkBytesReceived;

    private Long latencyMs;
    private boolean reachable;

    public TelemetrySnapshot() {}

    public TelemetrySnapshot(Long agentNodeId, Double cpu, Double mem, Long memUsed, Long memTotal,
                             Double disk, Long diskUsed, Long diskTotal, Long netSent, Long netRecv,
                             Long latencyMs, boolean reachable) {
        this.agentNodeId = agentNodeId;
        this.recordedAt = LocalDateTime.now();
        this.cpuUsagePercent = cpu;
        this.memoryUsagePercent = mem;
        this.memoryUsedBytes = memUsed;
        this.memoryTotalBytes = memTotal;
        this.diskUsagePercent = disk;
        this.diskUsedBytes = diskUsed;
        this.diskTotalBytes = diskTotal;
        this.networkBytesSent = netSent;
        this.networkBytesReceived = netRecv;
        this.latencyMs = latencyMs;
        this.reachable = reachable;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAgentNodeId() { return agentNodeId; }
    public void setAgentNodeId(Long agentNodeId) { this.agentNodeId = agentNodeId; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }

    public Double getCpuUsagePercent() { return cpuUsagePercent; }
    public void setCpuUsagePercent(Double cpuUsagePercent) { this.cpuUsagePercent = cpuUsagePercent; }

    public Double getMemoryUsagePercent() { return memoryUsagePercent; }
    public void setMemoryUsagePercent(Double memoryUsagePercent) { this.memoryUsagePercent = memoryUsagePercent; }

    public Long getMemoryUsedBytes() { return memoryUsedBytes; }
    public void setMemoryUsedBytes(Long memoryUsedBytes) { this.memoryUsedBytes = memoryUsedBytes; }

    public Long getMemoryTotalBytes() { return memoryTotalBytes; }
    public void setMemoryTotalBytes(Long memoryTotalBytes) { this.memoryTotalBytes = memoryTotalBytes; }

    public Double getDiskUsagePercent() { return diskUsagePercent; }
    public void setDiskUsagePercent(Double diskUsagePercent) { this.diskUsagePercent = diskUsagePercent; }

    public Long getDiskUsedBytes() { return diskUsedBytes; }
    public void setDiskUsedBytes(Long diskUsedBytes) { this.diskUsedBytes = diskUsedBytes; }

    public Long getDiskTotalBytes() { return diskTotalBytes; }
    public void setDiskTotalBytes(Long diskTotalBytes) { this.diskTotalBytes = diskTotalBytes; }

    public Long getNetworkBytesSent() { return networkBytesSent; }
    public void setNetworkBytesSent(Long networkBytesSent) { this.networkBytesSent = networkBytesSent; }

    public Long getNetworkBytesReceived() { return networkBytesReceived; }
    public void setNetworkBytesReceived(Long networkBytesReceived) { this.networkBytesReceived = networkBytesReceived; }

    public Long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Long latencyMs) { this.latencyMs = latencyMs; }

    public boolean isReachable() { return reachable; }
    public void setReachable(boolean reachable) { this.reachable = reachable; }
}
