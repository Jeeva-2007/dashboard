package com.monitoring.master.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.monitoring.master.dto.SlaveTelemetryDto;
import com.monitoring.master.model.AgentNode;
import com.monitoring.master.model.TaskLogEntry;
import com.monitoring.master.model.TelemetrySnapshot;
import com.monitoring.master.repository.AgentNodeRepository;
import com.monitoring.master.repository.TaskLogRepository;
import com.monitoring.master.repository.TelemetrySnapshotRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TelemetryPollingService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryPollingService.class);

    private final AgentNodeRepository agentNodeRepository;
    private final TelemetrySnapshotRepository snapshotRepository;
    private final TaskLogRepository taskLogRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    // Polling interval in seconds (default 5, 0 = paused)
    private final AtomicInteger pollingIntervalSeconds;
    private long lastPollTimeMillis = 0;

    public TelemetryPollingService(AgentNodeRepository agentNodeRepository,
                                   TelemetrySnapshotRepository snapshotRepository,
                                   TaskLogRepository taskLogRepository,
                                   ObjectMapper objectMapper,
                                   @Value("${master.polling.default-interval-seconds:5}") int defaultInterval) {
        this.agentNodeRepository = agentNodeRepository;
        this.snapshotRepository = snapshotRepository;
        this.taskLogRepository = taskLogRepository;
        this.objectMapper = objectMapper;
        this.pollingIntervalSeconds = new AtomicInteger(defaultInterval);

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
    }

    public int getPollingIntervalSeconds() {
        return pollingIntervalSeconds.get();
    }

    public void setPollingIntervalSeconds(int intervalSeconds) {
        this.pollingIntervalSeconds.set(intervalSeconds);
        log.info("Polling interval changed to {}s", intervalSeconds);
        taskLogRepository.save(new TaskLogEntry("Cluster", "CONFIG_POLL_INTERVAL", "OK", 200, 0L,
                "Polling interval updated to " + (intervalSeconds == 0 ? "PAUSED" : intervalSeconds + "s")));
    }

    /**
     * Runs every 1 second and checks if the configured interval has elapsed.
     */
    @Scheduled(fixedDelay = 1000)
    public void scheduledPollLoop() {
        int interval = pollingIntervalSeconds.get();
        if (interval <= 0) {
            // Paused
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastPollTimeMillis < (interval * 1000L)) {
            return;
        }
        lastPollTimeMillis = now;

        List<AgentNode> nodes = agentNodeRepository.findAll();
        if (nodes.isEmpty()) {
            return;
        }

        for (AgentNode node : nodes) {
            try {
                pollNode(node);
            } catch (Exception e) {
                log.error("Error polling node {}: {}", node.getName(), e.getMessage());
            }
        }
    }

    /**
     * Poll a specific node immediately and update its telemetry.
     */
    public boolean pollNode(AgentNode node) {
        String targetUrl = node.getBaseUrl() + "/telemetry";
        long startTime = System.currentTimeMillis();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long latencyMs = System.currentTimeMillis() - startTime;

            if (response.statusCode() == 200) {
                SlaveTelemetryDto telemetry = objectMapper.readValue(response.body(), SlaveTelemetryDto.class);

                // Update node entity
                node.setStatus("ONLINE");
                node.setLastSeen(LocalDateTime.now());
                node.setLastLatencyMs(latencyMs);

                if (telemetry.getAgentId() != null) {
                    node.setAgentId(telemetry.getAgentId());
                }

                if (telemetry.getSystem() != null) {
                    node.setHostname(telemetry.getSystem().getHostname());
                    node.setOs(telemetry.getSystem().getOs());
                    node.setOsVersion(telemetry.getSystem().getOsVersion());
                    node.setPlatform(telemetry.getSystem().getPlatform());
                    node.setArchitecture(telemetry.getSystem().getArchitecture());
                    node.setProcessor(telemetry.getSystem().getProcessor());
                }

                if (telemetry.getCpu() != null) {
                    node.setCpuUsagePercent(telemetry.getCpu().getUsagePercent());
                    node.setPhysicalCores(telemetry.getCpu().getPhysicalCores());
                    node.setLogicalCores(telemetry.getCpu().getLogicalCores());
                }

                if (telemetry.getMemory() != null) {
                    node.setMemoryUsagePercent(telemetry.getMemory().getUsagePercent());
                    node.setMemoryUsedBytes(telemetry.getMemory().getUsedBytes());
                    node.setMemoryTotalBytes(telemetry.getMemory().getTotalBytes());
                    node.setMemoryAvailableBytes(telemetry.getMemory().getAvailableBytes());
                }

                if (telemetry.getDisk() != null) {
                    node.setDiskUsagePercent(telemetry.getDisk().getUsagePercent());
                    node.setDiskUsedBytes(telemetry.getDisk().getUsedBytes());
                    node.setDiskTotalBytes(telemetry.getDisk().getTotalBytes());
                    node.setDiskFreeBytes(telemetry.getDisk().getFreeBytes());
                }

                if (telemetry.getNetwork() != null) {
                    node.setNetworkBytesSent(telemetry.getNetwork().getBytesSent());
                    node.setNetworkBytesReceived(telemetry.getNetwork().getBytesReceived());
                    node.setNetworkPacketsSent(telemetry.getNetwork().getPacketsSent());
                    node.setNetworkPacketsReceived(telemetry.getNetwork().getPacketsReceived());
                }

                if (telemetry.getUptime() != null) {
                    node.setUptimeSeconds(telemetry.getUptime().getUptimeSeconds());
                    node.setBootTime(telemetry.getUptime().getBootTime());
                }

                agentNodeRepository.save(node);

                // Save snapshot
                TelemetrySnapshot snapshot = new TelemetrySnapshot(
                        node.getId(),
                        node.getCpuUsagePercent(),
                        node.getMemoryUsagePercent(),
                        node.getMemoryUsedBytes(),
                        node.getMemoryTotalBytes(),
                        node.getDiskUsagePercent(),
                        node.getDiskUsedBytes(),
                        node.getDiskTotalBytes(),
                        node.getNetworkBytesSent(),
                        node.getNetworkBytesReceived(),
                        latencyMs,
                        true
                );
                snapshotRepository.save(snapshot);

                // Add log entry
                taskLogRepository.save(new TaskLogEntry(
                        node.getName(),
                        "POLL /telemetry",
                        "OK",
                        200,
                        latencyMs,
                        String.format("CPU: %.1f%% | RAM: %.1f%% | Latency: %dms",
                                node.getCpuUsagePercent() != null ? node.getCpuUsagePercent() : 0.0,
                                node.getMemoryUsagePercent() != null ? node.getMemoryUsagePercent() : 0.0,
                                latencyMs)
                ));

                return true;
            } else {
                markNodeOffline(node, response.statusCode(), "HTTP " + response.statusCode(), latencyMs);
                return false;
            }
        } catch (Exception e) {
            long latencyMs = System.currentTimeMillis() - startTime;
            markNodeOffline(node, 0, e.getClass().getSimpleName() + ": " + e.getMessage(), latencyMs);
            return false;
        }
    }

    private void markNodeOffline(AgentNode node, int httpStatus, String errorMsg, long latencyMs) {
        node.setStatus("OFFLINE");
        node.setLastLatencyMs(latencyMs);
        agentNodeRepository.save(node);

        snapshotRepository.save(new TelemetrySnapshot(
                node.getId(), null, null, null, null, null, null, null, null, null, latencyMs, false
        ));

        taskLogRepository.save(new TaskLogEntry(
                node.getName(),
                "POLL /telemetry",
                "ERROR",
                httpStatus,
                latencyMs,
                "Node unreachable: " + errorMsg
        ));
    }
}
