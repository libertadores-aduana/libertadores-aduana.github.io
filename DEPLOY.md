# Publicar en https://libertadores-aduana.github.io (gratis)

Guía para estudiantes: frontend en **GitHub Pages**, backend en **Render** (gratis) y base de datos en **Neon** (gratis).

## Cómo obtener exactamente `libertadores-aduana.github.io`

GitHub solo entrega esa URL si el **usuario u organización** se llama `libertadores-aduana` y el repositorio de sitio personal se llama `libertadores-aduana.github.io`.

| Opción | URL final |
|--------|-----------|
| Cuenta `libertadores-aduana` + repo `libertadores-aduana.github.io` | `https://libertadores-aduana.github.io` |
| Tu usuario `eduardo` + repo `aduana` | `https://eduardo.github.io/aduana/` (otra URL) |

### Pasos (cuenta dedicada — recomendado)

1. Cierra sesión en GitHub y crea una cuenta nueva: usuario **`libertadores-aduana`** (correo válido; no cuesta nada).
2. Crea el repositorio **`libertadores-aduana.github.io`** (público).
3. En tu PC, dentro de la carpeta del proyecto:

```bash
cd c:\Users\Eduardo\Desktop\aduana
git init
git add .
git commit -m "Sistema aduanero Los Libertadores - EFT"
git branch -M main
git remote add origin https://github.com/libertadores-aduana/libertadores-aduana.github.io.git
git push -u origin main
```

4. En GitHub: **Settings → Pages → Build and deployment → Source: GitHub Actions**.
5. Tras el push, en **Actions** verás el workflow “Publicar sitio GitHub Pages”. En 1–3 minutos el sitio queda en:

   **https://libertadores-aduana.github.io**

---

## Backend gratis (Render)

1. [render.com](https://render.com) → registro gratis.
2. **New → Web Service** → conecta el mismo repo (o solo la carpeta `backend` si usas monorepo).
3. Configuración sugerida:
   - **Root directory:** `backend`
   - **Build:** `mvn -DskipTests package`
   - **Start:** `java -jar target/libertadores-backend-1.0.0.jar`
4. Variables de entorno en Render:

| Variable | Valor |
|----------|--------|
| `DATABASE_URL` | Connection string **completa** de Neon (`postgresql://usuario:contraseña@host/neondb?sslmode=require`) |
| `AES_SECRET_KEY` | `openssl rand -base64 32` o generador online |
| `CORS_ORIGINS` | `https://libertadores-aduana.github.io,http://localhost:5500` |
| `FRONTEND_URL` | `https://libertadores-aduana.github.io/frontend` |

**Importante:** si usa `DATABASE_URL` con usuario y contraseña incluidos, **no** defina `DB_USER` ni `DB_PASSWORD` en Render (pueden quedar desactualizados y confundir).

### Verificar que la BD funciona

Abra en el navegador:

`https://libertadores-api.onrender.com/api/health/db`

Debe responder `{"ok":true,"database":"UP"}`. Si dice `password authentication failed`, copie de nuevo la connection string desde Neon → **Dashboard → Connect** y péguela en `DATABASE_URL` (si cambió la contraseña, use **Reset password** en Neon primero).

5. Copia la URL del servicio, por ejemplo: `https://libertadores-api.onrender.com`

6. Edita `frontend/js/config.js`:

```javascript
window.LIBERTADORES_API_BASE = "https://libertadores-api.onrender.com/api";
```

7. Haz commit y push; GitHub Pages volverá a publicar el frontend con la API correcta.

**Nota:** el plan gratis de Render “duerme” el servidor; la primera carga puede tardar ~1 minuto. Abre la URL antes de presentar.

---

## Base de datos gratis (Neon)

1. [neon.tech](https://neon.tech) → proyecto gratis PostgreSQL.
2. Copia la connection string y ejecuta `database/schema.sql` y, si aplica, `database/migrations/v2_auth_documentos.sql` en el SQL Editor.
3. Pega host, usuario y contraseña en las variables de Render.

---

## Probar en local (sin publicar)

```bash
# Terminal 1 — backend
cd backend
# configurar application.properties o variables
mvn spring-boot:run

# Terminal 2 — frontend
cd frontend
npx --yes serve -p 5500
```

Abre `http://localhost:5500` (usa `localhost:8080` como API automáticamente).

---

## Checklist antes de la presentación

- [ ] Sitio abre: https://libertadores-aduana.github.io
- [ ] `config.js` tiene la URL real de Render (sin `REEMPLAZA`)
- [ ] `CORS_ORIGINS` en Render incluye `https://libertadores-aduana.github.io`
- [ ] Login con `pdi@libertadores.cl` / `Pdi12345!` (usuarios demo del README)
- [ ] Aviso académico visible en el pie de página

---

## Dominio propio (opcional, no necesario)

`libertadores-aduana.github.io` **ya es tu dominio gratis**. No hace falta comprar `.cl` ni `.com` para la EFT.

Si más adelante compras un dominio, en GitHub Pages → **Custom domain** puedes apuntarlo; no es obligatorio.
