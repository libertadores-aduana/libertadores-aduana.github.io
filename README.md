# Sistema de Control Fronterizo — Los Libertadores

Arquitectura desacoplada para el paso fronterizo **Los Libertadores** (Aduana Chile): backend Java (Maven/Spring Boot) y frontend HTML/CSS/JS nativo.

## Estructura del proyecto

```
aduana/
├── backend/          # Java 17 + Spring Boot + JDBC (PreparedStatement)
├── frontend/         # HTML5, CSS3, JavaScript
├── database/         # schema.sql PostgreSQL
└── README.md
```

## Seguridad implementada

| Requisito | Implementación |
|-----------|----------------|
| Contraseñas empleados | BCrypt (`jbcrypt`) en `PasswordUtil` |
| RUT / pasaportes / patentes | AES-256-GCM en `AesEncryptionUtil` |
| SQL Injection | `PreparedStatement` en todos los repositorios |
| RBAC | `@RequireRole` + interceptores por institución (PDI, ADUANA, SAG, ADMIN) |

## Módulos de negocio

- **PDI:** Registro de pasajeros, permisos de menores, autorización de salida.
- **Aduana:** Consulta de patentes CL/AR, formulario SAT, plazos 180/90 días, alerta por robo.
- **SAG:** Declaración jurada (mayor propia; menor vía representante).
- **Reportes:** PDF (iText) y Excel (Apache POI).

## Puesta en marcha

### 1. Base de datos (PostgreSQL en Render o local)

1. Crear base `libertadores` en Render.
2. Ejecutar `database/schema.sql` en el SQL Editor de Render.
3. Copiar la URL JDBC (formato: `jdbc:postgresql://host:5432/db?sslmode=require`).

### 2. Backend (IntelliJ IDEA)

1. Abrir la carpeta `backend` como proyecto Maven.
2. Configurar variables de entorno (o editar `application.properties`):

```properties
DATABASE_URL=jdbc:postgresql://...
DB_USER=...
DB_PASSWORD=...
AES_SECRET_KEY=<base64 de 32 bytes>
```

3. Ejecutar `LibertadoresApplication`.
4. Al primer arranque con BD vacía se crean usuarios demo:

| Email | Contraseña | Rol |
|-------|------------|-----|
| pdi@libertadores.cl | Pdi12345! | PDI |
| aduana@libertadores.cl | Aduana123! | ADUANA |
| sag@libertadores.cl | Sag12345! | SAG |
| admin@libertadores.cl | Admin123! | ADMIN |

### 3. Frontend

Servir la carpeta `frontend` con Live Server (VS Code) o:

```bash
cd frontend
npx --yes serve -p 5500
```

Abrir `http://localhost:5500` (CORS ya permite ese origen).

## Interfaz institucional

- Barra **Gobierno de Chile** y franja roja/blanca/azul (estilo portales públicos).
- Logos oficiales desde **aduana.cl**, **pdi.cl** y **sag.gob.cl** (con respaldo local en `frontend/assets/logos/`).
- Enlaces a trámites reales: [aduana.cl](https://www.aduana.cl), [digitación vehículos](https://comext.aduana.cl/SNA_SCVM/vista/web/login.do), [declaración SAG](https://dj.sag.gob.cl/declaracion-jurada).
- Tipografía Roboto / Roboto Slab (Kit Digital).
- Diseño responsive: celular, tablet y PC.
- Aviso visible de **proyecto académico EFT** (no es sitio oficial del Estado).

## Cuentas y documentos (EFT Forma A)

| Función | Descripción |
|---------|-------------|
| Login | Solo cuentas habilitadas (`empleados.activo = true`) |
| Cambiar contraseña | Panel → Mi cuenta (mín. 8 caracteres, BCrypt) |
| Recuperar contraseña | `recuperar.html` → enlace válido 1 h → `restablecer.html` |
| Pre-registro viajeros | `pre-registro.html` — código + subida PDF/JPG antes del paso |
| Ayuda | `ayuda.html` — documentos requeridos según el caso |
| Responsive | Celular, tablet y PC (`viewport-fit`, menú lateral, botones ≥44px) |

Ejecutar migración si la BD ya existía: `database/migrations/v2_auth_documentos.sql`

## API principal

| Método | Ruta | Rol |
|--------|------|-----|
| POST | `/api/auth/login` | Público |
| POST | `/api/auth/cambiar-password` | Autenticado |
| POST | `/api/auth/recuperar` | Público |
| POST | `/api/auth/restablecer` | Público |
| POST | `/api/public/preregistro` | Público |
| POST | `/api/public/documentos` | Público (multipart) |
| GET | `/api/public/seguimiento/{codigo}` | Público |
| POST | `/api/documentos/upload` | Funcionario (multipart) |
| POST | `/api/pdi/pasajeros` | PDI |
| POST | `/api/pdi/menores/permiso` | PDI |
| POST | `/api/pdi/menores/autorizar-salida` | PDI |
| POST | `/api/aduana/vehiculos/sat` | ADUANA |
| POST | `/api/sag/declaraciones` | SAG |
| GET | `/api/reportes/resumen`, `/pdf`, `/excel` | Todos (autenticados) |

Header: `Authorization: Bearer <token>`

## Publicar gratis (GitHub Pages)

Sitio objetivo: **https://libertadores-aduana.github.io**

Guía paso a paso: ver [DEPLOY.md](DEPLOY.md).

## Próximos pasos sugeridos

1. Sustituir tokens en memoria por JWT firmado.
2. Pantallas separadas por institución y flujo de cruce completo.
3. Integración real con API aduanera argentina (hoy simulada).
4. Registrar encargos de robo desde panel Aduana para probar alertas.
