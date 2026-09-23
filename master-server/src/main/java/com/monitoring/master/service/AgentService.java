package com.monitoring.master.service;

import com.monitoring.master.dto.AddAgentRequest;
import com.monitoring.master.dto.ClusterOverviewDto;
import com.monitoring.master.model.AgentNode;
import com.monitoring.master.model.TaskLogEntry;
import com.monitoring.master.model.TelemetrySnapshot;
import com.monitoring.master.repository.AgentNodeRepository;
import com.monitoring.master.repository.TaskLogRepository;
import com.monitoring.master.repository.TelemetrySnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
public class AgentService {

    private final AgentNodeRepository agentNodeRepository;
    private final TelemetrySnapshotRepository snapshotRepository;
    private final TaskLogRepository taskLogRepository;
    private final TelemetryPollingService pollingService;
    private final HttpClient httpClient;

    public AgentService(AgentNodeRepository agentNodeRepository,
                        TelemetrySnapshotRepository snapshotRepository,
                        TaskLogRepository taskLogRepository,
                        TelemetryPollingService pollingService) {
        this.agentNodeRepository = agentNodeRepository;
        this.snapshotRepository = snapshotRepository;
        this.taskLogRepository = taskLogRepository;
        this.pollingService = pollingService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    public List<AgentNode> getAllAgents() {
        return agentNodeRepository.findAll();
    }

    public Optional<AgentNode> getAgentById(Long id) {
        return agentNodeRepository.findById(id);
    }

    @Transactional
    public AgentNode addAgent(AddAgentRequest req) {
        // Create new node
        AgentNode node = new AgentNode(
                req.getName() != null && !req.getName().isBlank() ? req.getName().trim() : "Node-" + System.currentTimeMillis() % 1000,
                req.getDescription() != null ? req.getDescription().trim() : "",
                req.getIp() != null && !req.getIp().isBlank() ? req.getIp().trim() : "127.0.0.1",
                req.getPort() > 0 ? req.getPort() : 8001
        );

        if (req.getPollingIntervalSeconds() > 0) {
            node.setPollingIntervalSeconds(req.getPollingIntervalSeconds());
        }

        AgentNode saved = agentNodeRepository.save(node);

        taskLogRepository.save(new TaskLogEntry(
                saved.getName(),
                "REGISTER_NODE",
                "OK",
                201,
                0L,
                "Registered agent at " + saved.getBaseUrl()
        ));

        // Immediately poll to populate specs and telemetry right away
        try {
            pollingService.pollNode(saved);
        } catch (Exception ignored) {}

        return agentNodeRepository.findById(saved.getId()).orElse(saved);
    }

    @Transactional
    public Optional<AgentNode> updateAgent(Long id, AddAgentRequest req) {
        return agentNodeRepository.findById(id).map(node -> {
            if (req.getName() != null && !req.getName().isBlank()) {
                node.setName(req.getName().trim());
            }
            if (req.getDescription() != null) {
                node.setDescription(req.getDescription().trim());
            }
            if (req.getIp() != null && !req.getIp().isBlank()) {
                node.setIp(req.getIp().trim());
            }
            if (req.getPort() > 0) {
                node.setPort(req.getPort());
            }
            if (req.getPollingIntervalSeconds() > 0) {
                node.setPollingIntervalSeconds(req.getPollingIntervalSeconds());
            }
            AgentNode updated = agentNodeRepository.save(node);
            taskLogRepository.save(new TaskLogEntry(
                    updated.getName(),
                    "UPDATE_NODE",
                    "OK",
                    200,
                    0L,
                    "Updated node settings"
            ));
            return updated;
        });
    }

    @Transactional
    public boolean deleteAgent(Long id) {
        return agentNodeRepository.findById(id).map(node -> {
            String name = node.getName();
            snapshotRepository.deleteByAgentNodeId(id);
            agentNodeRepository.delete(node);

            taskLogRepository.save(new TaskLogEntry(
                    name,
                    "DELETE_NODE",
                    "OK",
                    200,
                    0L,
                    "Removed node and historical snapshots"
            ));
            return true;
        }).orElse(false);
    }

    public Map<String, Object> testConnection(String ip, int port) {
        Map<String, Object> result = new HashMap<>();
        String url = "http://" + ip + ":" + port + "/health";
        long start = System.currentTimeMillis();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(2))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long latencyMs = System.currentTimeMillis() - start;

            boolean success = response.statusCode() == 200;
            result.put("success", success);
            result.put("httpStatus", response.statusCode());
            result.put("latencyMs", latencyMs);
            result.put("message", success ? "Connection successful! Agent is active." : "Agent replied with HTTP " + response.statusCode());
        } catch (Exception e) {
            long latencyMs = System.currentTimeMillis() - start;
            result.put("success", false);
            result.put("httpStatus", 0);
            result.put("latencyMs", latencyMs);
            result.put("message", "Failed to connect: " + e.getMessage());
        }
        return result;
    }

    public ClusterOverviewDto getClusterOverview() {
        List<AgentNode> all = agentNodeRepository.findAll();
        int total = all.size();
        int online = 0;
        int offline = 0;
        double sumCpu = 0;
        double sumRam = 0;
        int activeCount = 0;

        for (AgentNode n : all) {
            if ("ONLINE".equalsIgnoreCase(n.getStatus())) {
                online++;
                if (n.getCpuUsagePercent() != null) {
                    sumCpu += n.getCpuUsagePercent();
                }
                if (n.getMemoryUsagePercent() != null) {
                    sumRam += n.getMemoryUsagePercent();
                }
                activeCount++;
            } else {
                offline++;
            }
        }

        double avgCpu = activeCount > 0 ? (sumCpu / activeCount) : 0.0;
        double avgRam = activeCount > 0 ? (sumRam / activeCount) : 0.0;

        return new ClusterOverviewDto(
                total,
                online,
                offline,
                Math.round(avgCpu * 10.0) / 10.0,
                Math.round(avgRam * 10.0) / 10.0,
                pollingService.getPollingIntervalSeconds()
        );
    }

    public List<TelemetrySnapshot> getAgentHistory(Long id) {
        List<TelemetrySnapshot> list = snapshotRepository.findTop40ByAgentNodeIdOrderByRecordedAtDesc(id);
        Collections.reverse(list); // Chronological order
        return list;
    }
}
