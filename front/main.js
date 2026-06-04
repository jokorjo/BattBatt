const BASE = "https://battbatt-1.onrender.com/api";

// =========================
// NAVIGATION
// =========================
function showPage(page) {
  const pages = ["main", "storage", "processing", "dashboard", "admin"];

  pages.forEach(p => {
    const el = document.getElementById(p + "Page");
    if (el) {
      el.style.display = (p === page) ? "block" : "none";
    }
  });

  // 🔥 automaattinen data
  if (page === "storage") load();
  if (page === "dashboard") {
    loadWorkers();
    loadDevices();
  }
}

// =========================
// STORAGE SUMMARY
// =========================
async function load() {
  const res = await fetch(`${BASE}/batteries/summary`);
  const data = await res.json();

  const tbody = document.querySelector("#table tbody");
  if (!tbody) return;

  tbody.innerHTML = "";

  data.forEach(row => {
    const tr = document.createElement("tr");

    tr.innerHTML = `
      <td>${row.storageName}</td>
      <td>${row.chemistry}</td>
      <td>${row.totalWeight}</td>
      <td>${row.batteryCount}</td>
    `;

    tbody.appendChild(tr);
  });
}

// =========================
// WORKER SUMMARY
// =========================
async function loadWorkers() {
  const res = await fetch(`${BASE}/processing/worker-summary`);
  const data = await res.json();

  const tbody = document.querySelector("#workers tbody");
  if (!tbody) return;

  tbody.innerHTML = "";

  data.forEach(w => {
    const tr = document.createElement("tr");

    tr.innerHTML = `
      <td>${w.workerName || "Worker"}</td>
      <td>${w.taskCount}</td>
      <td>${w.totalTime.toFixed(2)}</td>
    `;

    tbody.appendChild(tr);
  });
}

// =========================
// DEVICE SUMMARY
// =========================
async function loadDevices() {
  const res = await fetch(`${BASE}/processing/device-summary`);
  const data = await res.json();

  const tbody = document.querySelector("#devices tbody");
  if (!tbody) return;

  tbody.innerHTML = "";

  data.forEach(d => {
    const tr = document.createElement("tr");

    tr.innerHTML = `
      <td>${d.deviceName}</td>
      <td>${d.taskCount}</td>
      <td>${d.totalTime.toFixed(2)}</td>
    `;

    tbody.appendChild(tr);
  });
}

// =========================
// OPTIMIZE STORAGE
// =========================
async function optimize() {
  await fetch(`${BASE}/storage/optimize`, {
    method: "POST"
  });

  load();
}

// =========================
// OPTIMIZE PROCESSING
// =========================
async function optimizeProcessing() {
  const workers = prompt("Workers?");
  const minutes = prompt("Working minutes?");

  if (!workers || !minutes) {
    alert("Give both values");
    return;
  }

  await fetch(`${BASE}/processing/optimize`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      workers: Number(workers),
      workingMinutes: Number(minutes)
    })
  });

  alert("Processing optimized");

  loadWorkers();
  loadDevices();
}

// =========================
// CONFIRM PROCESSING
// =========================
async function confirmProcessing() {
  await fetch(`${BASE}/processing/confirm-all`, {
    method: "POST"
  });

  alert("Processing confirmed");

  load();
  loadWorkers();
  loadDevices();
}

// =========================
// INITIAL PAGE
// =========================
showPage("main");
