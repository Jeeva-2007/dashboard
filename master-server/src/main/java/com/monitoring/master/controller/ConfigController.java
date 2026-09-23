package com.monitoring.master.controller;

import com.monitoring.master.service.TelemetryPollingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "*")
public class ConfigController {

    private final TelemetryPollingService pollingService;

    public ConfigController(TelemetryPollingService pollingService) {
        this.pollingService = pollingService;
    }

    @GetMapping("/polling-interval")
    public ResponseEntity<Map<String, Object>> getPollingInterval() {
        return ResponseEntity.ok(Map.of(
                "intervalSeconds", pollingService.getPollingIntervalSeconds()
        ));
    }

    @PostMapping("/polling-interval")
    public ResponseEntity<Map<String, Object>> setPollingInterval(@RequestBody Map<String, Object> body) {
        int seconds = 5;
        if (body.containsKey("intervalSeconds")) {
            seconds = Integer.parseInt(String.valueOf(body.get("intervalSeconds")));
        }
        pollingService.setPollingIntervalSeconds(seconds);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "intervalSeconds", pollingService.getPollingIntervalSeconds()
        ));
    }
}
