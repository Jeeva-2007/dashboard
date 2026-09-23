// Proxmox VE Server Monitoring Dashboard Controller

let nodes = [];
let selectedNodeId = null; // null = Datacenter Cluster View
let activeTab = 'summary'; // 'summary', 'charts', 'specs'
let pollingIntervalSeconds = 5;
let refreshTimer = null;
let cpuChart = null;
let memChart = null;

// Initial Load
document.addEventListener('DOMContentLoaded', () => {
  fetchPollingConfig();
  loadData();
  setupClientLoop();
});

// Setup recurring client refresh
function setupClientLoop() {
  if (refreshTimer) clearInterval(refreshTimer);
  const intervalMs = Math.max((pollingIntervalSeconds > 0 ? pollingIntervalSeconds : 5) * 1000, 2000);
  refreshTimer = setInterval(() => {
    loadData(true);
  }, intervalMs);
}

// Fetch polling interval from Master Server
async function fetchPollingConfig() {
  try {
    const res = await fetch('/api/config/polling-interval');
    if (res.ok) {
      const data = await res.json();
      pollingIntervalSeconds = data.intervalSeconds;
      const select = document.getElementById('poll-interval-select');
      if (select) {
        select.value = String(pollingIntervalSeconds);
      }
    }
  } catch (e) {
    console.error('Failed to get polling interval:', e);
  }
}

// Change Polling Interval
async function changePollingInterval(val) {
  const seconds = parseInt(val, 10);
  pollingIntervalSeconds = seconds;
  try {
    await fetch('/api/config/polling-interval', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ intervalSeconds: seconds })
    });
    setupClientLoop();
    loadLogs();
  } catch (e) {
    console.error('Failed to update polling interval:', e);
  }
}

// Fetch All Nodes and Task Logs
async function loadData(silent = false) {
  try {
    const res = await fetch('/api/agents');
    if (res.ok) {
      nodes = await res.json();
      renderResourceTree();
      renderMainContent();
    }
    loadLogs();
  } catch (e) {
    if (!silent) console.error('Failed to load agents:', e);
  }
}

// Fetch Task Logs
async function loadLogs() {
  try {
    const res = await fetch('/api/logs');
    if (res.ok) {
      const logs = await res.json();
      renderTaskLogs(logs);
    }
  } catch (e) {
    console.error('Failed to load logs:', e);
  }
}

// ==========================================
// RESOURCE TREE RENDERING
// ==========================================
function renderResourceTree() {
  const treeList = document.getElementById('tree-nodes-list');
  const countBadge = document.getElementById('node-count-badge');
  const datacenterItem = document.getElementById('tree-item-datacenter');

  if (countBadge) {
    countBadge.textContent = `${nodes.length} Node${nodes.length === 1 ? '' : 's'}`;
  }

  if (selectedNodeId === null) {
    datacenterItem.classList.add('active');
  } else {
    datacenterItem.classList.remove('active');
  }

  if (!treeList) return;
  treeList.innerHTML = '';

  nodes.forEach(node => {
    const item = document.createElement('div');
    const isOnline = node.status === 'ONLINE';
    const isSelected = selectedNodeId === node.id;

    item.className = `pve-tree-item ${isSelected ? 'active' : ''}`;
    item.onclick = () => selectNode(node.id);

    const cpuText = node.cpuUsagePercent !== null ? `${node.cpuUsagePercent.toFixed(0)}%` : '--';

    item.innerHTML = `
      <div class="pve-tree-label">
        <span class="status-dot ${isOnline ? 'online' : 'offline'}"></span>
        <span title="${escapeHtml(node.name)} (${node.ip}:${node.port})">${escapeHtml(node.name)}</span>
      </div>
      <span class="pve-tree-badge">${cpuText}</span>
    `;
    treeList.appendChild(item);
  });
}

function selectDatacenterView() {
  selectedNodeId = null;
  renderResourceTree();
  renderMainContent();
}

function selectNode(id) {
  selectedNodeId = id;
  renderResourceTree();
  renderMainContent();
}

function switchTab(tab) {
  activeTab = tab;
  document.querySelectorAll('.pve-tab').forEach(t => t.classList.remove('active'));
  const activeEl = document.getElementById(`tab-${tab}`);
  if (activeEl) activeEl.classList.add('active');
  renderMainContent();
}

// ==========================================
// MAIN CONTENT RENDERING
// ==========================================
function renderMainContent() {
  const container = document.getElementById('main-content');
  const tabsBar = document.getElementById('view-tabs');
  if (!container) return;

  // View: Datacenter Overview
  if (selectedNodeId === null) {
    if (tabsBar) tabsBar.style.display = 'none';

    if (nodes.length === 0) {
      container.innerHTML = `
        <div class="pve-empty-state">
          <div class="pve-empty-icon">🖥️</div>
          <h2 class="pve-empty-title">No Server Nodes in Datacenter</h2>
          <p class="pve-empty-desc">
            Your Proxmox Master Server is active. Register your Python slave agent
            (e.g., running on <code>127.0.0.1:8001</code>) to begin collecting real-time CPU, RAM, Disk, and Network telemetry.
          </p>
          <button class="pve-btn pve-btn-primary" onclick="openAddServerModal()">
            <span>+</span> Add First Server Node
          </button>
        </div>
      `;
      return;
    }

    // Cluster stats
    const total = nodes.length;
    const online = nodes.filter(n => n.status === 'ONLINE').length;
    const offline = total - online;

    let totalCpu = 0, totalRam = 0, onlineCount = 0;
    nodes.forEach(n => {
      if (n.status === 'ONLINE') {
        if (n.cpuUsagePercent) totalCpu += n.cpuUsagePercent;
        if (n.memoryUsagePercent) totalRam += n.memoryUsagePercent;
        onlineCount++;
      }
    });

    const avgCpu = onlineCount > 0 ? (totalCpu / onlineCount).toFixed(1) : '0.0';
    const avgRam = onlineCount > 0 ? (totalRam / onlineCount).toFixed(1) : '0.0';

    container.innerHTML = `
      <div class="pve-stat-grid">
        <div class="pve-stat-card">
          <div class="pve-stat-title">Total Nodes</div>
          <div class="pve-stat-value">${total}</div>
          <div class="pve-stat-sub">Registered servers</div>
        </div>
        <div class="pve-stat-card">
          <div class="pve-stat-title">Online Nodes</div>
          <div class="pve-stat-value" style="color: var(--pve-status-green);">${online}</div>
          <div class="pve-stat-sub">Responding to telemetry</div>
        </div>
        <div class="pve-stat-card">
          <div class="pve-stat-title">Offline Nodes</div>
          <div class="pve-stat-value" style="color: ${offline > 0 ? 'var(--pve-status-red)' : 'var(--pve-text-dim)'};">${offline}</div>
          <div class="pve-stat-sub">Unreachable agents</div>
        </div>
        <div class="pve-stat-card">
          <div class="pve-stat-title">Cluster Avg CPU</div>
          <div class="pve-stat-value">${avgCpu}%</div>
          <div class="pve-stat-sub">Across active nodes</div>
        </div>
        <div class="pve-stat-card">
          <div class="pve-stat-title">Cluster Avg RAM</div>
          <div class="pve-stat-value">${avgRam}%</div>
          <div class="pve-stat-sub">Memory utilization</div>
        </div>
      </div>

      <div style="margin-bottom: 12px; display: flex; justify-content: space-between; align-items: center;">
        <h3 style="font-size: 14px; font-weight: 700; color: var(--pve-text-bright);">Datacenter Server Nodes</h3>
        <button class="pve-btn pve-btn-primary pve-btn-sm" onclick="openAddServerModal()">+ Add Node</button>
      </div>

      <div class="pve-card-grid">
        ${nodes.map(n => renderNodeCard(n)).join('')}
      </div>
    `;
    return;
  }

  // View: Single Node Detail
  if (tabsBar) tabsBar.style.display = 'flex';
  const node = nodes.find(n => n.id === selectedNodeId);
  if (!node) {
    selectDatacenterView();
    return;
  }

  const isOnline = node.status === 'ONLINE';
  const headerHtml = `
    <div class="pve-node-banner">
      <div class="pve-node-header-info">
        <span class="status-dot ${isOnline ? 'online' : 'offline'}" style="width: 14px; height: 14px;"></span>
        <div>
          <div class="pve-node-title">${escapeHtml(node.name)}</div>
          <div style="font-size: 11px; color: var(--pve-text-muted); font-family: var(--font-mono); margin-top: 2px;">
            ${node.ip}:${node.port} ${node.description ? '— ' + escapeHtml(node.description) : ''}
          </div>
        </div>
        <span class="pve-badge ${isOnline ? 'pve-badge-online' : 'pve-badge-offline'}">
          ${node.status} (${node.lastLatencyMs || 0}ms)
        </span>
      </div>

      <div class="pve-node-specs">
        <div><strong>OS:</strong> ${escapeHtml(node.os || 'Unknown')}</div>
        <div><strong>Uptime:</strong> ${formatUptime(node.uptimeSeconds)}</div>
        <div><strong>Hostname:</strong> ${escapeHtml(node.hostname || '--')}</div>
      </div>

      <div style="display: flex; gap: 8px;">
        <button class="pve-btn pve-btn-sm" onclick="pollCurrentNodeNow()">↻ Poll</button>
        <button class="pve-btn pve-btn-sm" onclick="openEditServerModal(${node.id})">Edit</button>
        <button class="pve-btn pve-btn-danger pve-btn-sm" onclick="deleteNode(${node.id})">Remove</button>
      </div>
    </div>
  `;

  if (activeTab === 'summary') {
    container.innerHTML = headerHtml + renderNodeSummaryTab(node);
  } else if (activeTab === 'charts') {
    container.innerHTML = headerHtml + renderNodeChartsTab(node);
    loadNodeCharts(node.id);
  } else if (activeTab === 'specs') {
    container.innerHTML = headerHtml + renderNodeSpecsTab(node);
  }
}

// Render individual node card in cluster grid
function renderNodeCard(n) {
  const isOnline = n.status === 'ONLINE';
  const cpu = n.cpuUsagePercent !== null ? n.cpuUsagePercent.toFixed(1) : '0.0';
  const ram = n.memoryUsagePercent !== null ? n.memoryUsagePercent.toFixed(1) : '0.0';
  const disk = n.diskUsagePercent !== null ? n.diskUsagePercent.toFixed(1) : '0.0';

  return `
    <div class="pve-node-card" onclick="selectNode(${n.id})">
      <div class="pve-node-card-head">
        <div class="pve-node-card-name">
          <span class="status-dot ${isOnline ? 'online' : 'offline'}"></span>
          <span>${escapeHtml(n.name)}</span>
        </div>
        <span class="pve-badge ${isOnline ? 'pve-badge-online' : 'pve-badge-offline'}">
          ${n.status}
        </span>
      </div>

      <div style="font-size: 11px; color: var(--pve-text-muted); font-family: var(--font-mono);">
        ${n.ip}:${n.port} &bull; ${escapeHtml(n.os || 'Python Slave')}
      </div>

      <div style="display: flex; flex-direction: column; gap: 8px; margin-top: 4px;">
        <div>
          <div style="display: flex; justify-content: space-between; font-size: 11px; margin-bottom: 2px;">
            <span>CPU</span>
            <span style="font-family: var(--font-mono);">${cpu}%</span>
          </div>
          <div class="pve-meter-bar">
            <div class="pve-meter-fill ${getMeterClass(cpu)}" style="width: ${Math.min(cpu, 100)}%;"></div>
          </div>
        </div>

        <div>
          <div style="display: flex; justify-content: space-between; font-size: 11px; margin-bottom: 2px;">
            <span>RAM</span>
            <span style="font-family: var(--font-mono);">${ram}%</span>
          </div>
          <div class="pve-meter-bar">
            <div class="pve-meter-fill ${getMeterClass(ram)}" style="width: ${Math.min(ram, 100)}%;"></div>
          </div>
        </div>

        <div>
          <div style="display: flex; justify-content: space-between; font-size: 11px; margin-bottom: 2px;">
            <span>Disk</span>
            <span style="font-family: var(--font-mono);">${disk}%</span>
          </div>
          <div class="pve-meter-bar">
            <div class="pve-meter-fill ${getMeterClass(disk)}" style="width: ${Math.min(disk, 100)}%;"></div>
          </div>
        </div>
      </div>

      <div style="display: flex; justify-content: space-between; align-items: center; font-size: 11px; color: var(--pve-text-dim); margin-top: 4px; padding-top: 8px; border-top: 1px solid var(--pve-border);">
        <span>Uptime: ${formatUptime(n.uptimeSeconds)}</span>
        <span>Latency: ${n.lastLatencyMs || 0}ms</span>
      </div>
    </div>
  `;
}

// Proxmox Dual Horizontal Meters & Resources View
function renderNodeSummaryTab(n) {
  const cpu = n.cpuUsagePercent !== null ? n.cpuUsagePercent.toFixed(1) : 0;
  const ram = n.memoryUsagePercent !== null ? n.memoryUsagePercent.toFixed(1) : 0;
  const disk = n.diskUsagePercent !== null ? n.diskUsagePercent.toFixed(1) : 0;

  const ramUsedGb = (n.memoryUsedBytes ? n.memoryUsedBytes / (1024 ** 3) : 0).toFixed(2);
  const ramTotalGb = (n.memoryTotalBytes ? n.memoryTotalBytes / (1024 ** 3) : 0).toFixed(2);

  const diskUsedGb = (n.diskUsedBytes ? n.diskUsedBytes / (1024 ** 3) : 0).toFixed(2);
  const diskTotalGb = (n.diskTotalBytes ? n.diskTotalBytes / (1024 ** 3) : 0).toFixed(2);

  const netSentMb = (n.networkBytesSent ? n.networkBytesSent / (1024 ** 2) : 0).toFixed(1);
  const netRecvMb = (n.networkBytesReceived ? n.networkBytesReceived / (1024 ** 2) : 0).toFixed(1);

  return `
    <div class="pve-gauge-grid">
      <!-- CPU Gauge -->
      <div class="pve-gauge-card">
        <div class="pve-gauge-header">
          <span class="pve-gauge-label">CPU Usage</span>
          <span class="pve-gauge-val">${cpu}% of ${n.logicalCores || '--'} CPU(s)</span>
        </div>
        <div class="pve-meter-bar" style="height: 22px;">
          <div class="pve-meter-fill ${getMeterClass(cpu)}" style="width: ${Math.min(cpu, 100)}%;"></div>
          <span class="pve-meter-text">${cpu}%</span>
        </div>
        <div style="font-size: 11px; color: var(--pve-text-dim); margin-top: 6px;">
          Physical: ${n.physicalCores || '--'} cores | Logical: ${n.logicalCores || '--'} cores
        </div>
      </div>

      <!-- RAM Gauge -->
      <div class="pve-gauge-card">
        <div class="pve-gauge-header">
          <span class="pve-gauge-label">RAM Usage</span>
          <span class="pve-gauge-val">${ram}% (${ramUsedGb} GB / ${ramTotalGb} GB)</span>
        </div>
        <div class="pve-meter-bar" style="height: 22px;">
          <div class="pve-meter-fill ${getMeterClass(ram)}" style="width: ${Math.min(ram, 100)}%;"></div>
          <span class="pve-meter-text">${ram}%</span>
        </div>
        <div style="font-size: 11px; color: var(--pve-text-dim); margin-top: 6px;">
          Available: ${((n.memoryAvailableBytes || 0) / (1024 ** 3)).toFixed(2)} GB
        </div>
      </div>

      <!-- Disk Gauge -->
      <div class="pve-gauge-card">
        <div class="pve-gauge-header">
          <span class="pve-gauge-label">Boot Disk Size</span>
          <span class="pve-gauge-val">${disk}% (${diskUsedGb} GB / ${diskTotalGb} GB)</span>
        </div>
        <div class="pve-meter-bar" style="height: 22px;">
          <div class="pve-meter-fill ${getMeterClass(disk)}" style="width: ${Math.min(disk, 100)}%;"></div>
          <span class="pve-meter-text">${disk}%</span>
        </div>
        <div style="font-size: 11px; color: var(--pve-text-dim); margin-top: 6px;">
          Free: ${((n.diskFreeBytes || 0) / (1024 ** 3)).toFixed(2)} GB
        </div>
      </div>

      <!-- Network I/O -->
      <div class="pve-gauge-card">
        <div class="pve-gauge-header">
          <span class="pve-gauge-label">Network Cumulative Traffic</span>
          <span class="pve-gauge-val">TX / RX</span>
        </div>
        <div style="display: flex; gap: 16px; margin-top: 8px;">
          <div style="flex: 1; background: var(--pve-bg-input); padding: 8px; border-radius: 3px; border: 1px solid var(--pve-border);">
            <div style="font-size: 10px; color: var(--pve-text-dim);">BYTES SENT (TX)</div>
            <div style="font-size: 16px; font-weight: 700; color: var(--pve-orange); font-family: var(--font-mono);">${netSentMb} MB</div>
            <div style="font-size: 10px; color: var(--pve-text-muted);">${n.networkPacketsSent || 0} pkts</div>
          </div>
          <div style="flex: 1; background: var(--pve-bg-input); padding: 8px; border-radius: 3px; border: 1px solid var(--pve-border);">
            <div style="font-size: 10px; color: var(--pve-text-dim);">BYTES RECV (RX)</div>
            <div style="font-size: 16px; font-weight: 700; color: var(--pve-status-blue); font-family: var(--font-mono);">${netRecvMb} MB</div>
            <div style="font-size: 10px; color: var(--pve-text-muted);">${n.networkPacketsReceived || 0} pkts</div>
          </div>
        </div>
      </div>
    </div>
  `;
}

// Live Charts Tab
function renderNodeChartsTab(n) {
  return `
    <div class="pve-chart-container">
      <div class="pve-chart-title">
        <span>CPU & RAM Utilization History (%)</span>
        <span style="font-size: 11px; color: var(--pve-text-muted);">Real-time Telemetry Samples</span>
      </div>
      <div style="height: 260px;">
        <canvas id="cpuRamChart"></canvas>
      </div>
    </div>

    <div class="pve-chart-container">
      <div class="pve-chart-title">
        <span>Disk Space & Network Metrics</span>
      </div>
      <div style="height: 220px;">
        <canvas id="diskNetChart"></canvas>
      </div>
    </div>
  `;
}

// Fetch history and draw Chart.js
async function loadNodeCharts(nodeId) {
  try {
    const res = await fetch(`/api/agents/${nodeId}/history`);
    if (!res.ok) return;
    const history = await res.json();
    if (!history || history.length === 0) return;

    const labels = history.map(h => {
      const d = new Date(h.recordedAt);
      return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    });

    const cpuData = history.map(h => h.cpuUsagePercent || 0);
    const ramData = history.map(h => h.memoryUsagePercent || 0);
    const diskData = history.map(h => h.diskUsagePercent || 0);

    const ctx1 = document.getElementById('cpuRamChart');
    if (ctx1) {
      if (cpuChart) cpuChart.destroy();
      cpuChart = new Chart(ctx1, {
        type: 'line',
        data: {
          labels,
          datasets: [
            {
              label: 'CPU Usage %',
              data: cpuData,
              borderColor: '#e57000',
              backgroundColor: 'rgba(229, 112, 0, 0.1)',
              borderWidth: 2,
              fill: true,
              tension: 0.3
            },
            {
              label: 'RAM Usage %',
              data: ramData,
              borderColor: '#2ecc71',
              backgroundColor: 'rgba(46, 204, 113, 0.1)',
              borderWidth: 2,
              fill: true,
              tension: 0.3
            }
          ]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            y: { min: 0, max: 100, grid: { color: '#353c4d' } },
            x: { grid: { color: 'rgba(255,255,255,0.05)' } }
          },
          plugins: { legend: { labels: { color: '#e2e6ea' } } }
        }
      });
    }

    const ctx2 = document.getElementById('diskNetChart');
    if (ctx2) {
      if (memChart) memChart.destroy();
      memChart = new Chart(ctx2, {
        type: 'line',
        data: {
          labels,
          datasets: [
            {
              label: 'Disk Usage %',
              data: diskData,
              borderColor: '#3498db',
              backgroundColor: 'rgba(52, 152, 219, 0.1)',
              borderWidth: 2,
              fill: true,
              tension: 0.3
            }
          ]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            y: { min: 0, max: 100, grid: { color: '#353c4d' } },
            x: { grid: { color: 'rgba(255,255,255,0.05)' } }
          },
          plugins: { legend: { labels: { color: '#e2e6ea' } } }
        }
      });
    }

  } catch (e) {
    console.error('Failed to load charts history:', e);
  }
}

// Hardware & Specs Tab
function renderNodeSpecsTab(n) {
  return `
    <div style="background: var(--pve-bg-panel); border: 1px solid var(--pve-border); border-radius: 4px; overflow: hidden;">
      <table class="pve-table">
        <tbody>
          <tr><th style="width: 220px;">Agent UUID</th><td><code>${escapeHtml(n.agentId || 'Not reported yet')}</code></td></tr>
          <tr><th>Hostname</th><td>${escapeHtml(n.hostname || '--')}</td></tr>
          <tr><th>Operating System</th><td>${escapeHtml(n.os || '--')} (${escapeHtml(n.osVersion || '')})</td></tr>
          <tr><th>Platform / Kernel</th><td>${escapeHtml(n.platform || '--')}</td></tr>
          <tr><th>Architecture</th><td>${escapeHtml(n.architecture || '--')}</td></tr>
          <tr><th>Processor</th><td>${escapeHtml(n.processor || '--')}</td></tr>
          <tr><th>Physical Cores</th><td>${n.physicalCores || '--'}</td></tr>
          <tr><th>Logical Cores</th><td>${n.logicalCores || '--'}</td></tr>
          <tr><th>Total Memory</th><td>${((n.memoryTotalBytes || 0) / (1024 ** 3)).toFixed(2)} GB</td></tr>
          <tr><th>Total Disk Size</th><td>${((n.diskTotalBytes || 0) / (1024 ** 3)).toFixed(2)} GB</td></tr>
          <tr><th>System Boot Time</th><td>${escapeHtml(n.bootTime || '--')}</td></tr>
          <tr><th>Agent Endpoint</th><td><code>http://${n.ip}:${n.port}/telemetry</code></td></tr>
          <tr><th>Created At</th><td>${new Date(n.createdAt).toLocaleString()}</td></tr>
          <tr><th>Last Seen</th><td>${n.lastSeen ? new Date(n.lastSeen).toLocaleString() : 'Never'}</td></tr>
        </tbody>
      </table>
    </div>
  `;
}

// ==========================================
// TASK & POLL LOG CONSOLE
// ==========================================
function renderTaskLogs(logs) {
  const tbody = document.getElementById('task-log-tbody');
  const lastTimeSpan = document.getElementById('console-last-poll-time');
  if (!tbody) return;

  if (logs.length > 0 && lastTimeSpan) {
    const d = new Date(logs[0].timestamp);
    lastTimeSpan.textContent = `(Last: ${d.toLocaleTimeString()})`;
  }

  tbody.innerHTML = logs.map(l => {
    const isOk = l.status === 'OK';
    const d = new Date(l.timestamp);
    const timeStr = d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });

    return `
      <tr>
        <td style="color: var(--pve-text-dim); font-family: var(--font-mono);">${timeStr}</td>
        <td style="font-weight: 600; color: var(--pve-text-bright);">${escapeHtml(l.nodeName || 'Cluster')}</td>
        <td style="color: var(--pve-orange);">${escapeHtml(l.action)}</td>
        <td>
          <span style="color: ${isOk ? 'var(--pve-status-green)' : 'var(--pve-status-red)'}; font-weight: 700;">
            ${l.status} ${l.httpStatus ? `(${l.httpStatus})` : ''}
          </span>
        </td>
        <td style="font-family: var(--font-mono);">${l.latencyMs !== null ? l.latencyMs + 'ms' : '--'}</td>
        <td style="color: var(--pve-text-muted);">${escapeHtml(l.message || '')}</td>
      </tr>
    `;
  }).join('');
}

function toggleBottomConsole() {
  const consoleEl = document.getElementById('bottom-console');
  const icon = document.getElementById('console-toggle-icon');
  if (consoleEl.classList.contains('collapsed')) {
    consoleEl.classList.remove('collapsed');
    icon.textContent = '▼';
  } else {
    consoleEl.classList.add('collapsed');
    icon.textContent = '▲';
  }
}

// ==========================================
// ACTIONS (POLL, ADD, EDIT, DELETE)
// ==========================================
async function pollCurrentNodeNow() {
  if (selectedNodeId) {
    try {
      await fetch(`/api/agents/${selectedNodeId}/poll`, { method: 'POST' });
      await loadData();
    } catch (e) {
      console.error(e);
    }
  } else {
    // Poll all nodes
    await loadData();
  }
}

function openAddServerModal() {
  document.getElementById('modal-title').textContent = 'Add Server Node';
  document.getElementById('modal-node-id').value = '';
  document.getElementById('node-name').value = 'Slave-Node-' + (nodes.length + 1);
  document.getElementById('node-desc').value = 'Python Telemetry Agent';
  document.getElementById('node-ip').value = '127.0.0.1';
  document.getElementById('node-port').value = '8001';
  document.getElementById('node-poll-interval').value = '5';
  hideTestAlert();
  document.getElementById('server-modal').classList.add('open');
}

function openEditServerModal(id) {
  const node = nodes.find(n => n.id === id);
  if (!node) return;
  document.getElementById('modal-title').textContent = 'Edit Server Node';
  document.getElementById('modal-node-id').value = node.id;
  document.getElementById('node-name').value = node.name;
  document.getElementById('node-desc').value = node.description || '';
  document.getElementById('node-ip').value = node.ip;
  document.getElementById('node-port').value = node.port;
  document.getElementById('node-poll-interval').value = node.pollingIntervalSeconds || 5;
  hideTestAlert();
  document.getElementById('server-modal').classList.add('open');
}

function closeServerModal() {
  document.getElementById('server-modal').classList.remove('open');
}

async function testConnection() {
  const ip = document.getElementById('node-ip').value.trim();
  const port = document.getElementById('node-port').value.trim();
  const alertEl = document.getElementById('test-alert');

  alertEl.className = 'test-alert';
  alertEl.style.display = 'block';
  alertEl.textContent = 'Testing connection to http://' + ip + ':' + port + '/telemetry ...';

  try {
    const res = await fetch('/api/agents/test-connection', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ ip, port })
    });
    const result = await res.json();
    if (result.success) {
      alertEl.className = 'test-alert success';
      alertEl.textContent = `✓ ${result.message} (Latency: ${result.latencyMs}ms)`;
    } else {
      alertEl.className = 'test-alert error';
      alertEl.textContent = `✗ ${result.message} (Latency: ${result.latencyMs}ms)`;
    }
  } catch (e) {
    alertEl.className = 'test-alert error';
    alertEl.textContent = 'Failed to test connection: ' + e.message;
  }
}

function hideTestAlert() {
  const alertEl = document.getElementById('test-alert');
  if (alertEl) alertEl.style.display = 'none';
}

async function saveServerNode() {
  const id = document.getElementById('modal-node-id').value;
  const name = document.getElementById('node-name').value.trim();
  const description = document.getElementById('node-desc').value.trim();
  const ip = document.getElementById('node-ip').value.trim();
  const port = parseInt(document.getElementById('node-port').value, 10);
  const pollingIntervalSeconds = parseInt(document.getElementById('node-poll-interval').value, 10);

  if (!name || !ip || !port) {
    alert('Please provide Name, IP Address, and Port.');
    return;
  }

  const payload = { name, description, ip, port, pollingIntervalSeconds };

  try {
    let res;
    if (id) {
      res = await fetch(`/api/agents/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
    } else {
      res = await fetch('/api/agents', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });
    }

    if (res.ok) {
      closeServerModal();
      const savedNode = await res.json();
      selectedNodeId = savedNode.id;
      await loadData();
    } else {
      alert('Error saving server node.');
    }
  } catch (e) {
    alert('Error saving server: ' + e.message);
  }
}

async function deleteNode(id) {
  if (!confirm('Are you sure you want to remove this server node from the Datacenter?')) {
    return;
  }
  try {
    const res = await fetch(`/api/agents/${id}`, { method: 'DELETE' });
    if (res.ok) {
      selectedNodeId = null;
      await loadData();
    }
  } catch (e) {
    console.error(e);
  }
}

// Helpers
function getMeterClass(val) {
  if (val >= 90) return 'danger';
  if (val >= 75) return 'warning';
  return '';
}

function formatUptime(seconds) {
  if (!seconds || seconds <= 0) return '--';
  const d = Math.floor(seconds / (3600 * 24));
  const h = Math.floor((seconds % (3600 * 24)) / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  if (d > 0) return `${d}d ${h}h ${m}m`;
  if (h > 0) return `${h}h ${m}m`;
  return `${seconds}s`;
}

function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
