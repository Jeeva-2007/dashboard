package com.monitoring.master.repository;

import com.monitoring.master.model.TaskLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskLogRepository extends JpaRepository<TaskLogEntry, Long> {
    List<TaskLogEntry> findTop60ByOrderByTimestampDesc();
}
