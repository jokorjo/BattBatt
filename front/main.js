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

  if (page === "storage") load();
  if (page === "dashboard") {
    loadWorkers();
    loadDevices();
  }
}

// =========================
// BATTERY INPUT LIST
// =========================
let batteriesToAdd = [];

function addBattery() {
  const barcode = document.getElementById("barcode").value;
  const type = document.getElementById("batteryType").value;
  const classification = document.getElementById("classification").value;

  if (!barcode) {
    alert("Enter barcode");
    return;
  }

  const battery = {
    barcode: barcode,
    batteryType: { id: Number(type) },
    classification: classification
  };

  batteriesToAdd.push(battery);
  document.getElementById("barcode").value = "";

  renderBatteryList();
}

function removeBattery(index) {
  batteriesToAdd.splice(index, 1);
  renderBatteryList();
}

function renderBatteryList() {
  const ul = document.getElementById("batteryList");
  ul.innerHTML = "";

  batteriesToAdd.forEach((b, i) => {
    const li = document.createElement("li");

    li.innerHTML = `
      ${b.barcode} | Type ${b.batteryType.id} | ${b.classification}
      <button onclick="removeBattery(${i})">X</button>
    `;

    ul.appendChild(li);
  });
}

// =========================
// RENDER INSERTED BATTERIES
// =========================
function renderInserted(data) {
  const tbody = document.querySelector("#insertedTable tbody");
  if (!tbody) return;

  tbody.innerHTML = "";

  data.forEach(b => {
    const tr = document.createElement("tr");

    tr.innerHTML = `
      <td>${b.barcode}</td>
      <td>${b.batteryType.name}</td>
      <td>${b.batteryType.chemistry}</td>
      <td>${b.classification}</td>
      <td>${b.storageSlot ? b.storageSlot.name : "-"}</td>
    `;

    tbody.appendChild(tr);
  });
}

// =========================
// BULK (EI OPTIMIZE)
// =========================
async function bulkInsert() {
  if (batteriesToAdd.length === 0) {
    alert("No batteries added");
    return;
  }

  const ok = confirm("Are you sure info is right?");
  if (!ok) return;

  const res = await fetch(`${BASE}/batteries/bulk`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(batteriesToAdd)
  });

  const data = await res.json();

  renderInserted(data);

  batteriesToAdd = [];
  renderBatteryList();

  alert("Batteries added");
}

// =========================
// OPTIMIZE STORAGE
// =========================
async function optimize() {
  const res = await fetch(`${BASE}/storage/optimize`, {
    method: "POST"
  });

  const data = await res.json();

  renderInserted(data);
  load();
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
// PROCESSING + DASHBOARD (unchanged)
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

async function optimizeProcessing() {
  const workers = prompt("Workers?");
  const minutes = prompt("Working minutes?");

  if (!workers || !minutes) return;

  await fetch(`${BASE}/processing/optimize`, {
    method: "POST",
    headers: {"Content-Type": "application/json"},
    body: JSON.stringify({
      workers: Number(workers),
      workingMinutes: Number(minutes)
    })
  });

  loadWorkers();
  loadDevices();
}

async function confirmProcessing() {
  await fetch(`${BASE}/processing/confirm-all`, {
    method: "POST"
  });

  load();
  loadWorkers();
  loadDevices();
}

// =========================
// INIT
// =========================
window.onload = function () {
  showPage("main");
};
