package com.monitoring.master.repository;

import com.monitoring.master.model.AgentNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgentNodeRepository extends JpaRepository<AgentNode, Long> {
    Optional<AgentNode> findByIpAndPort(String ip, int port);
}
