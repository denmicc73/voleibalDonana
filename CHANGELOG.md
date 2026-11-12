# Changelog

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/).

## [2026-11-12] Decisiones de producto

### Decidido
- Nombre del club: **Almonte Doñana** (en el diseño de Figma, "Club Azul" es provisional).
- Categorías: las oficiales de voleibol en Andalucía, gestionadas desde base de datos por temporada.
- Diseño base: el de Figma (versiones escritorio y móvil). Fotos provisionales hasta tener las reales.
- El club es **femenino y masculino**: los textos de la web se redactan de forma inclusiva (el diseño estaba en femenino).
- Inscripción como **solicitud corta** (tutor/persona, email, año de nacimiento, categoría y privacidad) para minimizar datos de menores; el club completa el resto en persona.

### Cambiado
- `CLAUDE.md`: rellenado el contexto del proyecto ("Qué es", decisiones de producto, alcance de la v1 y la v2, referencia al Figma) y añadidas las reglas de commit + push por iteración y de avanzar por fases.

### Pospuesto a la v2
- Noticias, galería (fotos/vídeos) y clasificación, aunque aparecen en el diseño de Figma.

## [2026-11-12] agente-codigo

### Decisiones de arquitectura
- Spring Boot 4.1 + Java 25 + Thymeleaf (renderizado en servidor) + MySQL en producción / H2 en desarrollo.
- Esquema de base de datos versionado con Flyway.
- Perfiles `dev` / `prod`; los secretos se leen solo de variables de entorno.
- Las categorías serán una tabla por temporada.
- Hosting recomendado: Railway.
- Los logos de patrocinadores se guardarán como BLOB en base de datos.
- Descartado: Spring Boot 3.5, Lombok, tiempo real (websockets) y pagos online.

### Añadido
- Fase 0: proyecto generado con start.spring.io (Spring Boot 4.1.2) y movido desde Downloads a IdeaProjects.
- Esqueleto Maven con wrapper (`mvnw`), paquete `es.voleibaldonana`.
- Perfiles `application.properties`, `application-dev.properties` (H2 modo MySQL, consola H2) y `application-prod.properties` (MySQL por variables de entorno, cookie segura).
- Migración Flyway `V1__inicial.sql` vacía.
- `SecurityConfig` provisional (todo público salvo `/admin/**`, CSRF activo) y `DevH2ConsoleSecurityConfig` solo en dev.
- Página de inicio provisional (`HomeController`, `templates/index.html`).
- `.gitignore`, `.env.example`, `README.md` y `CLAUDE.md` rellenado.

### Cómo probarlo
- `mvnw.cmd test` y `mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev`; abrir http://localhost:8080 (debe mostrar "Web en construcción") y comprobar que `/admin` no es accesible.
