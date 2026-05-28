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

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
  });

  const contentType = response.headers.get("content-type") || "";
  let data;
  if (contentType.includes("application/json")) {
    data = await response.json();
  } else {
    data = await response.blob();
  }

  if (!response.ok) {
    const error =
      typeof data === "object" && data && data.error
        ? data.error
        : "Error en la solicitud";
    throw new Error(error);
  }
  return data;
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
