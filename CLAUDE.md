# Contexto del proyecto
Leer siempre

## Qué es
Web pública (para producción) del club de voleibol **Almonte Doñana**, de Almonte (Huelva). Club **femenino y masculino**.
Objetivos de la v1:
- **Marcadores**: resultados set a set y calendario de partidos, actualizados por el club desde un panel de administración.
- **Inscripciones**: la gente se apunta según su categoría.
- **Patrocinio**: escaparate de patrocinadores y formulario para empresas que quieran patrocinar.

## Decisiones de producto
- **Categorías**: las oficiales de voleibol en Andalucía (FAV), guardadas en BD por temporada (el admin las ajusta cada año).
- **Inscripción = solicitud corta** (como en el diseño): nombre de madre/padre/tutor o de la persona, email, año de nacimiento, categoría de interés y aceptación de privacidad. El club contacta después y completa los datos en persona. Motivo: minimizar datos de menores (RGPD). Nada de datos de salud en la web.
- **Diseño**: Figma, fileKey `k9Z3RmFeCKsRn1GRyHbHxZ`, frames "Inicio · Escritorio" (4:186) e "Inicio · Móvil" (4:196). Azul marino + amarillo, titulares en cursiva y cortes diagonales.
  - En el diseño, "Club Azul / Deportivo Femenino" es provisional: usar **Almonte Doñana**.
  - Los textos del diseño están en femenino ("juntas", "nosotras"). Como el club es mixto, usar redacción **inclusiva/neutra**.
- **Fotos**: provisionales hasta tener las reales. Los logos (escudo y patrocinadores) los aporta el usuario.
- **v2 (pospuesto)**: noticias, galería de fotos/vídeos, clasificación, marcador en tiempo real, pagos online y área de socios.
- **Hosting**: sin decidir (recomendado Railway). Repositorio: https://github.com/denmicc73/voleibalDonana (rama main).

## Stack
- Lenguaje: Java 25 (LTS), Maven 3.9 con wrapper
- Framework / CMS: Spring Boot 4.1.x (Web MVC, Thymeleaf, Security, Data JPA, Validation, Flyway)
- Base de datos: MySQL 8 en prod, H2 en memoria en dev; esquema con Flyway (src/main/resources/db/migration)
- Despliegue / hosting: Docker, hosting recomendado Railway, config por variables de entorno, SPRING_PROFILES_ACTIVE=prod

## Comandos
- Instalar dependencias: mvnw.cmd clean install -DskipTests
- Ejecutar en local: mvnw.cmd spring-boot:run -Pdev -Dspring-boot.run.profiles=dev (sin perfil de Spring la app no arranca)
- Ejecutar tests: mvnw.cmd test
- Linter / formato: formato IntelliJ (4 espacios); mvnw.cmd verify antes de commit

## Convenciones
- Estilo de código: paquete base es.voleibaldonana; capas controller / controller.admin / service / repository / model / DTO / config / exception; formularios vía DTO con Bean Validation; sin lógica en controladores; nunca ddl-auto=update en prod
- Nombres de ramas y commits: ramas feature/<fase>-<tema>, fix/<tema>; commits en español en imperativo
- Idioma de comentarios y documentación: español (comentarios, documentación y textos de la web)

## Reglas importantes
- No subir claves ni archivos .env al repositorio.
- Todo cambio se registra en CHANGELOG.md (archivo solo local, está en .gitignore y no se sube), y en cada iteración se hacen commits con buenos mensajes y **push**.
- Avanzar por fases y pedir permiso al usuario antes de empezar cada una.
- Credenciales solo por variables de entorno (.env.example sin valores).
- CSRF siempre activo.
- Datos de menores con consentimiento del tutor y acceso solo ADMIN.

## Flujo de agentes
1. agente-arquitectura (planificar)
2. agente-codigo (implementar)
3. agente-testing (probar)
4. agente-revision (revisar el cambio)
5. agente-seguridad (auditar antes de publicar)
6. agente-documentacion (documentar)
7. agente-seo (si es una web)
