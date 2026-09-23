package com.monitoring.master.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SlaveTelemetryDto {

    @JsonProperty("agent_id")
    private String agentId;

    private String timestamp;
    private SystemInfo system;
    private CpuInfo cpu;
    private MemoryInfo memory;
    private DiskInfo disk;
    private NetworkInfo network;
    private UptimeInfo uptime;

    public SlaveTelemetryDto() {}

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public SystemInfo getSystem() { return system; }
    public void setSystem(SystemInfo system) { this.system = system; }

    public CpuInfo getCpu() { return cpu; }
    public void setCpu(CpuInfo cpu) { this.cpu = cpu; }

    public MemoryInfo getMemory() { return memory; }
    public void setMemory(MemoryInfo memory) { this.memory = memory; }

    public DiskInfo getDisk() { return disk; }
    public void setDisk(DiskInfo disk) { this.disk = disk; }

    public NetworkInfo getNetwork() { return network; }
    public void setNetwork(NetworkInfo network) { this.network = network; }

    public UptimeInfo getUptime() { return uptime; }
    public void setUptime(UptimeInfo uptime) { this.uptime = uptime; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SystemInfo {
        private String hostname;
        private String os;
        @JsonProperty("os_version")
        private String osVersion;
        private String platform;
        private String architecture;
        private String processor;

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
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CpuInfo {
        @JsonProperty("usage_percent")
        private Double usagePercent;
        @JsonProperty("physical_cores")
        private Integer physicalCores;
        @JsonProperty("logical_cores")
        private Integer logicalCores;

        public Double getUsagePercent() { return usagePercent; }
        public void setUsagePercent(Double usagePercent) { this.usagePercent = usagePercent; }

        public Integer getPhysicalCores() { return physicalCores; }
        public void setPhysicalCores(Integer physicalCores) { this.physicalCores = physicalCores; }

        public Integer getLogicalCores() { return logicalCores; }
        public void setLogicalCores(Integer logicalCores) { this.logicalCores = logicalCores; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MemoryInfo {
        @JsonProperty("total_bytes")
        private Long totalBytes;
        @JsonProperty("used_bytes")
        private Long usedBytes;
        @JsonProperty("available_bytes")
        private Long availableBytes;
        @JsonProperty("usage_percent")
        private Double usagePercent;

        public Long getTotalBytes() { return totalBytes; }
        public void setTotalBytes(Long totalBytes) { this.totalBytes = totalBytes; }

        public Long getUsedBytes() { return usedBytes; }
        public void setUsedBytes(Long usedBytes) { this.usedBytes = usedBytes; }

        public Long getAvailableBytes() { return availableBytes; }
        public void setAvailableBytes(Long availableBytes) { this.availableBytes = availableBytes; }

        public Double getUsagePercent() { return usagePercent; }
        public void setUsagePercent(Double usagePercent) { this.usagePercent = usagePercent; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DiskInfo {
        @JsonProperty("total_bytes")
        private Long totalBytes;
        @JsonProperty("used_bytes")
        private Long usedBytes;
        @JsonProperty("free_bytes")
        private Long freeBytes;
        @JsonProperty("usage_percent")
        private Double usagePercent;

        public Long getTotalBytes() { return totalBytes; }
        public void setTotalBytes(Long totalBytes) { this.totalBytes = totalBytes; }

        public Long getUsedBytes() { return usedBytes; }
        public void setUsedBytes(Long usedBytes) { this.usedBytes = usedBytes; }

        public Long getFreeBytes() { return freeBytes; }
        public void setFreeBytes(Long freeBytes) { this.freeBytes = freeBytes; }

        public Double getUsagePercent() { return usagePercent; }
        public void setUsagePercent(Double usagePercent) { this.usagePercent = usagePercent; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NetworkInfo {
        @JsonProperty("bytes_sent")
        private Long bytesSent;
        @JsonProperty("bytes_received")
        private Long bytesReceived;
        @JsonProperty("packets_sent")
        private Long packetsSent;
        @JsonProperty("packets_received")
        private Long packetsReceived;

        public Long getBytesSent() { return bytesSent; }
        public void setBytesSent(Long bytesSent) { this.bytesSent = bytesSent; }

        public Long getBytesReceived() { return bytesReceived; }
        public void setBytesReceived(Long bytesReceived) { this.bytesReceived = bytesReceived; }

        public Long getPacketsSent() { return packetsSent; }
        public void setPacketsSent(Long packetsSent) { this.packetsSent = packetsSent; }

        public Long getPacketsReceived() { return packetsReceived; }
        public void setPacketsReceived(Long packetsReceived) { this.packetsReceived = packetsReceived; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UptimeInfo {
        @JsonProperty("boot_time")
        private String bootTime;
        @JsonProperty("uptime_seconds")
        private Long uptimeSeconds;

        public String getBootTime() { return bootTime; }
        public void setBootTime(String bootTime) { this.bootTime = bootTime; }

        public Long getUptimeSeconds() { return uptimeSeconds; }
        public void setUptimeSeconds(Long uptimeSeconds) { this.uptimeSeconds = uptimeSeconds; }
    }
}
