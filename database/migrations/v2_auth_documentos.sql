-- Ejecutar después de schema.sql

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
