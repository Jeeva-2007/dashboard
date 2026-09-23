package com.monitoring.master.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "agent_nodes")
public class AgentNode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_id")
    private String agentId;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private String ip;

    @Column(nullable = false)
    private int port;

    @Column(name = "polling_interval_seconds")
    private int pollingIntervalSeconds = 5;

    @Column(nullable = false)
    private String status = "PENDING"; // ONLINE, OFFLINE, PENDING

    @Column(name = "last_latency_ms")
    private Long lastLatencyMs = 0L;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Hardware & System details reported by agent
    private String hostname;
    private String os;
    private String osVersion;
    private String platform;
    private String architecture;
    private String processor;

    private Integer physicalCores;
    private Integer logicalCores;

    // Latest Telemetry Metrics
    private Double cpuUsagePercent;
    private Double memoryUsagePercent;
    private Long memoryUsedBytes;
    private Long memoryTotalBytes;
    private Long memoryAvailableBytes;

    private Double diskUsagePercent;
    private Long diskUsedBytes;
    private Long diskTotalBytes;
    private Long diskFreeBytes;

    private Long networkBytesSent;
    private Long networkBytesReceived;
    private Long networkPacketsSent;
    private Long networkPacketsReceived;

    private Long uptimeSeconds;
    private String bootTime;

    public AgentNode() {}

    public AgentNode(String name, String description, String ip, int port) {
        this.name = name;
        this.description = description;
        this.ip = ip;
        this.port = port;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public String getBaseUrl() {
        return "http://" + ip + ":" + port;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public int getPollingIntervalSeconds() { return pollingIntervalSeconds; }
    public void setPollingIntervalSeconds(int pollingIntervalSeconds) { this.pollingIntervalSeconds = pollingIntervalSeconds; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getLastLatencyMs() { return lastLatencyMs; }
    public void setLastLatencyMs(Long lastLatencyMs) { this.lastLatencyMs = lastLatencyMs; }

    public LocalDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getHostname() { return hostname; }
    public void setHostname(String hostname) { this.hostname = hostname; }

    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }

    public String getOsVersion() { return osVersion; }
    public void setOsVersion(String osVersion) { this.osVersion = osVersion; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public String getArchitecture() { return architecture; }
    public void setArchitecture(String architecture) { this.architecture = architecture; }

    public String getProcessor() { return processor; }
    public void setProcessor(String processor) { this.processor = processor; }

    public Integer getPhysicalCores() { return physicalCores; }
    public void setPhysicalCores(Integer physicalCores) { this.physicalCores = physicalCores; }

    public Integer getLogicalCores() { return logicalCores; }
    public void setLogicalCores(Integer logicalCores) { this.logicalCores = logicalCores; }

    public Double getCpuUsagePercent() { return cpuUsagePercent; }
    public void setCpuUsagePercent(Double cpuUsagePercent) { this.cpuUsagePercent = cpuUsagePercent; }

    public Double getMemoryUsagePercent() { return memoryUsagePercent; }
    public void setMemoryUsagePercent(Double memoryUsagePercent) { this.memoryUsagePercent = memoryUsagePercent; }

    public Long getMemoryUsedBytes() { return memoryUsedBytes; }
    public void setMemoryUsedBytes(Long memoryUsedBytes) { this.memoryUsedBytes = memoryUsedBytes; }

    public Long getMemoryTotalBytes() { return memoryTotalBytes; }
    public void setMemoryTotalBytes(Long memoryTotalBytes) { this.memoryTotalBytes = memoryTotalBytes; }

    public Long getMemoryAvailableBytes() { return memoryAvailableBytes; }
    public void setMemoryAvailableBytes(Long memoryAvailableBytes) { this.memoryAvailableBytes = memoryAvailableBytes; }

    public Double getDiskUsagePercent() { return diskUsagePercent; }
    public void setDiskUsagePercent(Double diskUsagePercent) { this.diskUsagePercent = diskUsagePercent; }

    public Long getDiskUsedBytes() { return diskUsedBytes; }
    public void setDiskUsedBytes(Long diskUsedBytes) { this.diskUsedBytes = diskUsedBytes; }

    public Long getDiskTotalBytes() { return diskTotalBytes; }
    public void setDiskTotalBytes(Long diskTotalBytes) { this.diskTotalBytes = diskTotalBytes; }

    public Long getDiskFreeBytes() { return diskFreeBytes; }
    public void setDiskFreeBytes(Long diskFreeBytes) { this.diskFreeBytes = diskFreeBytes; }

    public Long getNetworkBytesSent() { return networkBytesSent; }
    public void setNetworkBytesSent(Long networkBytesSent) { this.networkBytesSent = networkBytesSent; }

    public Long getNetworkBytesReceived() { return networkBytesReceived; }
    public void setNetworkBytesReceived(Long networkBytesReceived) { this.networkBytesReceived = networkBytesReceived; }

    public Long getNetworkPacketsSent() { return networkPacketsSent; }
    public void setNetworkPacketsSent(Long networkPacketsSent) { this.networkPacketsSent = networkPacketsSent; }

    public Long getNetworkPacketsReceived() { return networkPacketsReceived; }
    public void setNetworkPacketsReceived(Long networkPacketsReceived) { this.networkPacketsReceived = networkPacketsReceived; }

    public Long getUptimeSeconds() { return uptimeSeconds; }
    public void setUptimeSeconds(Long uptimeSeconds) { this.uptimeSeconds = uptimeSeconds; }

    public String getBootTime() { return bootTime; }
    public void setBootTime(String bootTime) { this.bootTime = bootTime; }
}
