-- SOLO DEV: partidos de demostración con fechas relativas a hoy (H2).
-- Rivales y resultados inventados. Se carga con spring.flyway.locations en application-dev.
UPDATE categoria SET horario = 'Mar. y jue. · 20:30 h' WHERE nombre IN ('Sénior', 'Júnior');
UPDATE categoria SET horario = 'Mar. y jue. · 19:00 h' WHERE nombre = 'Juvenil';
UPDATE categoria SET horario = 'Lun. y mié. · 18:00 h' WHERE nombre = 'Cadete';
UPDATE categoria SET horario = 'Lun. y mié. · 17:00 h' WHERE nombre = 'Infantil';
UPDATE categoria SET horario = 'Vie. · 17:00 h' WHERE nombre = 'Alevín';

INSERT INTO partido (categoria_id, genero, rival, en_casa, fecha_hora, lugar, jornada, estado)
SELECT c.id, d.genero, d.rival, d.en_casa, DATEADD('MINUTE', d.minutos, DATEADD('DAY', d.dias, CAST(CURRENT_DATE AS TIMESTAMP))),
       d.lugar, d.jornada, d.estado
FROM categoria c
JOIN (
    SELECT 'Sénior' AS cat, 'FEMENINO' AS genero, 'CD Rival' AS rival, TRUE AS en_casa, -8 AS dias, 720 AS minutos,
           'Pabellón Municipal de Almonte' AS lugar, 'J08' AS jornada, 'FINALIZADO' AS estado
    UNION ALL SELECT 'Sénior', 'MASCULINO', 'CV Marisma', FALSE, -9, 1080, NULL, 'J08', 'FINALIZADO'
    UNION ALL SELECT 'Sénior', 'FEMENINO', 'CD Visitante', FALSE, 6, 720, 'Pabellón de Bollullos', 'J09', 'PROGRAMADO'
    UNION ALL SELECT 'Sénior', 'MASCULINO', 'CD Norte', TRUE, 13, 690, 'Pabellón Municipal de Almonte', 'J09', 'PROGRAMADO'
    UNION ALL SELECT 'Cadete', 'FEMENINO', 'CD Costa', TRUE, -9, 600, 'Pabellón Municipal de Almonte', 'J05', 'FINALIZADO'
    UNION ALL SELECT 'Cadete', 'MASCULINO', 'CD Sur', FALSE, 5, 660, NULL, 'J06', 'PROGRAMADO'
    UNION ALL SELECT 'Juvenil', 'FEMENINO', 'CD Norte', TRUE, -8, 1020, 'Pabellón Municipal de Almonte', 'J05', 'FINALIZADO'
    UNION ALL SELECT 'Infantil', 'MIXTO', 'CD Marisma', TRUE, 5, 630, 'Pabellón Municipal de Almonte', NULL, 'APLAZADO'
) d ON d.cat = c.nombre
WHERE NOT EXISTS (SELECT 1 FROM partido);

-- Sets: Sénior fem. 3-1, Sénior masc. 1-3 (fuera), Cadete fem. 3-0, Juvenil fem. 2-3
INSERT INTO set_partido (partido_id, numero, puntos_club, puntos_rival)
SELECT p.id, s.numero, s.club, s.rival
FROM partido p
JOIN categoria c ON c.id = p.categoria_id
JOIN (
    SELECT 'Sénior' AS cat, 'FEMENINO' AS genero, 1 AS numero, 25 AS club, 20 AS rival
    UNION ALL SELECT 'Sénior', 'FEMENINO', 2, 23, 25
    UNION ALL SELECT 'Sénior', 'FEMENINO', 3, 25, 18
    UNION ALL SELECT 'Sénior', 'FEMENINO', 4, 25, 22
    UNION ALL SELECT 'Sénior', 'MASCULINO', 1, 22, 25
    UNION ALL SELECT 'Sénior', 'MASCULINO', 2, 25, 21
    UNION ALL SELECT 'Sénior', 'MASCULINO', 3, 19, 25
    UNION ALL SELECT 'Sénior', 'MASCULINO', 4, 23, 25
    UNION ALL SELECT 'Cadete', 'FEMENINO', 1, 25, 12
    UNION ALL SELECT 'Cadete', 'FEMENINO', 2, 25, 17
    UNION ALL SELECT 'Cadete', 'FEMENINO', 3, 25, 15
    UNION ALL SELECT 'Juvenil', 'FEMENINO', 1, 25, 23
    UNION ALL SELECT 'Juvenil', 'FEMENINO', 2, 18, 25
    UNION ALL SELECT 'Juvenil', 'FEMENINO', 3, 25, 20
    UNION ALL SELECT 'Juvenil', 'FEMENINO', 4, 21, 25
    UNION ALL SELECT 'Juvenil', 'FEMENINO', 5, 13, 15
) s ON s.cat = c.nombre AND s.genero = p.genero
WHERE p.estado = 'FINALIZADO'
  AND NOT EXISTS (SELECT 1 FROM set_partido);
