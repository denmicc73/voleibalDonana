-- Fase 2: usuarios del panel de administración.
-- La contraseña se guarda solo como hash (BCrypt, con prefijo {bcrypt}).
-- Roles: ADMIN (todo, incluidos los datos de inscripciones) y EDITOR (resultados y calendario).
CREATE TABLE usuario (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    nombre            VARCHAR(100) NOT NULL,
    email             VARCHAR(254) NOT NULL,
    password_hash     VARCHAR(100) NOT NULL,
    rol               VARCHAR(20)  NOT NULL,
    activo            BOOLEAN      NOT NULL DEFAULT TRUE,
    intentos_fallidos INT          NOT NULL DEFAULT 0,
    bloqueado_hasta   DATETIME(6)  NULL,
    ultimo_acceso     DATETIME(6)  NULL,
    creado_en         DATETIME(6)  NOT NULL,
    CONSTRAINT pk_usuario PRIMARY KEY (id),
    CONSTRAINT uk_usuario_email UNIQUE (email),
    CONSTRAINT ck_usuario_rol CHECK (rol IN ('ADMIN', 'EDITOR'))
);
