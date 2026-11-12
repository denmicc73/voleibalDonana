# Club Voleibol Almonte Doñana

Web del club de voleibol Almonte Doñana (Almonte, Huelva): marcadores, inscripción por categorías y patrocinadores. Estado: Fase 0 (esqueleto).

## Requisitos
- JDK 25
- Maven 3.9 (o el wrapper incluido)

## Arrancar en local
```
mvnw.cmd spring-boot:run -Pdev -Dspring-boot.run.profiles=dev
```
Abrir http://localhost:8080. En dev se usa H2 en memoria (consola en `/h2-console`).

`-Pdev` (perfil Maven) añade H2 y su consola, que no van en el jar de producción.
`-Dspring-boot.run.profiles=dev` activa el perfil de Spring.

**Sin perfil de Spring la aplicación no arranca**: hay que activar siempre `dev`, `prod` o `test`.

## Tests y verificación
```
mvnw.cmd test
mvnw.cmd verify
```
Ejecuta `mvnw.cmd verify` antes de cada commit.

## Producción
Se empaqueta sin el perfil Maven `dev` (`mvnw.cmd clean package`), así el jar no incluye H2.
Variables de entorno necesarias (ver `.env.example`):

| Variable | Descripción |
|----------|-------------|
| `SPRING_PROFILES_ACTIVE` | Debe ser `prod` |
| `DB_URL` | URL JDBC de MySQL, p. ej. `jdbc:mysql://host:3306/base` |
| `DB_USER` | Usuario de la base de datos |
| `DB_PASSWORD` | Contraseña de la base de datos |

No subir nunca `.env` al repositorio.
