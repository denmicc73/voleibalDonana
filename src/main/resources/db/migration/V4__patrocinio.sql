-- Fase 4: patrocinadores (con logo) y solicitudes de patrocinio desde la web.

CREATE TABLE patrocinador (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    nombre       VARCHAR(100) NOT NULL,
    web          VARCHAR(255) NULL,
    nivel        VARCHAR(20)  NOT NULL,
    orden        INT          NOT NULL DEFAULT 0,
    visible      BOOLEAN      NOT NULL DEFAULT TRUE,
    logo_tipo    VARCHAR(30)  NULL,
    logo_version BIGINT       NULL,
    CONSTRAINT pk_patrocinador PRIMARY KEY (id),
    CONSTRAINT ck_patrocinador_nivel CHECK (nivel IN ('PRINCIPAL', 'COLABORADOR'))
);

-- El logo va aparte para no cargarlo al listar. En la BD y no en disco: el disco del hosting
-- (p. ej. Railway) se borra en cada despliegue.
CREATE TABLE patrocinador_logo (
    patrocinador_id BIGINT     NOT NULL,
    datos           MEDIUMBLOB NOT NULL,
    CONSTRAINT pk_patrocinador_logo PRIMARY KEY (patrocinador_id),
    CONSTRAINT fk_patrocinador_logo FOREIGN KEY (patrocinador_id) REFERENCES patrocinador (id) ON DELETE CASCADE
);

-- Solicitudes del formulario "Patrocina al club". Datos de contacto de empresas: acceso solo ADMIN.
CREATE TABLE solicitud_patrocinio (
    id        BIGINT        NOT NULL AUTO_INCREMENT,
    empresa   VARCHAR(150)  NOT NULL,
    contacto  VARCHAR(100)  NOT NULL,
    email     VARCHAR(254)  NOT NULL,
    telefono  VARCHAR(30)   NULL,
    mensaje   VARCHAR(1000) NULL,
    recibida  DATETIME(6)   NOT NULL,
    atendida  BOOLEAN       NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_solicitud_patrocinio PRIMARY KEY (id)
);

CREATE INDEX ix_solicitud_patrocinio_recibida ON solicitud_patrocinio (recibida);
