/**
 * Logos oficiales (hotlink a portales del Estado) con respaldo local si falla la carga.
 * Proyecto académico EFT — enlaces a trámites reales en aduana.cl, sag.gob.cl, pdi.cl
 */
const LOGOS = {
  aduana: {
    url: "https://www.aduana.cl/aduana/site/artic/20181121/imag/foto_0000000120181121144429/logo.png",
    fallback: "assets/logos/aduana.svg",
    alt: "Servicio Nacional de Aduanas de Chile",
    href: "https://www.aduana.cl/",
  },
  pdi: {
    url: "https://www.pdi.cl/assets/img/logo-pdi.svg",
    fallback: "assets/logos/pdi.svg",
    alt: "Policía de Investigaciones de Chile",
    href: "https://www.pdi.cl/",
  },
  sag: {
    url: "https://dj.sag.gob.cl/assets/img/logosfinales_desktop.svg",
    fallback: "assets/logos/sag.svg",
    alt: "Servicio Agrícola y Ganadero",
    href: "https://www.sag.gob.cl/",
  },
  gob: {
    url: "https://www.gob.cl/favicon.ico",
    fallback: "assets/logos/gobierno-chile.svg",
    alt: "Gobierno de Chile",
    href: "https://www.gob.cl/",
  },
};

const ENLACES_OFICIALES = {
  digitacionVehiculos: "https://comext.aduana.cl/SNA_SCVM/vista/web/login.do",
  declaracionSag: "https://dj.sag.gob.cl/declaracion-jurada",
  aduanaViajero: "https://www.aduana.cl/aduana/site/edic/base/port/viajero_turista.html",
  ayudaAduana: "https://www.aduana.cl/aduana/site/edic/base/port/centro_ayuda.html",
};

function imgLogo(key, className = "") {
  const L = LOGOS[key];
  return `<a href="${L.href}" target="_blank" rel="noopener noreferrer" class="inst-logo-link ${className}" title="${L.alt}">
    <img src="${L.url}" alt="${L.alt}" class="logo-${key}"
         onerror="this.onerror=null;this.src='${L.fallback}'">
  </a>`;
}

function renderGovBar() {
  return `
    <div class="gov-bar" role="banner">
      <div class="gov-bar-inner">
        <div class="gov-bar-chile">
          <span class="gov-bar-flag" aria-hidden="true"></span>
          <a href="https://www.gob.cl/" target="_blank" rel="noopener">Gobierno de Chile</a>
        </div>
        <div class="gov-bar-links">
          <a href="https://www.aduana.cl/" target="_blank" rel="noopener">aduana.cl</a>
          <a href="https://www.sag.gob.cl/" target="_blank" rel="noopener">sag.gob.cl</a>
          <a href="https://www.pdi.cl/" target="_blank" rel="noopener">pdi.cl</a>
          <a href="${ENLACES_OFICIALES.ayudaAduana}" target="_blank" rel="noopener">Centro de ayuda</a>
        </div>
      </div>
    </div>
    <div class="gov-franja" aria-hidden="true"></div>
  `;
}

function renderInstHeader(options = {}) {
  const compact = options.compact === true;
  const logos = `
    <div class="inst-logos-row" style="${compact ? "margin-bottom:0;border:none;padding-bottom:0" : ""}">
      ${imgLogo("aduana", "inst-logo-aduana")}
      ${imgLogo("pdi")}
      ${imgLogo("sag")}
    </div>`;

  if (compact) {
    return `
      <header class="inst-brand-header" style="padding:0.75rem 1rem">
        <div class="inst-brand-inner">${logos}</div>
      </header>`;
  }

  return `
    <header class="inst-brand-header">
      <div class="inst-brand-inner">
        ${logos}
        <div class="inst-hero-text">
          <p class="brand-title">Paso Fronterizo Los Libertadores</p>
          <p class="brand-slogan">Protegemos las fronteras y facilitamos el comercio exterior</p>
          <span class="brand-pass">Región de Valparaíso · Complejo fronterizo Chile — Argentina</span>
        </div>
      </div>
    </header>
    <div class="aviso-academico container" style="margin-top:1rem;padding:0 1rem">
      <strong>Proyecto académico (EFT Ingeniería de Software).</strong>
      Interfaz inspirada en los portales oficiales. Para trámites vigentes use
      <a href="https://www.aduana.cl/" target="_blank" rel="noopener">aduana.cl</a>,
      <a href="${ENLACES_OFICIALES.declaracionSag}" target="_blank" rel="noopener">Declaración Jurada SAG</a> y
      <a href="https://www.pdi.cl/" target="_blank" rel="noopener">pdi.cl</a>.
    </div>
  `;
}

function renderInstFooter() {
  return `
    <footer class="inst-footer">
      <div class="inst-footer-inner">
        <div class="inst-footer-grid">
          <div>
            <h3>Servicio Nacional de Aduanas</h3>
            <ul>
              <li><a href="https://www.aduana.cl/" target="_blank" rel="noopener">Portal aduana.cl</a></li>
              <li><a href="${ENLACES_OFICIALES.digitacionVehiculos}" target="_blank" rel="noopener">Digitación vehículos a Argentina</a></li>
              <li><a href="${ENLACES_OFICIALES.aduanaViajero}" target="_blank" rel="noopener">Información para viajeros</a></li>
              <li><a href="${ENLACES_OFICIALES.ayudaAduana}" target="_blank" rel="noopener">Centro de ayuda</a></li>
            </ul>
          </div>
          <div>
            <h3>PDI — Control migratorio</h3>
            <ul>
              <li><a href="https://www.pdi.cl/" target="_blank" rel="noopener">Policía de Investigaciones</a></li>
              <li><a href="ayuda.html#menores">Requisitos menores de edad</a></li>
            </ul>
          </div>
          <div>
            <h3>SAG — Sanidad agropecuaria</h3>
            <ul>
              <li><a href="https://www.sag.gob.cl/" target="_blank" rel="noopener">Portal sag.gob.cl</a></li>
              <li><a href="${ENLACES_OFICIALES.declaracionSag}" target="_blank" rel="noopener">Declaración jurada digital</a></li>
              <li><a href="https://www.sag.gob.cl/content/ingreso-de-mascotas" target="_blank" rel="noopener">Ingreso de mascotas</a></li>
            </ul>
          </div>
        </div>
        <div class="inst-footer-bottom">
          Sistema Los Libertadores — Examen Final Transversal · RQY1102 · No constituye sitio oficial del Estado.
        </div>
      </div>
    </footer>
  `;
}

function renderPanelHeaderLogos() {
  const a = LOGOS.aduana;
  const p = LOGOS.pdi;
  const s = LOGOS.sag;
  return `
    <div class="header-logos-mini" aria-hidden="true">
      <img class="logo-aduana-mini" src="${a.url}" alt="" onerror="this.src='${a.fallback}'">
      <img src="${p.url}" alt="" onerror="this.src='${p.fallback}'">
      <img src="${s.url}" alt="" onerror="this.src='${s.fallback}'">
    </div>
  `;
}

function initInstitucionalLayout() {
  const gov = document.getElementById("gov-bar-container");
  if (gov) gov.innerHTML = renderGovBar();

  const header = document.getElementById("inst-header-container");
  if (header) header.innerHTML = renderInstHeader({ compact: header.dataset.compact === "true" });

  const footer = document.getElementById("inst-footer-container");
  if (footer) footer.innerHTML = renderInstFooter();

  const panelLogos = document.getElementById("panel-logos-container");
  if (panelLogos) panelLogos.innerHTML = renderPanelHeaderLogos();
}

document.addEventListener("DOMContentLoaded", initInstitucionalLayout);
