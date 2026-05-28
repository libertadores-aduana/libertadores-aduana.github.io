-- PostgreSQL - Sistema Los Libertadores
-- Ejecutar en Render o local antes de iniciar el backend

CREATE TABLE IF NOT EXISTS empleados (
    id              BIGSERIAL PRIMARY KEY,
    rut             VARCHAR(12) NOT NULL UNIQUE,
    nombre_completo VARCHAR(120) NOT NULL,
    email           VARCHAR(120) NOT NULL UNIQUE,
    password_hash   VARCHAR(100) NOT NULL,
    rol             VARCHAR(20) NOT NULL CHECK (rol IN ('PDI', 'ADUANA', 'SAG', 'ADMIN')),
    activo          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS pasajeros (
    id               BIGSERIAL PRIMARY KEY,
    documento_enc    TEXT NOT NULL,
    tipo_documento   VARCHAR(20) NOT NULL,
    nombres_enc      TEXT NOT NULL,
    apellidos_enc    TEXT NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    nacionalidad     VARCHAR(60),
    menor_edad       BOOLEAN NOT NULL DEFAULT FALSE,
    tutor_id         BIGINT REFERENCES pasajeros(id)
);

CREATE TABLE IF NOT EXISTS permisos_menor (
    id                    BIGSERIAL PRIMARY KEY,
    pasajero_menor_id     BIGINT NOT NULL REFERENCES pasajeros(id),
    tipo_permiso          VARCHAR(40) NOT NULL,
    numero_documento      VARCHAR(80) NOT NULL,
    fecha_emision         DATE NOT NULL,
    validado              BOOLEAN NOT NULL DEFAULT FALSE,
    empleado_validador_id BIGINT REFERENCES empleados(id)
);

CREATE TABLE IF NOT EXISTS vehiculos_sat (
    id              BIGSERIAL PRIMARY KEY,
    patente_enc     TEXT NOT NULL,
    pais_origen     VARCHAR(30) NOT NULL,
    tipo_vehiculo   VARCHAR(40) NOT NULL,
    diplomatico     BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_ingreso   DATE NOT NULL,
    fecha_max_salida DATE NOT NULL,
    encargo_robo    BOOLEAN NOT NULL DEFAULT FALSE,
    estado_sat      VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS encargos_robo (
    id          BIGSERIAL PRIMARY KEY,
    patente_enc TEXT NOT NULL,
    pais        VARCHAR(30),
    vigente     BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS declaraciones_juradas (
    id                BIGSERIAL PRIMARY KEY,
    pasajero_id       BIGINT NOT NULL REFERENCES pasajeros(id),
    representante_id  BIGINT REFERENCES pasajeros(id),
    productos_agro    BOOLEAN NOT NULL DEFAULT FALSE,
    animales_mascotas BOOLEAN NOT NULL DEFAULT FALSE,
    detalle           TEXT,
    fecha_declaracion TIMESTAMP NOT NULL,
    empleado_sag_id   BIGINT REFERENCES empleados(id)
);

CREATE INDEX IF NOT EXISTS idx_pasajeros_menor ON pasajeros(menor_edad);
CREATE INDEX IF NOT EXISTS idx_vehiculos_patente ON vehiculos_sat(patente_enc);
CREATE INDEX IF NOT EXISTS idx_encargos_patente ON encargos_robo(patente_enc);

CREATE TABLE IF NOT EXISTS tokens_recuperacion (
    id           BIGSERIAL PRIMARY KEY,
    empleado_id  BIGINT NOT NULL REFERENCES empleados(id),
    token_hash   VARCHAR(64) NOT NULL,
    expira_en    TIMESTAMP NOT NULL,
    usado        BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS pre_registros (
    id              BIGSERIAL PRIMARY KEY,
    codigo          VARCHAR(12) NOT NULL UNIQUE,
    tipo_tramite    VARCHAR(40) NOT NULL,
    email_contacto  VARCHAR(120),
    referencia_enc  TEXT,
    creado_en       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS documentos_adjuntos (
    id                   BIGSERIAL PRIMARY KEY,
    pre_registro_id      BIGINT REFERENCES pre_registros(id),
    pasajero_id          BIGINT REFERENCES pasajeros(id),
    permiso_menor_id     BIGINT REFERENCES permisos_menor(id),
    vehiculo_sat_id      BIGINT REFERENCES vehiculos_sat(id),
    empleado_subidor_id  BIGINT REFERENCES empleados(id),
    tipo_documento       VARCHAR(40) NOT NULL,
    nombre_archivo       VARCHAR(255) NOT NULL,
    ruta_almacenamiento  TEXT NOT NULL,
    mime_type            VARCHAR(80),
    tamano_bytes         BIGINT,
    subido_en            TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_docs_pasajero ON documentos_adjuntos(pasajero_id);
CREATE INDEX IF NOT EXISTS idx_docs_preregistro ON documentos_adjuntos(pre_registro_id);
CREATE INDEX IF NOT EXISTS idx_preregistro_codigo ON pre_registros(codigo);
