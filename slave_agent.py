import psutil
import platform
import socket
import uuid
import time
from datetime import datetime, timezone

from fastapi import FastAPI
import uvicorn


# ==========================================
# SLAVE AGENT CONFIGURATION
# ==========================================

AGENT_ID = str(uuid.uuid4())

app = FastAPI(
    title="Server Slave Agent",
    description="System telemetry agent",
    version="1.0"
)


# ==========================================
# SYSTEM INFORMATION
# ==========================================

def get_system_info():

    return {
        "hostname": socket.gethostname(),
        "os": platform.system(),
        "os_version": platform.version(),
        "platform": platform.platform(),
        "architecture": platform.machine(),
        "processor": platform.processor()
    }


# ==========================================
# CPU
# ==========================================

def get_cpu_info():

    return {
        "usage_percent": psutil.cpu_percent(interval=0.5),
        "physical_cores": psutil.cpu_count(logical=False),
        "logical_cores": psutil.cpu_count(logical=True)
    }


# ==========================================
# MEMORY
# ==========================================

def get_memory_info():

    memory = psutil.virtual_memory()

    return {
        "total_bytes": memory.total,
        "used_bytes": memory.used,
        "available_bytes": memory.available,
        "usage_percent": memory.percent
    }


# ==========================================
# DISK
# ==========================================

def get_disk_info():

    disk = psutil.disk_usage("/")

    return {
        "total_bytes": disk.total,
        "used_bytes": disk.used,
        "free_bytes": disk.free,
        "usage_percent": disk.percent
    }


# ==========================================
# NETWORK
# ==========================================

def get_network_info():

    network = psutil.net_io_counters()

    return {
        "bytes_sent": network.bytes_sent,
        "bytes_received": network.bytes_recv,
        "packets_sent": network.packets_sent,
        "packets_received": network.packets_recv
    }


# ==========================================
# UPTIME
# ==========================================

def get_uptime():

    boot_time = psutil.boot_time()

    uptime = time.time() - boot_time

    return {
        "boot_time": datetime.fromtimestamp(
            boot_time,
            timezone.utc
        ).isoformat(),

        "uptime_seconds": int(uptime)
    }


# ==========================================
# TELEMETRY
# ==========================================

def collect_telemetry():

    return {

        "agent_id": AGENT_ID,

        "timestamp": datetime.now(
            timezone.utc
        ).isoformat(),

        "system": get_system_info(),

        "cpu": get_cpu_info(),

        "memory": get_memory_info(),

        "disk": get_disk_info(),

        "network": get_network_info(),

        "uptime": get_uptime()
    }


# ==========================================
# ROOT ENDPOINT
# ==========================================

@app.get("/")
def root():

    return {
        "agent": "Server Slave Agent",
        "agent_id": AGENT_ID,
        "status": "running"
    }


# ==========================================
# HEALTH ENDPOINT
# ==========================================

@app.get("/health")
def health():

    return {
        "status": "healthy",
        "agent_id": AGENT_ID
    }


# ==========================================
# TELEMETRY ENDPOINT
# ==========================================

@app.get("/telemetry")
def telemetry():

    return collect_telemetry()


# ==========================================
# START SERVER
# ==========================================

if __name__ == "__main__":

    print("=" * 60)
    print("          SERVER SLAVE AGENT")
    print("=" * 60)

    print(f"Agent ID : {AGENT_ID}")
    print("API     : http://localhost:8001")
    print("Status  : Starting...")
    print("=" * 60)

    uvicorn.run(
        app,
        host="0.0.0.0",
        port=8998
    )
    