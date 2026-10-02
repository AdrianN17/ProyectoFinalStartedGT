# Inventario de Starters (Pattern 01)

Inventario de los starters consumidos por `bank-creditcard-service`: los 4 Galaxy Starters
corporativos desarrollados durante el curso *Starter Development* (`guia/05.-Starters/v2.0.0/`,
via el BOM `oms-starter-bom-core`, migrados a **Spring Boot 4.1.1 / Java 21** con componentes
**Azure** en reemplazo de Kafka/HashiCorp Vault) y el toolkit `andes-api-toolkit` (via
`andes-api-bom`), resuelto desde el repositorio corporativo **Nexus** en vez de construirse
localmente.

## Tabla resumen

| # | Starter | Version | GroupId:ArtifactId | Funcionalidad principal | Componentes clave | Escenario de uso | Beneficio |
|---|---|---|---|---|---|---|---|
| 1 | `oms-starter-bom-core` | 3.0.0 | `pe.edu.galaxy.training.java.bom:oms-starter-bom-core` | BOM (Bill of Materials) que centraliza las versiones de los demas starters | `pom.xml` con `dependencyManagement` | Importarlo como `platform(...)` en cualquier microservicio | Evita conflictos de versiones y estandariza upgrades corporativos |
| 2 | `oms-starter-logs-core` | 3.0.0 | `pe.edu.galaxy.training.java:oms-starter-logs-core` | Logging estructurado (JSON) con correlacion de trazas | `LogsAutoConfiguration`, `TraceFilter`, `OperationLoggingAspect`, `@LogOperation` | Servicios REST que necesitan trazabilidad extremo a extremo (`X-Trace-Id` / `X-Correlation-Id`) | Logs uniformes y correlacionables entre microservicios sin codigo repetido |
| 3 | `oms-starter-audit-core` | 3.0.0 | `pe.edu.galaxy.training.java:oms-starter-audit-core` | Auditoria de operaciones de negocio publicada en **Azure Service Bus** (en este proyecto se enruta a cola por SKU Basic) | `AuditAutoConfig`, `ServiceBusAutoConfig`, `AuditAspect`, `@Auditable` | Registrar quien hizo que operacion (CREATE/UPDATE/DELETE) y su resultado | Trazabilidad de negocio y cumplimiento normativo sin acoplar el codigo al productor Service Bus |
| 4 | `oms-starter-security-core` | 3.0.0 | `pe.edu.galaxy.training.java:oms-starter-security-core` | Cifrado (**Azure Key Vault**, operaciones criptograficas sobre clave administrada) y enmascarado de datos sensibles | `SensitiveSecurityAutoConfiguration`, `SensitiveRepositoryAspect`, `@Encrypt`, `@Mask`, `@Sensitive` | Entidades JPA con PII (email, telefono, documento) | Proteccion de datos sensibles en reposo y en las respuestas JSON, de forma declarativa |
| 5 | `oms-starter-observability-core` | 3.0.0 | `pe.edu.galaxy.training.java:oms-starter-observability-core` | Metricas de negocio, health checks y exposicion Prometheus | `ObservabilityAutoConfiguration`, `ObservedMetricAspect`, `@ObservedMetric`, Actuator | Monitoreo de metodos de negocio y salud del servicio | Observabilidad lista para Grafana/Prometheus sin configurar Micrometer manualmente |

## Detalle por starter

### 1. oms-starter-bom-core (BOM)
- **Contenido administrado**: `oms-starter-logs-core:3.0.0`, `oms-starter-audit-core:3.0.0`,
  `oms-starter-security-core:3.0.0`, `oms-starter-observability-core:3.0.0`.
- **Publicacion**: `mvn clean install` (Maven Local).
- **Consumo**: `implementation platform('pe.edu.galaxy.training.java.bom:oms-starter-bom-core:3.0.0')`.

### 2. oms-starter-logs-core
- **Propiedades**: prefijo `oms.logs` (`enabled`, `service-name`, `trace-header-name`,
  `correlation-header-name`, `log-request-body`, `log-response-body`, `excluded-paths`).
- **Anotacion**: `@LogOperation(value, businessKey)` a nivel de metodo.
- **Uso**: `CreditCardServiceImpl` (emision, bloqueo, autorizacion de transacciones).

### 3. oms-starter-audit-core
- **Propiedades**: prefijo `oms.audit` (`enabled`, `log-request`, `log-response`, `log-errors`,
  `service-bus.connection-string`, `service-bus.queue-name`).
- **Autenticacion**: Service Bus se autentica **solo con connection string** (clave compartida
  SAS); no usa `DefaultAzureCredential`/`az login` (reservado exclusivamente a Key Vault).
- **Anotacion**: `@Auditable(operation, entity, description)` a nivel de metodo.
- **Uso**: `CreditCardServiceImpl` -> cola `queue-audit`.

### 4. oms-starter-security-core
- **Propiedades**: prefijo `oms.sensitive` (`encrypt.enabled`, `encrypt.provider`,
  `encrypt.azure-key-vault.*` [`vault-url`, `key-name`, `key-version`, `algorithm`],
  `mask.enabled`, `audit.enabled`).
- **Autenticacion**: Azure Key Vault se autentica con `DefaultAzureCredential` (en local: `az
  login`; en Azure: Managed Identity).
- **Anotaciones**: `@Encrypt` (campo), `@Mask(type, visibleStart, visibleEnd, maskChar)` (campo),
  `@Sensitive(level, category)` (campo).
- **Uso**: `CreditCardEntity.cardNumber` (`@Mask(CARD)`) y `.cvv` (`@Mask(FULL)`), ambos `@Encrypt`
  + `@Sensitive(HIGH, "PCI")`.

### 5. oms-starter-observability-core
- **Propiedades**: prefijo `oms.observability` (`business-metrics-enabled`,
  `method-metrics-enabled`, `health-enabled`), mas `management.endpoints.web.exposure.include`.
- **Anotacion**: `@ObservedMetric(name, description, operation)` a nivel de metodo.
- **Uso**: `creditcard.issue`, `creditcard.updateStatus`, `creditcard.transaction.authorize`.

### 6. andes-api-toolkit (starter propio, resuelto desde Nexus)

A diferencia de los cinco starters anteriores (reutilizados de `guia/`, permitido por la Seccion
I.c de la rubrica), `andes-api-toolkit` es un ecosistema de starters **implementado por el
alumno** en otro curso (*Java Library Development*), con enfoque API-first (OpenAPI 3). Todos
sus artefactos se usan en `bank-creditcard-service`:

- `andes-api-common`: modelos/excepciones (`ApiResponse`/`ApiError`/`ApiMetadata`,
  `AndesNotFoundException`, etc.), usado transitivamente por server y client.
- `andes-api-server` + `andes-api-server-spring-boot-starter`: correlation id, wrapping de
  respuestas en `{success, data, error, metadata}`, manejo centralizado de errores, OpenAPI.
  Expone `contracts/openapi-creditcard.yaml` (`CreditCardController`).
- `andes-api-client` + `andes-api-client-spring-boot-starter`: clientes REST tipados con
  timeouts, headers y mapeo de errores HTTP a excepciones. Implementa el cliente nombrado
  `fraudCheck` (`FraudCheckClient` -> `FraudCheckSimulatorController` o
  `scripts/mock_fraudcheck_server.py`), segun el contrato `contracts/openapi-fraudcheck.yaml`.
- `andes-id-generator-spring-boot-starter` (bean `IdGeneratorService`): genera el
  `referenceId` (ULID con prefijo `TXN-`) de cada transaccion en
  `CreditCardServiceImpl.authorizeTransaction` — forma recomendada (inyectable, mockeable en
  tests) de usar la libreria clasica `andes-id-generator`.
- `andes-id-generator` (libreria clasica, sin Spring, metodos `static`): se usa directamente
  (sin pasar por el bean) `ChecksumUtils.sha256Hex(...)` para calcular la clave de idempotencia
  de cada transaccion, como lo haria cualquier proyecto Java puro sin Spring.
- `andes-text-utils` (libreria clasica, sin Spring): `SlugUtils.slugify(...)` normaliza el
  nombre del comercio (`merchantSlug`); `MaskUtils.maskDigits(...)` enmascara el numero de
  tarjeta en el log tecnico de auditoria (`log.info(...)`) — distinto de la anotacion `@Mask`
  de `oms-starter-security-core`, que enmascara el **JSON de respuesta**, no las lineas de log.

Su codigo fuente vive en un repositorio separado (`ProyectoFinalLibreriaGT/andes-api-toolkit/`,
fuera de este workspace) y **no se construye localmente**: `bank-creditcard-service` lo resuelve
como dependencia binaria desde el repositorio corporativo **Nexus**
(`http://localhost:8089/repository/maven-releases`, version `1.0.0`), configurado en
`build.gradle` con credenciales via `findProperty('nexusUser')` / `findProperty('nexusPassword')`
(ver [../README.md](../README.md), seccion "Requisitos previos", para como configurarlas).

- **Propiedades**: prefijos `andes.api.server.*` y `andes.api.client.clients.*` (el starter de
  id-generator no tiene propiedades: el bean `IdGeneratorService` se auto-registra).
- **Nota de integracion**: dado que `AndesResponseBodyAdvice` (andes-api-server) envuelve
  *todas* las respuestas del proceso -incluido el simulador interno de fraude-,
  `FraudCheckClient` deserializa explicitamente a `ApiResponse<FraudCheckResponse>` y extrae
  `.getData()`, en vez de mapear directo al tipo de negocio (ver
  [ISSUES-CONOCIDOS.md](ISSUES-CONOCIDOS.md) para el detalle).

## Proyecto integrado

`bank-creditcard-service` combina los 4 Galaxy Starters simultaneamente sobre un dominio de banca
real (tarjetas de credito), apilando `@ObservedMetric` + `@Auditable` + `@LogOperation` sobre el
mismo metodo de servicio y `@Encrypt` + `@Mask` + `@Sensitive` sobre la misma entidad JPA, **y**
ademas usa el toolkit `andes-api-toolkit` (resuelto desde Nexus) para exponer la API (contrato
OpenAPI) y para consumir el servicio externo de scoring de fraude. Ver [../README.md](../README.md).
