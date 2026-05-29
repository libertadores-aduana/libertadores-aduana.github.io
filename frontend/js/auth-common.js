function saveSession(data) {
  sessionStorage.setItem("token", data.token);
  sessionStorage.setItem("rol", data.rol);
  sessionStorage.setItem("nombre", data.nombre || "");
  if (data.usuarioId != null) {
    sessionStorage.setItem("usuarioId", String(data.usuarioId));
  } else {
    sessionStorage.removeItem("usuarioId");
  }
  if (data.empleadoId != null) {
    sessionStorage.setItem("empleadoId", String(data.empleadoId));
  } else {
    sessionStorage.removeItem("empleadoId");
  }
}

function isViajero() {
  return getRol() === "VIAJERO";
}

function isFuncionario() {
  const rol = getRol();
  return rol && rol !== "VIAJERO";
}

function redirectAfterLogin() {
  if (isViajero()) {
    window.location.href = "perfil.html";
  } else {
    window.location.href = "panel.html";
  }
}

function requireViajero() {
  if (!getToken() || !isViajero()) {
    window.location.href = "index.html#viajero";
    return false;
  }
  return true;
}

function requireFuncionario() {
  if (!getToken() || isViajero()) {
    window.location.href = "index.html";
    return false;
  }
  return true;
}
