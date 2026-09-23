package com.monitoring.master.dto;

public class ClusterOverviewDto {
    private int totalNodes;
    private int onlineNodes;
    private int offlineNodes;
    private double avgCpuPercent;
    private double avgMemoryPercent;
    private int pollingIntervalSeconds;

    public ClusterOverviewDto() {}

    public ClusterOverviewDto(int totalNodes, int onlineNodes, int offlineNodes,
                              double avgCpuPercent, double avgMemoryPercent, int pollingIntervalSeconds) {
        this.totalNodes = totalNodes;
        this.onlineNodes = onlineNodes;
        this.offlineNodes = offlineNodes;
        this.avgCpuPercent = avgCpuPercent;
        this.avgMemoryPercent = avgMemoryPercent;
        this.pollingIntervalSeconds = pollingIntervalSeconds;
    }

    public int getTotalNodes() { return totalNodes; }
    public void setTotalNodes(int totalNodes) { this.totalNodes = totalNodes; }

    public int getOnlineNodes() { return onlineNodes; }
    public void setOnlineNodes(int onlineNodes) { this.onlineNodes = onlineNodes; }

    public int getOfflineNodes() { return offlineNodes; }
    public void setOfflineNodes(int offlineNodes) { this.offlineNodes = offlineNodes; }

    public double getAvgCpuPercent() { return avgCpuPercent; }
    public void setAvgCpuPercent(double avgCpuPercent) { this.avgCpuPercent = avgCpuPercent; }

    public double getAvgMemoryPercent() { return avgMemoryPercent; }
    public void setAvgMemoryPercent(double avgMemoryPercent) { this.avgMemoryPercent = avgMemoryPercent; }

    public int getPollingIntervalSeconds() { return pollingIntervalSeconds; }
    public void setPollingIntervalSeconds(int pollingIntervalSeconds) { this.pollingIntervalSeconds = pollingIntervalSeconds; }
}
