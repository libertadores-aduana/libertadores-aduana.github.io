if (!getToken()) {
  window.location.href = "index.html";
  throw new Error("redirect");
}
if (typeof isViajero === "function" && isViajero()) {
  window.location.href = "perfil.html";
  throw new Error("redirect");
}

const rol = getRol();
document.getElementById("userBadge").textContent =
  `${sessionStorage.getItem("nombre") || ""} · ${rol}`;

if (rol === "PDI" || rol === "ADMIN") {
  document.getElementById("navPdi").classList.remove("hidden");
  document.getElementById("sec-pdi").classList.remove("hidden");
}
if (rol === "ADUANA" || rol === "ADMIN") {
  document.getElementById("navAduana").classList.remove("hidden");
  document.getElementById("sec-aduana").classList.remove("hidden");
}
if (rol === "SAG" || rol === "ADMIN") {
  document.getElementById("navSag").classList.remove("hidden");
  document.getElementById("sec-sag").classList.remove("hidden");
}

/* Navegación responsive */
const sidebar = document.getElementById("sidebar");
const overlay = document.getElementById("sidebarOverlay");
const menuToggle = document.getElementById("menuToggle");

function closeSidebar() {
  sidebar.classList.remove("open");
  overlay.classList.remove("visible");
}

menuToggle.addEventListener("click", () => {
  sidebar.classList.toggle("open");
  overlay.classList.toggle("visible");
});
overlay.addEventListener("click", closeSidebar);

function showSection(name) {
  document.querySelectorAll(".panel-section").forEach((s) => s.classList.add("hidden"));
  const el = document.getElementById("sec-" + name);
  if (el) el.classList.remove("hidden");
  document.querySelectorAll(".sidebar nav a").forEach((a) => {
    a.classList.toggle("active", a.dataset.section === name);
  });
  closeSidebar();
}

document.querySelectorAll(".sidebar nav a[data-section]").forEach((a) => {
  a.addEventListener("click", (e) => {
    e.preventDefault();
    showSection(a.dataset.section);
  });
});

showSection("cuenta");

function showMessage(elId, text, type) {
  const el = document.getElementById(elId);
  el.textContent = typeof text === "string" ? text : JSON.stringify(text, null, 2);
  el.className = `message ${type}`;
}

/* Cambiar contraseña */
document.getElementById("formCambiarPassword").addEventListener("submit", async (e) => {
  e.preventDefault();
  const f = e.target;
  try {
    const data = await apiRequest("/auth/cambiar-password", {
      method: "POST",
      body: JSON.stringify({
        passwordActual: f.passwordActual.value,
        passwordNueva: f.passwordNueva.value,
      }),
    });
    showMessage("msgCuenta", data.mensaje, "success");
    f.reset();
  } catch (err) {
    showMessage("msgCuenta", err.message, "error");
  }
});

/* Buscar código pre-registro */
document.getElementById("formBuscarCodigo").addEventListener("submit", async (e) => {
  e.preventDefault();
  const codigo = e.target.codigo.value.trim().toUpperCase();
  const pre = document.getElementById("resultCodigo");
  try {
    const data = await apiRequest("/public/seguimiento/" + codigo);
    pre.textContent = JSON.stringify(data, null, 2);
    pre.classList.remove("hidden");
  } catch (err) {
    pre.textContent = err.message;
    pre.classList.remove("hidden");
  }
});

async function handleUpload(form, msgId, extraFields = {}) {
  const f = form;
  const fd = new FormData();
  fd.append("archivo", f.archivo.files[0]);
  fd.append("tipoDocumento", f.tipoDocumento.value);
  if (f.pasajeroId && f.pasajeroId.value) fd.append("pasajeroId", f.pasajeroId.value);
  if (f.codigoPreRegistro && f.codigoPreRegistro.value) {
    fd.append("codigoPreRegistro", f.codigoPreRegistro.value.toUpperCase());
  }
  Object.entries(extraFields).forEach(([k, v]) => {
    if (v != null) fd.append(k, v);
  });
  const data = await uploadFile("/documentos/upload", fd, false);
  showMessage(msgId, data.mensaje + " — " + data.nombreArchivo, "success");
  f.archivo.value = "";
}

document.getElementById("formUploadGeneral")?.addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    await handleUpload(e.target, "msgDoc");
  } catch (err) {
    showMessage("msgDoc", err.message, "error");
  }
});

document.getElementById("formUploadPermiso")?.addEventListener("submit", async (e) => {
  e.preventDefault();
  const pasajeroId = document.querySelector("#formPermiso [name=pasajeroMenorId]").value;
  if (!pasajeroId) {
    showMessage("msgPdiPermiso", "Indique primero el ID del pasajero menor", "alert");
    return;
  }
  e.target.pasajeroId.value = pasajeroId;
  try {
    await handleUpload(e.target, "msgPdiPermiso");
  } catch (err) {
    showMessage("msgPdiPermiso", err.message, "error");
  }
});

document.getElementById("formUploadSat")?.addEventListener("submit", async (e) => {
  e.preventDefault();
  try {
    await handleUpload(e.target, "msgAduana");
  } catch (err) {
    showMessage("msgAduana", err.message, "error");
  }
});

document.getElementById("formUploadSag")?.addEventListener("submit", async (e) => {
  e.preventDefault();
  const pasajeroId = document.querySelector("#formDeclaracion [name=pasajeroId]").value;
  if (!pasajeroId) {
    showMessage("msgSag", "Indique el ID del pasajero en el formulario de declaración", "alert");
    return;
  }
  e.target.pasajeroId.value = pasajeroId;
  try {
    await handleUpload(e.target, "msgSag");
  } catch (err) {
    showMessage("msgSag", err.message, "error");
  }
});

/* PDI */
document.getElementById("formPasajero")?.addEventListener("submit", async (e) => {
  e.preventDefault();
  const f = e.target;
  try {
    const data = await apiRequest("/pdi/pasajeros", {
      method: "POST",
      body: JSON.stringify({
        documento: f.documento.value,
        tipoDocumento: f.tipoDocumento.value,
        nombres: f.nombres.value,
        apellidos: f.apellidos.value,
        fechaNacimiento: f.fechaNacimiento.value,
        nacionalidad: f.nacionalidad.value,
        tutorId: f.tutorId.value ? Number(f.tutorId.value) : null,
      }),
    });
    showMessage("msgPdiPasajero", data, data.requierePermiso ? "alert" : "success");
    if (data.pasajeroId) {
      document.querySelector("#formPermiso [name=pasajeroMenorId]").value = data.pasajeroId;
    }
  } catch (err) {
    showMessage("msgPdiPasajero", err.message, "error");
  }
});

document.getElementById("formPermiso")?.addEventListener("submit", async (e) => {
  e.preventDefault();
  const f = e.target;
  try {
    const data = await apiRequest("/pdi/menores/permiso", {
      method: "POST",
      body: JSON.stringify({
        pasajeroMenorId: Number(f.pasajeroMenorId.value),
        tipoPermiso: f.tipoPermiso.value,
        numeroDocumento: f.numeroDocumento.value,
        fechaEmision: f.fechaEmision.value,
      }),
    });
    showMessage("msgPdiPermiso", data, "success");
  } catch (err) {
    showMessage("msgPdiPermiso", err.message, "error");
  }
});

document.getElementById("btnAutorizarSalida")?.addEventListener("click", async () => {
  const id = document.querySelector("#formPermiso [name=pasajeroMenorId]").value;
  if (!id) return;
  try {
    const data = await apiRequest("/pdi/menores/autorizar-salida", {
      method: "POST",
      body: JSON.stringify({ pasajeroMenorId: Number(id) }),
    });
    showMessage("msgPdiPermiso", data, data.autorizado ? "success" : "error");
  } catch (err) {
    showMessage("msgPdiPermiso", err.message, "error");
  }
});

/* Aduana */
document.getElementById("formSat")?.addEventListener("submit", async (e) => {
  e.preventDefault();
  const f = e.target;
  try {
    const data = await apiRequest("/aduana/vehiculos/sat", {
      method: "POST",
      body: JSON.stringify({
        patente: f.patente.value,
        paisOrigen: f.paisOrigen.value,
        tipoVehiculo: f.tipoVehiculo.value,
      }),
    });
    showMessage("msgAduana", data, data.alerta ? "alert" : "success");
  } catch (err) {
    showMessage("msgAduana", err.message, "error");
  }
});

/* SAG */
document.getElementById("formDeclaracion")?.addEventListener("submit", async (e) => {
  e.preventDefault();
  const f = e.target;
  try {
    const data = await apiRequest("/sag/declaraciones", {
      method: "POST",
      body: JSON.stringify({
        pasajeroId: Number(f.pasajeroId.value),
        representanteId: f.representanteId.value ? Number(f.representanteId.value) : null,
        productosAgro: f.productosAgro.checked,
        animalesMascotas: f.animalesMascotas.checked,
        detalle: f.detalle.value,
      }),
    });
    showMessage("msgSag", data, "success");
  } catch (err) {
    showMessage("msgSag", err.message, "error");
  }
});

/* Reportes */
document.getElementById("btnResumen").addEventListener("click", async () => {
  const pre = document.getElementById("resumenJson");
  try {
    const data = await apiRequest("/reportes/resumen");
    pre.textContent = JSON.stringify(data, null, 2);
    pre.classList.remove("hidden");
    showSection("reportes");
  } catch (err) {
    pre.textContent = err.message;
    pre.classList.remove("hidden");
  }
});

async function downloadReport(path, filename) {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: { Authorization: `Bearer ${getToken()}` },
  });
  if (!res.ok) throw new Error("No se pudo descargar");
  const blob = await res.blob();
  const url = URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
}

document.getElementById("btnPdf").addEventListener("click", () =>
  downloadReport("/reportes/pdf", "reporte-libertadores.pdf").catch((e) => alert(e.message))
);
document.getElementById("btnExcel").addEventListener("click", () =>
  downloadReport("/reportes/excel", "reporte-libertadores.xlsx").catch((e) => alert(e.message))
);
