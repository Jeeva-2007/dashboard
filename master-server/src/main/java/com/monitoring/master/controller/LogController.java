package com.monitoring.master.controller;

import com.monitoring.master.model.TaskLogEntry;
import com.monitoring.master.repository.TaskLogRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "*")
public class LogController {

    private final TaskLogRepository taskLogRepository;

    public LogController(TaskLogRepository taskLogRepository) {
        this.taskLogRepository = taskLogRepository;
    }

    @GetMapping
    public List<TaskLogEntry> getRecentLogs() {
        return taskLogRepository.findTop60ByOrderByTimestampDesc();
    }
}
