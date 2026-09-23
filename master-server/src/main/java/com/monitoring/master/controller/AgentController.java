package com.monitoring.master.controller;

import com.monitoring.master.dto.AddAgentRequest;
import com.monitoring.master.dto.ClusterOverviewDto;
import com.monitoring.master.model.AgentNode;
import com.monitoring.master.model.TelemetrySnapshot;
import com.monitoring.master.service.AgentService;
import com.monitoring.master.service.TelemetryPollingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agents")
@CrossOrigin(origins = "*")
public class AgentController {

    private final AgentService agentService;
    private final TelemetryPollingService pollingService;

    public AgentController(AgentService agentService, TelemetryPollingService pollingService) {
        this.agentService = agentService;
        this.pollingService = pollingService;
    }

    @GetMapping
    public List<AgentNode> listAll() {
        return agentService.getAllAgents();
    }

    @GetMapping("/overview")
    public ClusterOverviewDto getOverview() {
        return agentService.getClusterOverview();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgentNode> getById(@PathVariable Long id) {
        return agentService.getAgentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AgentNode> create(@RequestBody AddAgentRequest request) {
        AgentNode node = agentService.addAgent(request);
        return ResponseEntity.ok(node);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AgentNode> update(@PathVariable Long id, @RequestBody AddAgentRequest request) {
        return agentService.updateAgent(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = agentService.deleteAgent(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/poll")
    public ResponseEntity<AgentNode> pollNow(@PathVariable Long id) {
        return agentService.getAgentById(id).map(node -> {
            pollingService.pollNode(node);
            return ResponseEntity.ok(agentService.getAgentById(id).orElse(node));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<TelemetrySnapshot>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(agentService.getAgentHistory(id));
    }

    @PostMapping("/test-connection")
    public ResponseEntity<Map<String, Object>> testConnection(@RequestBody Map<String, Object> payload) {
        String ip = String.valueOf(payload.getOrDefault("ip", "127.0.0.1"));
        int port = 8001;
        try {
            port = Integer.parseInt(String.valueOf(payload.getOrDefault("port", "8001")));
        } catch (NumberFormatException ignored) {}
        return ResponseEntity.ok(agentService.testConnection(ip, port));
    }
}
