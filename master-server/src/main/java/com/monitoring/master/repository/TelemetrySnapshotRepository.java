package com.monitoring.master.repository;

import com.monitoring.master.model.TelemetrySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TelemetrySnapshotRepository extends JpaRepository<TelemetrySnapshot, Long> {
    List<TelemetrySnapshot> findTop40ByAgentNodeIdOrderByRecordedAtDesc(Long agentNodeId);
    void deleteByAgentNodeId(Long agentNodeId);
}
