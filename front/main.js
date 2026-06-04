const BASE = "https://battbatt-1.onrender.com/api";

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

async function optimize() {
  await fetch(`${BASE}/storage/optimize`, {
    method: "POST"
  });

  load();
}

load();
