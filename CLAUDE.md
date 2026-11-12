# Contexto del proyecto
Leer siempre

## Qué es
Una pagina web para mi club de volleyball.Es un club de almonte,en la pagina quiero poder poner los marcadores y que las personas se puedan apuntar segun sus 
categorias.Aparte de patrocinar el club.

## Stack
- Lenguaje: Java 25 (LTS), Maven 3.9 con wrapper
- Framework / CMS: Spring Boot 4.1.x (Web MVC, Thymeleaf, Security, Data JPA, Validation, Flyway)
- Base de datos: MySQL 8 en prod, H2 en memoria en dev; esquema con Flyway (src/main/resources/db/migration)
- Despliegue / hosting: Docker, hosting recomendado Railway, config por variables de entorno, SPRING_PROFILES_ACTIVE=prod

## Comandos
- Instalar dependencias: mvnw.cmd clean install -DskipTests
- Ejecutar en local: mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
- Ejecutar tests: mvnw.cmd test
- Linter / formato: formato IntelliJ (4 espacios); mvnw.cmd verify antes de commit

## Convenciones
- Estilo de código: paquete base es.voleibaldonana; capas controller / controller.admin / service / repository / model / DTO / config / exception; formularios vía DTO con Bean Validation; sin lógica en controladores; nunca ddl-auto=update en prod
- Nombres de ramas y commits: ramas feature/<fase>-<tema>, fix/<tema>; commits en español en imperativo
- Idioma de comentarios y documentación: español (comentarios, documentación y textos de la web)

## Reglas importantes
- No subir claves ni archivos .env al repositorio.
- Todo cambio se registra en CHANGELOG.md.
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
