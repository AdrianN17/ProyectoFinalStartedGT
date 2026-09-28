# Proyecto Final - Starter Development (Galaxy Training)

**`bank-creditcard-service`**: microservicio bancario de **Tarjetas de Credito** que integra, en
un unico proyecto Spring Boot, los 4 Galaxy Starters v2.0.0 (`guia/05.-Starters/v2.0.0/`: logs,
audit, security, observability) **y** el starter propio `andes-api-toolkit/` (server + client,
enfoque API-first con OpenAPI), tal como exige la rubrica del curso *Starter Development*.

> Este directorio vive **fuera** de `guia/` a proposito: `guia/` sera eliminado despues de las
> pruebas del curso, por lo que todo el trabajo nuevo del proyecto final se hizo aqui.

## Contenido

| Ruta | Que es | Rubrica |
|---|---|---|
| [docs/INVENTARIO-STARTERS.md](docs/INVENTARIO-STARTERS.md) | Inventario completo de los starters (BOM + 4 Galaxy + Andes) | Pattern 01 |
| [docs/ARQUITECTURA.md](docs/ARQUITECTURA.md) | Diagramas de arquitectura y secuencia (Mermaid) | Pattern 02 / 10 |
| [contracts/openapi-creditcard.yaml](contracts/openapi-creditcard.yaml) | Contrato OpenAPI 3 del API de Tarjetas de Credito (API-first) | Pattern 03 |
| [src/main/java/.../creditcard](src/main/java/pe/edu/galaxy/training/java/gt/creditcard) | Proyecto integrado: dominio Banca / Tarjeta de Credito usando los 5 starters a la vez | Pattern 02-09 |
| [scripts/publish-starters.ps1](scripts/publish-starters.ps1) | Automatiza la publicacion de starters + BOMs (Galaxy y Andes) en Maven Local | Pattern 09 |

## Caso de uso: Banca - Tarjeta de Credito

- **Emitir tarjeta** (`POST /api/v1/credit-cards`): numero de tarjeta y CVV cifrados en reposo
  (`@Encrypt`) y enmascarados en la respuesta JSON (`@Mask`), clasificados como datos PCI
  (`@Sensitive`) &mdash; `oms-starter-security-core`.
- **Consultar / bloquear tarjeta** (`GET`, `PATCH /{cardId}/status`).
- **Autorizar una transaccion** (`POST /{cardId}/transactions`): valida el limite de credito,
  llama a un servicio de **scoring de fraude** a traves de `andes-api-client-spring-boot-starter`
  (cliente nombrado `fraudCheck`, con timeouts/errores estandarizados) y aprueba o rechaza la
  transaccion segun el riesgo devuelto.
- Cada operacion queda auditada en Kafka (`oms-starter-audit-core`), registrada con trazas
  correlacionadas (`oms-starter-logs-core`), medida con metricas de negocio
  (`oms-starter-observability-core`) y expuesta bajo el envelope estandar `{success, data, error,
  metadata}` con manejo centralizado de errores (`andes-api-server-spring-boot-starter`).

## Requisitos previos

- JDK 21
- Los starters Galaxy, su BOM, y el BOM + artefactos de `andes-api-toolkit` deben estar publicados
  en Maven Local (`~/.m2/repository`). Ejecutar una vez:

```powershell
./scripts/publish-starters.ps1
```

Este script recorre `guia/05.-Starters/v2.0.0/` (gradlew publishToMavenLocal + mvn install del
BOM) y `andes-api-toolkit/` (mvn install), sin modificar nada dentro de esas carpetas.

> Los Galaxy Starters (Spring Boot 3.5.6 originalmente) y `andes-api-toolkit` (Spring Boot 4.1.1)
> deben resolver a una version de Spring Boot compatible entre si para convivir en el mismo
> classpath de este proyecto unico; ese ajuste de versiones se gestiona directamente en los
> starters de origen.

## Compilar y ejecutar

```bash
# Compilar
./gradlew build

# Ejecutar (puerto 8080)
./gradlew bootRun

# Ejecutar las pruebas (unitarias + integracion) con cobertura JaCoCo
./gradlew test jacocoTestReport
```

## Probar el flujo completo

```bash
# 1) Emitir una tarjeta
curl -X POST http://localhost:8080/api/v1/credit-cards \
  -H "Content-Type: application/json" \
  -d '{"cardHolderName":"Ana Lopez","cardNumber":"4111111111111111","cvv":"123","expirationMonth":12,"expirationYear":2030,"creditLimit":5000}'

# 2) Autorizar una compra de bajo riesgo (aprobada)
curl -X POST http://localhost:8080/api/v1/credit-cards/1/transactions \
  -H "Content-Type: application/json" \
  -d '{"merchant":"Amazon","amount":200,"type":"PURCHASE"}'

# 3) Autorizar una compra de alto monto (rechazada por el scoring de fraude)
curl -X POST http://localhost:8080/api/v1/credit-cards/1/transactions \
  -H "Content-Type: application/json" \
  -d '{"merchant":"Suspicious Shop","amount":4000,"type":"PURCHASE"}'

# 4) Bloquear la tarjeta
curl -X PATCH http://localhost:8080/api/v1/credit-cards/1/status \
  -H "Content-Type: application/json" -d '{"status":"BLOCKED"}'

# 5) Metricas y salud
curl http://localhost:8080/actuator/prometheus | grep creditcard
curl http://localhost:8080/actuator/health
```

La respuesta llega envuelta en `{success, data, error, metadata}` (andes-api-server), con el
numero de tarjeta/CVV ya enmascarados (oms-starter-security-core).

## SemVer y versionamiento

- Los starters Galaxy (`guia/05.-Starters/v2.0.0/`) versionan `oms-starter-audit-core` en `2.0.0`
  mientras que `logs-core`, `security-core` y `observability-core` permanecen en `1.0.0`; el BOM
  (`2.0.0`) referencia siempre la combinacion compatible mas reciente.
- `andes-api-toolkit` versiona todos sus modulos en `1.0.0-SNAPSHOT`, gestionados por
  `andes-api-bom`.
- Este proyecto (`bank-creditcard-service`) parte en `1.0.0` (primera version estable del proyecto
  final) y debe incrementarse siguiendo SemVer (`MAJOR.MINOR.PATCH`).

## Infraestructura opcional (para demo con integraciones reales)

Por defecto el proyecto funciona sin infraestructura externa (H2 en memoria, Vault/Kafka
deshabilitados en pruebas; el scoring de fraude usa un simulador interno autocontenido). Para una
demostracion completa:

```bash
# Kafka (oms-starter-audit-core)
docker run -d --name kafka -p 9092:9092 apache/kafka:3.7.0

# Vault Transit (oms-starter-security-core)
docker run --cap-add=IPC_LOCK -e VAULT_DEV_ROOT_TOKEN_ID=root \
  -e VAULT_DEV_LISTEN_ADDRESS=0.0.0.0:8200 -p 8200:8200 hashicorp/vault
docker exec -it <container_id> sh -c "vault login root && vault secrets enable transit && vault write -f transit/keys/oms-key"
```

Detalle completo del setup de Vault: `guia/05.-Starters/v2.0.0/oms-starter-security-core/docker-vault-test.md`.

## Testing y calidad (Pattern 08)

- `CreditCardServiceImplTest`: pruebas unitarias (JUnit 5 + Mockito) sobre la logica de negocio
  (emision, bloqueo, validacion de limite, aprobacion/rechazo por fraude).
- `CreditCardServiceApplicationTests`: pruebas de integracion (`@SpringBootTest` + `MockMvc`) que
  levantan el contexto completo (5 starters activos) y validan el enmascarado de datos sensibles,
  el flujo real de autorizacion contra el simulador de fraude (via `andes-api-client`), y el
  mapeo de errores de negocio a HTTP (404/409/422) por `andes-api-server`.
- Cobertura: `./gradlew test jacocoTestReport` (reporte en `build/reports/jacoco`).
