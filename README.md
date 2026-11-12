# Club Voleibol Doñana - Almonte

Web del club de voleibol de Almonte: marcadores, inscripción por categorías y patrocinadores. Estado: Fase 0 (esqueleto).

## Requisitos
- JDK 25
- Maven 3.9 (o el wrapper incluido)

## Arrancar en local
```
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```
Abrir http://localhost:8080. En dev se usa H2 en memoria (consola en `/h2-console`).

## Tests
```
mvnw.cmd test
```

## Producción
Configurar las variables de `.env.example` (`SPRING_PROFILES_ACTIVE=prod`, `DB_URL`, `DB_USER`, `DB_PASSWORD`...). No subir nunca `.env` al repositorio.
