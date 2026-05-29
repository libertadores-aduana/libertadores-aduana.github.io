-- Ejecutar en Neon después de schema.sql

CREATE TABLE IF NOT EXISTS usuarios_viajeros (
    id                BIGSERIAL PRIMARY KEY,
    email             VARCHAR(120) NOT NULL UNIQUE,
    password_hash     VARCHAR(100) NOT NULL,
    nombre_completo   VARCHAR(120) NOT NULL,
    rut               VARCHAR(12),
    telefono          VARCHAR(30),
    nacionalidad      VARCHAR(60),
    email_verificado  BOOLEAN NOT NULL DEFAULT FALSE,
    activo            BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en         TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS tokens_verificacion_email (
    id           BIGSERIAL PRIMARY KEY,
    usuario_id   BIGINT NOT NULL REFERENCES usuarios_viajeros(id) ON DELETE CASCADE,
    token_hash   VARCHAR(64) NOT NULL,
    expira_en    TIMESTAMP NOT NULL,
    usado        BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios_viajeros(email);
CREATE INDEX IF NOT EXISTS idx_tokens_verif_usuario ON tokens_verificacion_email(usuario_id);

ALTER TABLE pre_registros ADD COLUMN IF NOT EXISTS usuario_id BIGINT REFERENCES usuarios_viajeros(id);
CREATE INDEX IF NOT EXISTS idx_preregistro_usuario ON pre_registros(usuario_id);
