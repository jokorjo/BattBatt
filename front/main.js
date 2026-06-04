const BASE = "https://battbatt-1.onrender.com/api";

// =========================
// LOAD STORAGE SUMMARY
// =========================
async function load() {
  const res = await fetch(`${BASE}/batteries/summary`);
  const data = await res.json();

  const tbody = document.querySelector("#table tbody");
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

/* 🔥 LISÄÄ TÄMÄ TÄHÄN ALLE */
async function confirmProcessing() {
  await fetch(`${BASE}/processing/confirm-all`, {
    method: "POST"
  });

  alert("Processing confirmed");

  load();
  loadWorkers();
  loadDevices();
}
