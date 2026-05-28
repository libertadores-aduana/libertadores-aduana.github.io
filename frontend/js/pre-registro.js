document.getElementById("formPreRegistro").addEventListener("submit", async (e) => {
  e.preventDefault();
  const f = e.target;
  const msg = document.getElementById("msgPre");
  msg.className = "message hidden";
  try {
    const data = await apiRequest("/public/preregistro", {
      method: "POST",
      body: JSON.stringify({
        tipoTramite: f.tipoTramite.value,
        email: f.email.value || null,
        referencia: f.referencia.value || null,
      }),
    });
    msg.textContent = data.mensaje;
    msg.className = "message success";
    const box = document.getElementById("codigoBox");
    box.textContent = data.codigoSeguimiento;
    box.classList.remove("hidden");
    document.getElementById("codigoPreRegistro").value = data.codigoSeguimiento;
    document.getElementById("seccionUpload").classList.remove("hidden");
    document.getElementById("seccionUpload").scrollIntoView({ behavior: "smooth" });
  } catch (err) {
    msg.textContent = err.message;
    msg.className = "message error";
  }
});

document.getElementById("formUpload").addEventListener("submit", async (e) => {
  e.preventDefault();
  const f = e.target;
  const msg = document.getElementById("msgUpload");
  msg.className = "message hidden";
  const fd = new FormData();
  fd.append("archivo", f.archivo.files[0]);
  fd.append("tipoDocumento", f.tipoDocumento.value);
  fd.append("codigoPreRegistro", f.codigoPreRegistro.value);
  try {
    const data = await uploadFile("/public/documentos", fd, true);
    msg.textContent = data.mensaje + " — " + data.nombreArchivo;
    msg.className = "message success";
    f.archivo.value = "";
  } catch (err) {
    msg.textContent = err.message;
    msg.className = "message error";
  }
});

document.getElementById("formConsulta").addEventListener("submit", async (e) => {
  e.preventDefault();
  const codigo = e.target.codigo.value.trim().toUpperCase();
  const pre = document.getElementById("consultaResult");
  try {
    const data = await apiRequest("/public/seguimiento/" + codigo);
    pre.textContent = JSON.stringify(data, null, 2);
    pre.classList.remove("hidden");
  } catch (err) {
    pre.textContent = err.message;
    pre.classList.remove("hidden");
  }
});
