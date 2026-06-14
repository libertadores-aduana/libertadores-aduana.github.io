function resolveApiBase() {
  const host = window.location.hostname;
  if (host === "localhost" || host === "127.0.0.1") {
    return "http://localhost:8080/api";
  }
  if (window.LIBERTADORES_API_BASE && !window.LIBERTADORES_API_BASE.includes("REEMPLAZA")) {
    return window.LIBERTADORES_API_BASE.replace(/\/$/, "");
  }
  return "http://localhost:8080/api";
}

const API_BASE = resolveApiBase();

function getToken() {
  return sessionStorage.getItem("token");
}

function getRol() {
  return sessionStorage.getItem("rol");
}

function friendlyApiError(raw, status) {
  const msg = String(raw || "").toLowerCase();
  if (msg.includes("failed to fetch") || msg.includes("networkerror")) {
    return "No se pudo conectar con el servidor. Si es la primera vez hoy, espere hasta 1 minuto (el servidor gratuito despierta) e intente de nuevo.";
  }
  if (msg.includes("password authentication failed") || msg.includes("database") && status >= 500) {
    return "El servidor no puede conectar con la base de datos. Revise DATABASE_URL en Render (contraseña de Neon actualizada).";
  }
  if (msg.includes("debe iniciar sesión") || msg.includes("no autenticado")) {
    return "Su sesión expiró (el servidor se reinició). Cierre sesión e ingrese de nuevo.";
  }
  if (status === 401 || status === 403) {
    return raw || "Correo o contraseña incorrectos.";
  }
  if (status >= 500) {
    return raw || "Error interno del servidor. Intente de nuevo en unos segundos.";
  }
  return raw || "Error en la solicitud";
}

async function apiRequest(path, options = {}) {
  const headers = {
    ...(options.headers || {}),
  };
  if (!(options.body instanceof FormData)) {
    headers["Content-Type"] = headers["Content-Type"] || "application/json";
  }
  const token = getToken();
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  let response;
  try {
    response = await fetch(`${API_BASE}${path}`, {
      ...options,
      headers,
    });
  } catch (err) {
    throw new Error(friendlyApiError(err.message));
  }

  const contentType = response.headers.get("content-type") || "";
  let data;
  if (contentType.includes("application/json")) {
    data = await response.json();
  } else {
    data = await response.blob();
  }

  if (!response.ok) {
    const raw =
      typeof data === "object" && data && data.error
        ? data.error
        : "Error en la solicitud";
    throw new Error(friendlyApiError(raw, response.status));
  }
  return data;
}

function setButtonLoading(button, loading, loadingText) {
  if (!button) return;
  if (loading) {
    button.dataset.prevText = button.textContent;
    button.textContent = loadingText || "Conectando…";
    button.disabled = true;
  } else {
    button.textContent = button.dataset.prevText || button.textContent;
    button.disabled = false;
  }
}

async function uploadFile(path, formData, usePublic = false) {
  const url = usePublic
    ? `${API_BASE}/public/documentos`
    : `${API_BASE}${path}`;

  const headers = {};
  const token = getToken();
  if (token && !usePublic) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(url, {
    method: "POST",
    headers,
    body: formData,
  });

  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.error || "Error al subir archivo");
  }
  return data;
}

function logout() {
  sessionStorage.clear();
  window.location.href = "index.html";
}

function getQueryParam(name) {
  return new URLSearchParams(window.location.search).get(name);
}
