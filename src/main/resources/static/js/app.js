const API_BASE = window.location.origin && window.location.origin !== "null"
  ? `${window.location.origin}/api`
  : "http://localhost:8080/api";

const state = { vehicles: [], jobs: [], bays: [], mechanics: [], bills: [], vehicleLoadError: null };

const $ = (id) => document.getElementById(id);
const esc = (v) => String(v ?? "").replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[c]));
const idOf = x => typeof x === "object" && x ? x.id : x;
const nameOf = x => typeof x === "object" && x ? (x.name || x.registrationNumber || `#${x.id}`) : `#${x}`;

async function api(path, options = {}) {
  const res = await fetch(API_BASE + path, {
    headers: { "Content-Type": "application/json", ...(options.headers || {}) },
    ...options
  });
  const text = await res.text();
  let data = {};
  try { data = text ? JSON.parse(text) : {}; } catch { data = { message: text }; }
  if (!res.ok) throw new Error(data.message || data.error || `Request failed (${res.status})`);
  return data;
}

function showAlert(message, error = false) {
  const box = $("alertBox");
  box.textContent = message;
  box.className = `alert ${error ? "error" : ""}`;
  clearTimeout(showAlert.timer);
  showAlert.timer = setTimeout(() => box.classList.add("hidden"), 4500);
}

function switchSection(name) {
  document.querySelectorAll(".section").forEach(s => s.classList.toggle("active", s.id === name));
  document.querySelectorAll(".nav-item").forEach(b => b.classList.toggle("active", b.dataset.section === name));
  $("pageTitle").textContent = name === "jobs" ? "Job Cards" : name.charAt(0).toUpperCase() + name.slice(1);
  if (name === "dashboard") renderDashboard();
  if (name === "jobs") renderJobs();
  if (name === "vehicles") renderVehicles();
  if (name === "bays") renderBays();
  if (name === "mechanics") renderMechanics();
  if (name === "bills") renderBills();
}

document.querySelectorAll(".nav-item").forEach(b => b.addEventListener("click", () => switchSection(b.dataset.section)));
document.querySelectorAll("[data-go]").forEach(b => b.addEventListener("click", () => switchSection(b.dataset.go)));

function jobStatus(job) {
  return job.status || "WAITING";
}

function vehicleLabel(job) {
  const v = job.vehicle;
  return typeof v === "object" ? `${v.registrationNumber || ""} ${v.vehicleModel || ""}`.trim() : `Vehicle #${v || ""}`;
}

function renderDashboard() {
  $("statJobs").textContent = state.jobs.length;
  $("statWaiting").textContent = state.jobs.filter(j => jobStatus(j) === "WAITING").length;
  $("statProgress").textContent = state.jobs.filter(j => jobStatus(j) === "IN_PROGRESS").length;
  $("statCompleted").textContent = state.jobs.filter(j => jobStatus(j) === "COMPLETED").length;

  const recent = state.jobs.slice().sort((a,b) => (b.id||0)-(a.id||0)).slice(0,6);
  $("dashboardJobs").innerHTML = recent.length ? `
    <table class="table"><thead><tr><th>Job</th><th>Vehicle</th><th>Status</th></tr></thead>
    <tbody>${recent.map(j => `<tr><td>#${esc(j.id)}</td><td>${esc(vehicleLabel(j))}</td><td><span class="status ${esc(jobStatus(j))}">${esc(jobStatus(j).replace("_"," "))}</span></td></tr>`).join("")}</tbody></table>
  ` : `<div class="empty">No job cards yet.</div>`;

  $("dashboardBays").innerHTML = state.bays.length ? state.bays.map(b => {
    const active = state.jobs.find(j => idOf(j.bay) === b.id && jobStatus(j) !== "COMPLETED");
    return `<div class="bay-card ${active ? "busy" : "free"}"><div class="bay-title"><span>Bay ${esc(b.bayNumber)}</span><span>${active ? "Busy" : "Free"}</span></div><div class="bay-meta">${active ? `Job #${esc(active.id)} · ${esc(jobStatus(active))}` : "Ready for next job"}</div></div>`;
  }).join("") : `<div class="empty">No bays configured.</div>`;
}

function renderJobs() {
  const query = ($("jobSearch").value || "").toLowerCase();
  const rows = state.jobs.filter(j => `${j.id} ${vehicleLabel(j)} ${j.requestedServices || ""}`.toLowerCase().includes(query));
  $("jobsTable").innerHTML = rows.length ? `
    <table class="table"><thead><tr><th>Job</th><th>Vehicle</th><th>Services</th><th>Bay</th><th>Mechanic</th><th>Status</th><th>Actions</th></tr></thead>
    <tbody>${rows.map(j => `<tr>
      <td>#${esc(j.id)}</td>
      <td>${esc(vehicleLabel(j))}</td>
      <td>${esc(j.requestedServices || "-")}</td>
      <td>${j.bay ? `Bay ${esc(j.bay.bayNumber || j.bay)}` : "-"}</td>
      <td>${j.mechanic ? esc(nameOf(j.mechanic)) : "-"}</td>
      <td><span class="status ${esc(jobStatus(j))}">${esc(jobStatus(j).replace("_"," "))}</span></td>
      <td><button class="action-btn" onclick="openAssign(${j.id})">Assign</button> <button class="action-btn" onclick="openStatus(${j.id})">Status</button> <button class="action-btn" onclick="openBill(${j.id})">Bill</button> <button class="action-btn danger" onclick="deleteRecord('/jobcards',${j.id},'job card')">Delete</button></td>
    </tr>`).join("")}</tbody></table>` : `<div class="empty">No matching job cards.</div>`;
}

function renderVehicles() {
  const query = ($("vehicleSearch").value || "").toLowerCase();
  const rows = state.vehicles.filter(v => `${v.registrationNumber} ${v.ownerName} ${v.vehicleModel}`.toLowerCase().includes(query));
  $("vehiclesTable").innerHTML = rows.length ? `
    <table class="table"><thead><tr><th>Registration</th><th>Owner</th><th>Phone</th><th>Model</th><th>Type</th><th>Actions</th></tr></thead>
    <tbody>${rows.map(v => `<tr><td><b>${esc(v.registrationNumber)}</b></td><td>${esc(v.ownerName)}</td><td>${esc(v.phoneNumber || "-")}</td><td>${esc(v.vehicleModel)}</td><td>${esc(v.vehicleType)}</td><td><button class="action-btn danger" onclick="deleteRecord('/vehicles',${v.id},'vehicle')">Delete</button></td></tr>`).join("")}</tbody></table>` : `<div class="empty">${state.vehicleLoadError ? esc(state.vehicleLoadError) : "No vehicles found. Add a vehicle to get started."}</div>`;
}

function renderBays() {
  $("baysGrid").innerHTML = state.bays.length ? state.bays.map(b => {
    const active = state.jobs.find(j => idOf(j.bay) === b.id && jobStatus(j) !== "COMPLETED");
    return `<div class="bay-card ${active ? "busy" : "free"}">
      <div class="bay-title"><span>Bay ${esc(b.bayNumber)}</span><span>${active ? "Occupied" : "Available"}</span></div>
      <div class="bay-meta">${esc(b.bayName || "Service Bay")}<br>${active ? `Active Job: #${esc(active.id)}` : "No active job"}</div>
      <button class="action-btn danger" onclick="deleteRecord('/bays',${b.id},'bay')" ${state.jobs.some(j=>idOf(j.bay)===b.id) ? "disabled" : ""}>Delete bay</button>
    </div>`;
  }).join("") : `<div class="empty">Add service bays to start scheduling.</div>`;
}

function renderMechanics() {
  $("mechanicsGrid").innerHTML = state.mechanics.length ? state.mechanics.map(m => `
    <div class="mechanic-card">
      <div class="mechanic-head"><div class="avatar">${esc((m.name||"M").charAt(0).toUpperCase())}</div><div><h3>${esc(m.name)}</h3><p>${esc(m.specialization || "General Service")}</p></div></div>
      <p>Status: <b>${m.available === false ? "Busy" : "Available"}</b></p>
      <button class="action-btn danger" onclick="deleteRecord('/mechanics',${m.id},'mechanic')" ${m.available === false ? "disabled" : ""}>Delete mechanic</button>
    </div>`).join("") : `<div class="empty">No mechanics added.</div>`;
}

function renderBills() {
  $("billsTable").innerHTML = state.bills.length ? `
    <table class="table"><thead><tr><th>Bill</th><th>Job</th><th>Parts</th><th>Labour</th><th>Total</th><th>Date</th><th>Actions</th></tr></thead>
    <tbody>${state.bills.map(b => `<tr><td>#${esc(b.id)}</td><td>#${esc(idOf(b.jobCard))}</td><td>₹${Number(b.partsAmount||0).toFixed(2)}</td><td>₹${Number(b.labourCharge||0).toFixed(2)}</td><td><b>₹${Number(b.totalAmount||0).toFixed(2)}</b></td><td>${esc(b.generatedAt || "-")}</td><td><button class="action-btn danger" onclick="deleteRecord('/bills',${b.id},'bill')">Delete</button></td></tr>`).join("")}</tbody></table>` : `<div class="empty">No bills generated yet.</div>`;
}

function openModal(title, html, submit) {
  $("modalTitle").textContent = title;
  $("modalForm").innerHTML = html;
  $("modal").classList.remove("hidden");
  $("modalForm").onsubmit = async e => { e.preventDefault(); try { await submit(new FormData(e.target)); closeModal(); await loadAll(); showAlert("Saved successfully."); } catch(err) { showAlert(err.message, true); } };
}
function closeModal(){ $("modal").classList.add("hidden"); $("modalForm").innerHTML = ""; }
async function deleteRecord(path, id, label) {
  if (!window.confirm(`Delete this ${label}? This cannot be undone.`)) return;
  try { await api(`${path}/${id}`, {method:"DELETE"}); await loadAll(); showAlert(`${label[0].toUpperCase()+label.slice(1)} deleted.`); }
  catch (err) { showAlert(err.message, true); }
}
$("closeModal").onclick = closeModal;
$("modal").addEventListener("click", e => { if(e.target === $("modal")) closeModal(); });

function openVehicleForm(){
  openModal("Add Vehicle", `
    <div class="form-grid">
      <div class="field"><label>Registration Number</label><input name="registrationNumber" required placeholder="TN 38 AB 1234"></div>
      <div class="field"><label>Owner Name</label><input name="ownerName" required></div>
      <div class="field"><label>Phone Number</label><input name="phoneNumber" type="tel" required></div>
      <div class="field"><label>Vehicle Model</label><input name="vehicleModel" required placeholder="Hyundai i20"></div>
      <div class="field"><label>Vehicle Type</label><select name="vehicleType"><option>Car</option><option>Bike</option><option>SUV</option><option>Van</option><option>Other</option></select></div>
    </div>
    <div class="form-actions"><button type="button" class="secondary" onclick="closeModal()">Cancel</button><button class="primary">Save Vehicle</button></div>`,
    async f => api("/vehicles",{method:"POST",body:JSON.stringify(Object.fromEntries(f))})
  );
}

async function openJobForm(){
  if (!await refreshVehicles()) return;
  if (!state.vehicles.length) { showAlert("No vehicles are saved yet. Add a vehicle to create a job card.", true); switchSection("vehicles"); return; }
  openModal("Create Job Card", `
    <div class="form-grid">
      <div class="field full"><label>Vehicle</label><select name="vehicleId" required>${state.vehicles.map(v=>`<option value="${v.id}">${esc(v.registrationNumber)} — ${esc(v.vehicleModel)}</option>`).join("")}</select></div>
      <div class="field full"><label>Requested Services</label><input name="requestedServices" required placeholder="Oil change, brake inspection"></div>
      <div class="field full"><label>Description</label><textarea name="description" placeholder="Customer concerns or additional notes"></textarea></div>
    </div>
    <div class="form-actions"><button type="button" class="secondary" onclick="closeModal()">Cancel</button><button class="primary">Create Job Card</button></div>`,
    async f => api("/job-cards",{method:"POST",body:JSON.stringify({vehicleId:Number(f.get("vehicleId")),requestedServices:f.get("requestedServices"),description:f.get("description")})})
  );
}

function openAssign(jobId){
  const job = state.jobs.find(j=>j.id===jobId);
  openModal(`Assign Job #${jobId}`, `
    <div class="field"><label>Service Bay</label><select name="bayId" required>${state.bays.map(b=>`<option value="${b.id}">Bay ${esc(b.bayNumber)} — ${esc(b.bayName||"")}</option>`).join("")}</select></div>
    <div class="field" style="margin-top:12px"><label>Mechanic</label><select name="mechanicId" required>${state.mechanics.map(m=>`<option value="${m.id}">${esc(m.name)} — ${esc(m.specialization||"")}</option>`).join("")}</select></div>
    <div class="form-actions"><button type="button" class="secondary" onclick="closeModal()">Cancel</button><button class="primary">Assign Job</button></div>`,
    async f => api(`/job-cards/${jobId}/assign`,{method:"PUT",body:JSON.stringify({bayId:Number(f.get("bayId")),mechanicId:Number(f.get("mechanicId"))})})
  );
}

function openStatus(jobId){
  const job = state.jobs.find(j=>j.id===jobId);
  openModal(`Update Job #${jobId}`, `
    <div class="field"><label>New Status</label><select name="status">
      ${["WAITING","IN_PROGRESS","QUALITY_CHECK","COMPLETED"].map(s=>`<option ${s===jobStatus(job)?"selected":""}>${s}</option>`).join("")}
    </select></div>
    <p class="small" style="margin-top:12px">A job must pass QUALITY_CHECK before it can be COMPLETED.</p>
    <div class="form-actions"><button type="button" class="secondary" onclick="closeModal()">Cancel</button><button class="primary">Update Status</button></div>`,
    async f => api(`/job-cards/${jobId}/status`,{method:"PUT",body:JSON.stringify({status:f.get("status")})})
  );
}

function openBill(jobId){
  openModal(`Generate Bill for Job #${jobId}`, `
    <div class="form-grid">
      <div class="field"><label>Parts Cost (₹)</label><input name="partsCost" type="number" min="0" step="0.01" required value="0"></div>
      <div class="field"><label>Labour Charge (₹)</label><input name="labourCharge" type="number" min="0" step="0.01" required value="0"></div>
    </div>
    <div class="form-actions"><button type="button" class="secondary" onclick="closeModal()">Cancel</button><button class="primary">Generate Bill</button></div>`,
    async f => api("/bills",{method:"POST",body:JSON.stringify({jobCardId:jobId,partsAmount:Number(f.get("partsCost")),labourCharge:Number(f.get("labourCharge"))})})
  );
}

function openBayForm(){
  openModal("Add Service Bay", `
    <div class="form-grid">
      <div class="field"><label>Bay Number</label><input name="bayNumber" type="number" min="1" required></div>
      <div class="field"><label>Bay Name</label><input name="bayName" required placeholder="General Service"></div>
      <div class="field"><label>Active</label><select name="active"><option value="true">Yes</option><option value="false">No</option></select></div>
    </div>
    <div class="form-actions"><button type="button" class="secondary" onclick="closeModal()">Cancel</button><button class="primary">Save Bay</button></div>`,
    async f => api("/bays",{method:"POST",body:JSON.stringify({bayNumber:Number(f.get("bayNumber")),bayName:f.get("bayName"),active:f.get("active")==="true"})})
  );
}

function openMechanicForm(){
  openModal("Add Mechanic", `
    <div class="form-grid">
      <div class="field"><label>Name</label><input name="name" required></div>
      <div class="field"><label>Specialization</label><input name="specialization" required placeholder="Engine / Electrical / General"></div>
      <div class="field"><label>Available</label><select name="available"><option value="true">Yes</option><option value="false">No</option></select></div>
    </div>
    <div class="form-actions"><button type="button" class="secondary" onclick="closeModal()">Cancel</button><button class="primary">Save Mechanic</button></div>`,
    async f => api("/mechanics",{method:"POST",body:JSON.stringify({name:f.get("name"),phoneNumber:f.get("phoneNumber"),specialization:f.get("specialization"),available:f.get("available")==="true"})})
  );
}

function asList(data) {
  const rows = Array.isArray(data) ? data : data?.content;
  if (!Array.isArray(rows)) throw new Error("The API returned an unexpected response format.");
  return rows;
}

async function refreshVehicles() {
  try {
    state.vehicles = asList(await api("/vehicles"));
    state.vehicleLoadError = null;
    renderVehicles();
    return true;
  } catch (error) {
    state.vehicles = [];
    state.vehicleLoadError = `Unable to load vehicles. Please check the backend connection (${error.message}).`;
    renderVehicles();
    $("connectionText").textContent = "Backend unavailable";
    showAlert(state.vehicleLoadError, true);
    return false;
  }
}

async function loadAll() {
  const vehiclesLoaded = await refreshVehicles();
  const resources = await Promise.allSettled([
    api("/job-cards"), api("/bays"), api("/mechanics"), api("/bills")
  ]);
  const setters = [
    data => { state.jobs = asList(data); },
    data => { state.bays = asList(data); },
    data => { state.mechanics = asList(data); },
    data => { state.bills = asList(data); }
  ];
  const errors = [];
  resources.forEach((result, index) => {
    if (result.status === "fulfilled") {
      try { setters[index](result.value); } catch (error) { errors.push(error.message); }
    } else errors.push(result.reason?.message || "A GarageDesk API request failed.");
  });
  $("connectionText").textContent = vehiclesLoaded && errors.length === 0 ? "Connected to Spring Boot" : "Backend connection issue";
  if (errors.length) showAlert(`Unable to load GarageDesk data: ${errors.join("; ")}`, true);
  renderDashboard(); renderJobs(); renderVehicles(); renderBays(); renderMechanics(); renderBills();
}

$("newVehicleBtn").onclick = openVehicleForm;
$("newJobBtn").onclick = openJobForm;
$("quickJobBtn").onclick = () => { switchSection("jobs"); openJobForm(); };
$("newBayBtn").onclick = openBayForm;
$("newMechanicBtn").onclick = openMechanicForm;
$("jobSearch").addEventListener("input", renderJobs);
$("vehicleSearch").addEventListener("input", renderVehicles);

loadAll();
