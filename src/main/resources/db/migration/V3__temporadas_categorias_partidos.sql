-- Fase 3: temporadas, categorías, partidos y resultados set a set.

CREATE TABLE temporada (
    id     BIGINT      NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(20) NOT NULL,
    activa BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT pk_temporada PRIMARY KEY (id),
    CONSTRAINT uk_temporada_nombre UNIQUE (nombre)
);

-- Categorías oficiales de la FAV, por temporada (el club ajusta años y horarios cada año).
-- anio_desde/anio_hasta: años de nacimiento; anio_desde NULL significa "y antes" (sénior).
CREATE TABLE categoria (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    temporada_id     BIGINT       NOT NULL,
    nombre           VARCHAR(50)  NOT NULL,
    descripcion_edad VARCHAR(50)  NULL,
    anio_desde       INT          NULL,
    anio_hasta       INT          NULL,
    horario          VARCHAR(100) NULL,
    orden            INT          NOT NULL DEFAULT 0,
    visible          BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_categoria PRIMARY KEY (id),
    CONSTRAINT fk_categoria_temporada FOREIGN KEY (temporada_id) REFERENCES temporada (id),
    CONSTRAINT uk_categoria_temporada_nombre UNIQUE (temporada_id, nombre)
);

CREATE TABLE partido (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    categoria_id BIGINT       NOT NULL,
    genero       VARCHAR(20)  NOT NULL,
    rival        VARCHAR(100) NOT NULL,
    en_casa      BOOLEAN      NOT NULL,
    fecha_hora   DATETIME(6)  NOT NULL,
    lugar        VARCHAR(150) NULL,
    jornada      VARCHAR(30)  NULL,
    estado       VARCHAR(20)  NOT NULL,
    CONSTRAINT pk_partido PRIMARY KEY (id),
    CONSTRAINT fk_partido_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id),
    CONSTRAINT ck_partido_genero CHECK (genero IN ('FEMENINO', 'MASCULINO', 'MIXTO')),
    CONSTRAINT ck_partido_estado CHECK (estado IN ('PROGRAMADO', 'FINALIZADO', 'APLAZADO', 'SUSPENDIDO'))
);

CREATE INDEX ix_partido_fecha_hora ON partido (fecha_hora);

-- Puntos de cada set desde el punto de vista del club (puntos_club) y del rival.
CREATE TABLE set_partido (
    partido_id   BIGINT NOT NULL,
    numero       INT    NOT NULL,
    puntos_club  INT    NOT NULL,
    puntos_rival INT    NOT NULL,
    CONSTRAINT pk_set_partido PRIMARY KEY (partido_id, numero),
    CONSTRAINT fk_set_partido_partido FOREIGN KEY (partido_id) REFERENCES partido (id) ON DELETE CASCADE,
    CONSTRAINT ck_set_partido_numero CHECK (numero BETWEEN 1 AND 5),
    CONSTRAINT ck_set_partido_puntos CHECK (puntos_club >= 0 AND puntos_rival >= 0)
);

-- Temporada inicial y categorías FAV con años ORIENTATIVOS (el club los revisa desde el panel).
INSERT INTO temporada (nombre, activa) VALUES ('2026/27', TRUE);

INSERT INTO categoria (temporada_id, nombre, descripcion_edad, anio_desde, anio_hasta, horario, orden, visible)
SELECT t.id, c.nombre, c.descripcion_edad, c.anio_desde, c.anio_hasta, NULL, c.orden, TRUE
FROM temporada t
CROSS JOIN (
    SELECT 'Sénior' AS nombre, 'Primer equipo' AS descripcion_edad, NULL AS anio_desde, 2006 AS anio_hasta, 1 AS orden
    UNION ALL SELECT 'Júnior', '18–19 años', 2007, 2008, 2
    UNION ALL SELECT 'Juvenil', '16–17 años', 2009, 2010, 3
    UNION ALL SELECT 'Cadete', '14–15 años', 2011, 2012, 4
    UNION ALL SELECT 'Infantil', '12–13 años', 2013, 2014, 5
    UNION ALL SELECT 'Alevín', '10–11 años', 2015, 2016, 6
) c
WHERE t.nombre = '2026/27';
