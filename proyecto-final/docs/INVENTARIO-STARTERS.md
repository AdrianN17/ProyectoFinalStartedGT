# Inventario de Starters (Pattern 01)

Inventario de los starters consumidos por `bank-creditcard-service`: los 4 Galaxy Starters
corporativos desarrollados durante el curso *Starter Development* (`guia/05.-Starters/v2.0.0/`,
via el BOM `oms-starter-bom-core`) y el starter propio `andes-api-toolkit` (via `andes-api-bom`).

## Tabla resumen

| # | Starter | Version | GroupId:ArtifactId | Funcionalidad principal | Componentes clave | Escenario de uso | Beneficio |
|---|---|---|---|---|---|---|---|
| 1 | `oms-starter-bom-core` | 2.0.0 | `pe.edu.galaxy.training.java.bom:oms-starter-bom-core` | BOM (Bill of Materials) que centraliza las versiones de los demas starters | `pom.xml` con `dependencyManagement` | Importarlo como `platform(...)` en cualquier microservicio | Evita conflictos de versiones y estandariza upgrades corporativos |
| 2 | `oms-starter-logs-core` | 1.0.0 | `pe.edu.galaxy.training.java:oms-starter-logs-core` | Logging estructurado (JSON) con correlacion de trazas | `LogsAutoConfiguration`, `TraceFilter`, `OperationLoggingAspect`, `@LogOperation` | Servicios REST que necesitan trazabilidad extremo a extremo (`X-Trace-Id` / `X-Correlation-Id`) | Logs uniformes y correlacionables entre microservicios sin codigo repetido |
| 3 | `oms-starter-audit-core` | 2.0.0 | `pe.edu.galaxy.training.java:oms-starter-audit-core` | Auditoria de operaciones de negocio publicada en Kafka | `AuditAutoConfig`, `KafkaAutoConfig`, `AuditAspect`, `@Auditable` | Registrar quien hizo que operacion (CREATE/UPDATE/DELETE) y su resultado | Trazabilidad de negocio y cumplimiento normativo sin acoplar el codigo al productor Kafka |
| 4 | `oms-starter-security-core` | 1.0.0 | `pe.edu.galaxy.training.java:oms-starter-security-core` | Cifrado (Vault Transit) y enmascarado de datos sensibles | `SensitiveSecurityAutoConfiguration`, `SensitiveRepositoryAspect`, `@Encrypt`, `@Mask`, `@Sensitive` | Entidades JPA con PII (email, telefono, documento) | Proteccion de datos sensibles en reposo y en las respuestas JSON, de forma declarativa |
| 5 | `oms-starter-observability-core` | 1.0.0 | `pe.edu.galaxy.training.java:oms-starter-observability-core` | Metricas de negocio, health checks y exposicion Prometheus | `ObservabilityAutoConfiguration`, `ObservedMetricAspect`, `@ObservedMetric`, Actuator | Monitoreo de metodos de negocio y salud del servicio | Observabilidad lista para Grafana/Prometheus sin configurar Micrometer manualmente |

## Detalle por starter

### 1. oms-starter-bom-core (BOM)
- **Contenido administrado**: `oms-starter-logs-core:1.0.0`, `oms-starter-audit-core:2.0.0`,
  `oms-starter-security-core:1.0.0`, `oms-starter-observability-core:1.0.0`.
- **Publicacion**: `mvn clean install` (Maven Local).
- **Consumo**: `implementation platform('pe.edu.galaxy.training.java.bom:oms-starter-bom-core:2.0.0')`.

### 2. oms-starter-logs-core
- **Propiedades**: prefijo `oms.logs` (`enabled`, `service-name`, `trace-header-name`,
  `correlation-header-name`, `log-request-body`, `log-response-body`, `excluded-paths`).
- **Anotacion**: `@LogOperation(value, businessKey)` a nivel de metodo.
- **Uso**: `CreditCardServiceImpl` (emision, bloqueo, autorizacion de transacciones).

### 3. oms-starter-audit-core
- **Propiedades**: prefijo `oms.audit` (`enabled`, `log-request`, `log-response`, `log-errors`,
  `kafka.bootstrap-servers`, `kafka.topic-name`).
- **Anotacion**: `@Auditable(operation, entity, description)` a nivel de metodo.
- **Uso**: `CreditCardServiceImpl` -> topico `topic-audit`.

### 4. oms-starter-security-core
- **Propiedades**: prefijo `oms.sensitive` (`encrypt.enabled`, `encrypt.provider`,
  `encrypt.vault.*`, `mask.enabled`, `audit.enabled`).
- **Anotaciones**: `@Encrypt` (campo), `@Mask(type, visibleStart, visibleEnd, maskChar)` (campo),
  `@Sensitive(level, category)` (campo).
- **Uso**: `CreditCardEntity.cardNumber` (`@Mask(CARD)`) y `.cvv` (`@Mask(FULL)`), ambos `@Encrypt`
  + `@Sensitive(HIGH, "PCI")`.

### 5. oms-starter-observability-core
- **Propiedades**: prefijo `oms.observability` (`business-metrics-enabled`,
  `method-metrics-enabled`, `health-enabled`), mas `management.endpoints.web.exposure.include`.
- **Anotacion**: `@ObservedMetric(name, description, operation)` a nivel de metodo.
- **Uso**: `creditcard.issue`, `creditcard.updateStatus`, `creditcard.transaction.authorize`.

### 6. andes-api-toolkit (starter propio)

A diferencia de los cinco starters anteriores (reutilizados de `guia/`, permitido por la Seccion
I.c de la rubrica), `andes-api-toolkit/` (raiz del workspace) es un ecosistema de starters
**implementado por el alumno** en otro curso (*Java Library Development*), con enfoque API-first
(OpenAPI 3): `andes-api-common` (modelos/excepciones), `andes-api-server` +
`andes-api-server-spring-boot-starter` (correlation id, wrapping de respuestas en
`{success, data, error, metadata}`, manejo centralizado de errores, OpenAPI) y `andes-api-client` +
`andes-api-client-spring-boot-starter` (clientes REST tipados con timeouts, headers y mapeo de
errores HTTP a excepciones).

- **Propiedades**: prefijos `andes.api.server.*` y `andes.api.client.clients.*`.
- **Uso en este proyecto**: `andes-api-server` expone `contracts/openapi-creditcard.yaml`
  (`CreditCardController`); `andes-api-client` implementa el cliente nombrado `fraudCheck` que
  llama al servicio de scoring de fraude (`FraudCheckClient` -> `FraudCheckSimulatorController`).
- Ver [andes-api-toolkit/README.md](../../andes-api-toolkit/README.md).

## Proyecto integrado

`bank-creditcard-service` combina los 4 Galaxy Starters simultaneamente sobre un dominio de banca
real (tarjetas de credito), apilando `@ObservedMetric` + `@Auditable` + `@LogOperation` sobre el
mismo metodo de servicio y `@Encrypt` + `@Mask` + `@Sensitive` sobre la misma entidad JPA, **y**
ademas usa el starter propio `andes-api-toolkit` para exponer la API (contrato OpenAPI) y para
consumir el servicio externo de scoring de fraude. Ver [../README.md](../README.md).
