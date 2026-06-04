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
// BULK + OPTIMIZE
// =========================
async function bulkInsert() {
  if (batteriesToAdd.length === 0) {
    alert("No batteries added");
    return;
  }

  const ok = confirm("Are you sure info is right?");

  if (!ok) return;

  await fetch(`${BASE}/batteries/bulk`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify(batteriesToAdd)
  });

  batteriesToAdd = [];
  renderBatteryList();

  await optimize();

  alert("Batteries added and optimized");
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
// PROCESSING
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
// INIT
// =========================
window.onload = function () {
  showPage("main");
};
