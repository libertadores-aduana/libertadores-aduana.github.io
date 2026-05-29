if (!requireViajero()) {
  throw new Error("redirect");
}

document.getElementById("userBadge").textContent = sessionStorage.getItem("nombre") || "Viajero";

document.querySelectorAll(".perfil-tab").forEach((btn) => {
  btn.addEventListener("click", () => showTab(btn.dataset.tab));
});

document.querySelectorAll("[data-goto]").forEach((el) => {
  el.addEventListener("click", (e) => {
    e.preventDefault();
    showTab(el.dataset.goto);
  });
});

function showTab(name) {
  document.querySelectorAll(".perfil-section").forEach((s) => s.classList.add("hidden"));
  document.querySelectorAll(".perfil-tab").forEach((b) => {
    b.classList.remove("btn-primary", "active");
    b.classList.add("btn-secondary");
  });
  const section = document.getElementById("tab-" + name);
  if (section) section.classList.remove("hidden");
  const tabBtn = document.querySelector('.perfil-tab[data-tab="' + name + '"]');
  if (tabBtn) {
    tabBtn.classList.remove("btn-secondary");
    tabBtn.classList.add("btn-primary", "active");
  }
  if (name === "tramites") cargarTramites();
  if (name === "perfil") cargarPerfil();
}

async function cargarPerfil() {
  try {
    const p = await apiRequest("/viajero/perfil");
    document.getElementById("perfilEmail").value = p.email || "";
    document.getElementById("perfilNombre").value = p.nombreCompleto || "";
    document.getElementById("perfilRut").value = p.rut || "";
    document.getElementById("perfilTelefono").value = p.telefono || "";
    document.getElementById("perfilNacionalidad").value = p.nacionalidad || "";
  } catch (err) {
    console.error(err);
  }
}

document.getElementById("formPerfil").addEventListener("submit", async (e) => {
  e.preventDefault();
  const msg = document.getElementById("msgPerfil");
  msg.className = "message hidden";
  try {
    const data = await apiRequest("/viajero/perfil", {
      method: "PUT",
      body: JSON.stringify({
        nombreCompleto: document.getElementById("perfilNombre").value.trim(),
        rut: document.getElementById("perfilRut").value.trim(),
        telefono: document.getElementById("perfilTelefono").value.trim(),
        nacionalidad: document.getElementById("perfilNacionalidad").value.trim(),
      }),
    });
    sessionStorage.setItem("nombre", data.nombreCompleto);
    document.getElementById("userBadge").textContent = data.nombreCompleto;
    msg.textContent = "Perfil actualizado";
    msg.className = "message success";
  } catch (err) {
    msg.textContent = err.message;
    msg.className = "message error";
  }
});

document.getElementById("formPassword").addEventListener("submit", async (e) => {
  e.preventDefault();
  const msg = document.getElementById("msgPass");
  msg.className = "message hidden";
  const n1 = document.getElementById("passNueva").value;
  const n2 = document.getElementById("passNueva2").value;
  if (n1 !== n2) {
    msg.textContent = "Las contraseñas nuevas no coinciden";
    msg.className = "message error";
    return;
  }
  try {
    const data = await apiRequest("/viajero/cambiar-password", {
      method: "POST",
      body: JSON.stringify({
        passwordActual: document.getElementById("passActual").value,
        passwordNueva: n1,
      }),
    });
    msg.textContent = data.mensaje;
    msg.className = "message success";
    e.target.reset();
  } catch (err) {
    msg.textContent = err.message;
    msg.className = "message error";
  }
});

async function cargarTramites() {
  const box = document.getElementById("listaTramites");
  try {
    const lista = await apiRequest("/viajero/preregistros");
    if (!lista.length) {
      box.innerHTML = "<p>No tiene pre-registros. Cree uno en <strong>Nuevo pre-registro</strong>.</p>";
      return;
    }
    box.innerHTML = "<ul class=\"tramite-list\">" + lista.map((t) =>
      "<li><strong>" + t.codigo + "</strong> — " + t.tipoTramite +
      " <span style=\"color:#666\">(" + (t.creadoEn || "") + ")</span></li>"
    ).join("") + "</ul>";
  } catch (err) {
    box.innerHTML = "<p class=\"message error\">" + err.message + "</p>";
  }
}

document.getElementById("formPreRegistroPerfil").addEventListener("submit", async (e) => {
  e.preventDefault();
  const msg = document.getElementById("msgPrePerfil");
  msg.className = "message hidden";
  try {
    const data = await apiRequest("/viajero/preregistro", {
      method: "POST",
      body: JSON.stringify({
        tipoTramite: document.getElementById("tipoTramite").value,
        referencia: document.getElementById("referencia").value.trim(),
      }),
    });
    msg.textContent = data.mensaje;
    msg.className = "message success";
    const codigoBox = document.getElementById("codigoBoxPerfil");
    codigoBox.textContent = "Código: " + data.codigoSeguimiento;
    codigoBox.classList.remove("hidden");
    document.getElementById("codigoPreRegistroPerfil").value = data.codigoSeguimiento;
    document.getElementById("formUploadPerfil").classList.remove("hidden");
  } catch (err) {
    msg.textContent = err.message;
    msg.className = "message error";
  }
});

document.getElementById("formUploadPerfil").addEventListener("submit", async (e) => {
  e.preventDefault();
  const msg = document.getElementById("msgUploadPerfil");
  msg.className = "message hidden";
  const fd = new FormData();
  fd.append("archivo", document.getElementById("archivoPerfil").files[0]);
  fd.append("tipoDocumento", document.getElementById("tipoDocumentoPerfil").value);
  fd.append("codigoPreRegistro", document.getElementById("codigoPreRegistroPerfil").value);
  try {
    const data = await apiRequest("/viajero/documentos", { method: "POST", body: fd });
    msg.textContent = data.mensaje + " — " + data.nombreArchivo;
    msg.className = "message success";
    document.getElementById("archivoPerfil").value = "";
  } catch (err) {
    msg.textContent = err.message;
    msg.className = "message error";
  }
});

cargarPerfil();
