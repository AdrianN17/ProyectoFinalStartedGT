# Proyecto Final - Starter Development (Galaxy Training)

**`bank-creditcard-service`**: microservicio bancario de **Tarjetas de Credito** que integra, en
un unico proyecto Spring Boot 4.1.1 / Java 21, los 4 Galaxy Starters v3.0.0 (`guia/05.-Starters/v2.0.0/`:
logs, audit, security, observability) **y** el toolkit `andes-api-toolkit` (server + client,
enfoque API-first con OpenAPI), resuelto desde el repositorio corporativo Nexus, tal como exige
la rubrica del curso *Starter Development*.

> Este directorio vive **fuera** de `guia/` a proposito: `guia/` sera eliminado despues de las
> pruebas del curso, por lo que todo el trabajo nuevo del proyecto final se hizo aqui.

## Contenido

| Ruta | Que es | Rubrica |
|---|---|---|
| [docs/INVENTARIO-STARTERS.md](docs/INVENTARIO-STARTERS.md) | Inventario completo de los starters (BOM + 4 Galaxy + Andes) | Pattern 01 |
| [docs/ARQUITECTURA.md](docs/ARQUITECTURA.md) | Diagramas de arquitectura y secuencia (Mermaid) | Pattern 02 / 10 |
| [contracts/openapi-creditcard.yaml](contracts/openapi-creditcard.yaml) | Contrato OpenAPI 3 del API de Tarjetas de Credito (API-first, lado servidor) | Pattern 03 |
| [contracts/openapi-fraudcheck.yaml](contracts/openapi-fraudcheck.yaml) | Contrato OpenAPI 3 del API externa de Fraud Check (lado cliente; modelos generados con `openApiGenerate`) | Pattern 03 |
| [scripts/mock_fraudcheck_server.py](scripts/mock_fraudcheck_server.py) | Mock standalone en Python (sin dependencias) del contrato de Fraud Check, para probar `FraudCheckClient` en aislamiento | Pattern 09 |
| [src/main/java/.../creditcard](src/main/java/pe/edu/galaxy/training/java/gt/creditcard) | Proyecto integrado: dominio Banca / Tarjeta de Credito usando los 5 starters a la vez | Pattern 02-09 |
| [scripts/publish-starters.ps1](scripts/publish-starters.ps1) | Automatiza la publicacion de starters + BOMs (Galaxy y Andes) en Maven Local | Pattern 09 |
| [scripts/publish-starters-to-nexus.ps1](scripts/publish-starters-to-nexus.ps1) / [.sh](scripts/publish-starters-to-nexus.sh) | Automatiza la publicacion de los 4 Galaxy Starters + BOM en el Nexus corporativo (`maven-releases`/`maven-snapshots`) | Pattern 09 |

## Caso de uso: Banca - Tarjeta de Credito

- **Emitir tarjeta** (`POST /api/v1/credit-cards`): numero de tarjeta y CVV cifrados en reposo
  (`@Encrypt`) y enmascarados en la respuesta JSON (`@Mask`), clasificados como datos PCI
  (`@Sensitive`) &mdash; `oms-starter-security-core`.
- **Consultar / bloquear tarjeta** (`GET`, `PATCH /{cardId}/status`).
- **Autorizar una transaccion** (`POST /{cardId}/transactions`): valida el limite de credito,
  llama a un servicio de **scoring de fraude** a traves de `andes-api-client-spring-boot-starter`
  (cliente nombrado `fraudCheck`, con timeouts/errores estandarizados) y aprueba o rechaza la
  transaccion segun el riesgo devuelto.

### Los 2 contratos OpenAPI del proyecto

El proyecto usa `andes-api-toolkit` en sus dos facetas (servidor y cliente), cada una con su
propio contrato OpenAPI:

1. **Servidor** ([contracts/openapi-creditcard.yaml](contracts/openapi-creditcard.yaml)): el API
   de Tarjetas de Credito que este servicio **expone**. `CreditCardController` lo implementa a
   mano (API-first); `andes-api-server-spring-boot-starter` aporta el envelope `{success, data,
   error, metadata}`, correlation id y manejo de errores (`AndesResponseBodyAdvice`).
2. **Cliente** ([contracts/openapi-fraudcheck.yaml](contracts/openapi-fraudcheck.yaml)): el API
   externa de Fraud Check que este servicio **consume** via `FraudCheckClient`
   (`andes-api-client-spring-boot-starter`, cliente nombrado `fraudCheck`). Los modelos
   `FraudCheckRequest`/`FraudCheckResponse` **no se escriben a mano**: se generan en build time
   desde el contrato con `openapi-generator-gradle-plugin` (tarea `openApiGenerate`, paquete
   `...creditcard.fraud.generated`), igual que el patron usado en
   `andes-api-toolkit/examples/poc-client`.
   - Este contrato lo satisfacen dos implementaciones intercambiables: el simulador interno
     `FraudCheckSimulatorController` (Java, mismo proceso) y el mock standalone
     [scripts/mock_fraudcheck_server.py](scripts/mock_fraudcheck_server.py) (Python, proceso
     independiente, sin el envoltorio `ApiResponse` que aplica `AndesResponseBodyAdvice` a todo
     el proceso Java) — util para probar el cliente de forma aislada, como lo haria un servicio
     externo real:
     ```bash
     python3 scripts/mock_fraudcheck_server.py --port 9090
     # y en application.yml: andes.api.client.clients.fraudCheck.base-url=http://localhost:9090
     ```
- Cada operacion queda auditada en **Azure Service Bus** (`oms-starter-audit-core`): en este
  proyecto, cuando el namespace usa SKU **Basic**, se publica en la cola `queue-audit`,
  registrada con trazas correlacionadas (`oms-starter-logs-core`), medida con metricas de negocio
  (`oms-starter-observability-core`) y expuesta bajo el envelope estandar `{success, data, error,
  metadata}` con manejo centralizado de errores (`andes-api-server-spring-boot-starter`).

## Requisitos previos

- JDK 21
- Los starters Galaxy y su BOM deben estar publicados en Maven Local (`~/.m2/repository`).
  Ejecutar una vez:

```powershell
./scripts/publish-starters.ps1
```

Este script recorre `guia/05.-Starters/v2.0.0/` (`gradlew publishToMavenLocal` + `mvn install`
del BOM), sin modificar nada dentro de esas carpetas.

- El toolkit `andes-api-toolkit` (`pe.andes.api:*`, incluido su BOM `andes-api-bom`) **ya no se
  construye localmente**: se resuelve directamente desde el repositorio corporativo **Nexus**
  (`http://localhost:8089/repository/maven-releases`, version `1.0.0`), configurado en
  `build.gradle`. Para que Gradle pueda autenticarse contra Nexus, define las credenciales en
  **uno** de estos lugares (nunca las subas al repositorio ni las pongas directamente en
  `build.gradle`):

  **Opcion A — `~/.gradle/gradle.properties`** (recomendada para desarrollo local; este archivo
  vive en tu `home`, fuera del repo git):
  ```properties
  nexusUser=tu-usuario-nexus
  nexusPassword=tu-password-nexus
  ```

  **Opcion B — variables de entorno** (recomendada para CI/CD):
  ```bash
  export ORG_GRADLE_PROJECT_nexusUser=tu-usuario-nexus
  export ORG_GRADLE_PROJECT_nexusPassword=tu-password-nexus
  ```

  Si Nexus corre en local via Docker con HTTP plano (sin TLS), los repos ya tienen
  `allowInsecureProtocol = true` en `build.gradle`; en un Nexus corporativo real con HTTPS no
  deberia ser necesario.

> Los Galaxy Starters y `andes-api-toolkit` corren ambos sobre **Spring Boot 4.1.1 / Java 21**
> (ver migracion documentada en cada starter), por lo que conviven sin conflicto de versiones en
> el classpath de este proyecto unico.

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

- Los starters Galaxy (`guia/05.-Starters/v2.0.0/`) y su BOM versionan todos en `3.0.0`
  (migracion a Spring Boot 4.1.1 / Java 21 + Azure Service Bus/Key Vault).
- `andes-api-toolkit` versiona todos sus modulos en `1.0.0`, gestionados por
  `andes-api-bom`, y se resuelve desde el repositorio Nexus (`maven-releases`).
- Este proyecto (`bank-creditcard-service`) parte en `1.0.0` (primera version estable del proyecto
  final) y debe incrementarse siguiendo SemVer (`MAJOR.MINOR.PATCH`).

## Infraestructura opcional (para demo con integraciones reales)

Por defecto el proyecto funciona sin infraestructura externa: H2 en memoria, Azure Service
Bus/Key Vault deshabilitados o simulados en pruebas (ver
[docs/ISSUES-CONOCIDOS.md](docs/ISSUES-CONOCIDOS.md) para el detalle de como se simula Key Vault
en tests), y el scoring de fraude usa un simulador interno autocontenido. Para una demostracion
completa contra Azure real:

```bash
# Azure Service Bus (oms-starter-audit-core): crear namespace + cola en Azure (SKU Basic)
az servicebus namespace create --name <mi-namespace> --resource-group <mi-rg> --sku Basic
az servicebus queue create --name queue-audit --namespace-name <mi-namespace> --resource-group <mi-rg>
# Luego exportar la connection string (oms.audit.service-bus.connection-string):
export AZURE_SERVICEBUS_CONNECTION_STRING="Endpoint=sb://<mi-namespace>.servicebus.windows.net/;..."

# Azure Key Vault (oms-starter-security-core): autenticacion via DefaultAzureCredential (az login)
az login
az keyvault create --name <mi-keyvault> --resource-group <mi-rg> --location eastus
az keyvault key create --vault-name <mi-keyvault> --name oms-key --kty RSA --size 2048
export AZURE_KEYVAULT_URL="https://<mi-keyvault>.vault.azure.net"
```

Detalle completo del setup de Key Vault:
`guia/05.-Starters/v2.0.0/oms-starter-security-core/azure-keyvault-test.md`.

## Testing y calidad (Pattern 08)

- `CreditCardServiceImplTest`: pruebas unitarias (JUnit 5 + Mockito) sobre la logica de negocio
  (emision, bloqueo, validacion de limite, aprobacion/rechazo por fraude).
- `CreditCardServiceApplicationTests`: pruebas de integracion (`@SpringBootTest` + `MockMvc`) que
  levantan el contexto completo (5 starters activos) y validan el cifrado/enmascarado de datos
  sensibles (Key Vault simulado con un `CryptographyClient` mockeado, ver
  `config/KeyVaultTestConfig.java` y [docs/ISSUES-CONOCIDOS.md](docs/ISSUES-CONOCIDOS.md)), el
  flujo real de autorizacion contra el simulador de fraude (via `andes-api-client`), y el mapeo
  de errores de negocio a HTTP (404/409/422) por `andes-api-server`.
- Cobertura: `./gradlew test jacocoTestReport` (reporte en `build/reports/jacoco`).
