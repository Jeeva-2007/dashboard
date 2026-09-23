package com.monitoring.master.dto;

public class AddAgentRequest {
    private String name;
    private String description;
    private String ip;
    private int port = 8001;
    private int pollingIntervalSeconds = 5;

    public AddAgentRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public int getPollingIntervalSeconds() { return pollingIntervalSeconds; }
    public void setPollingIntervalSeconds(int pollingIntervalSeconds) { this.pollingIntervalSeconds = pollingIntervalSeconds; }
}
