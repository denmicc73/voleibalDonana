# Club Voleibol Almonte Doñana

Web del club de voleibol Almonte Doñana (Almonte, Huelva): marcadores, inscripción por categorías y patrocinadores. Estado: Fase 2 (acceso al panel de administración: login, roles, usuarios y cambio de contraseña). La portada sigue con datos de ejemplo.

Estructura de la parte visual: plantillas Thymeleaf en `src/main/resources/templates` (fragmentos comunes en `fragments/`, legales en `legal/`, errores en `error/`, panel en `admin/`) y recursos estáticos en `src/main/resources/static` (`css/`, `js/`, `fonts/` auto-alojadas y `img/`). El escudo y la foto actuales son provisionales.

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

## Panel de administración
Entrada en `/admin/login` (no aparece en el menú de la web). El usuario es el email.

- **Roles**: *Administración* (ADMIN) entra en todo, incluidos usuarios e inscripciones; *Edición de resultados* (EDITOR) solo en partidos y calendario.
- **Primer administrador**: al arrancar, si no hay ningún ADMIN, se crea con `ADMIN_EMAIL` y `ADMIN_PASSWORD` (y `ADMIN_NOMBRE`, opcional). Después cambia la contraseña en *Mi cuenta*; las variables ya no se usan mientras exista un administrador.
- **En dev** sin esas variables se crea `admin@dev.local` con una contraseña aleatoria que sale en el log (`[SOLO DEV]`).
- **Contraseñas**: de 12 a 72 caracteres, guardadas con BCrypt.
- **Intentos fallidos**: 5 seguidos bloquean la cuenta 15 minutos (`club.acceso.max-intentos` y `club.acceso.bloqueo`). Un ADMIN puede desbloquearla desde *Usuarios*.
- Al desactivar una cuenta se cierran sus sesiones abiertas.
- **Contraseña olvidada**: no hay recuperación por email. Un ADMIN pone una temporal con *Restablecer contraseña* en *Usuarios* y la persona la cambia en *Mi cuenta*.

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
| `ADMIN_EMAIL` | Email del primer administrador (solo se usa si no hay ninguno) |
| `ADMIN_PASSWORD` | Su contraseña inicial, de 12 a 72 caracteres (cámbiala después en *Mi cuenta*) |
| `ADMIN_NOMBRE` | Opcional; nombre que se muestra en el panel (por defecto "Administración") |

No subir nunca `.env` al repositorio.
