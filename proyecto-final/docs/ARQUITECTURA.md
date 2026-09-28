# Arquitectura del Proyecto Final (Pattern 10)

## Vision general

```mermaid
graph TB
    subgraph "guia/05.-Starters/v2.0.0 (solo lectura, se publica a Maven Local)"
        BOM["oms-starter-bom-core:2.0.0<br/>(BOM)"]
        LOGS["oms-starter-logs-core"]
        AUDIT["oms-starter-audit-core"]
        SEC["oms-starter-security-core"]
        OBS["oms-starter-observability-core"]
    end

    subgraph "andes-api-toolkit (raiz del workspace, se publica a Maven Local)"
        ANDESBOM["andes-api-bom:1.0.0-SNAPSHOT"]
        ASERVER["andes-api-server-spring-boot-starter"]
        ACLIENT["andes-api-client-spring-boot-starter"]
    end

    subgraph "proyecto-final/ (bank-creditcard-service, proyecto unico)"
        APP["CreditCardController / Service / Entity"]
        FRAUD["FraudCheckSimulatorController<br/>(interno, autocontenido)"]
    end

    BOM -. gestiona versiones .-> LOGS
    BOM -. gestiona versiones .-> AUDIT
    BOM -. gestiona versiones .-> SEC
    BOM -. gestiona versiones .-> OBS
    ANDESBOM -. gestiona versiones .-> ASERVER
    ANDESBOM -. gestiona versiones .-> ACLIENT

    LOGS --> APP
    AUDIT --> APP
    SEC --> APP
    OBS --> APP
    ASERVER --> APP
    ACLIENT --> APP

    APP -->|topic-audit| KAFKA[(Kafka)]
    APP -->|transit encrypt/decrypt| VAULT[(HashiCorp Vault)]
    APP -->|/actuator/prometheus| PROM[(Prometheus)]
    ACLIENT -->|HTTP self-call| FRAUD
```

## Flujo: autorizar una transaccion de tarjeta de credito

```mermaid
sequenceDiagram
    participant C as Cliente HTTP
    participant F as TraceFilter (logs-core)
    participant Corr as CorrelationIdFilter (andes-api-server)
    participant Ctrl as CreditCardController
    participant Svc as CreditCardServiceImpl
    participant Asp as Aspects (Audit/Log/Observability)
    participant Repo as CreditCardRepository (JPA)
    participant SecAsp as SensitiveRepositoryAspect (security-core)
    participant Client as FraudCheckClient (andes-api-client)
    participant Fraud as FraudCheckSimulatorController
    participant DB as H2
    participant Kafka as Kafka (topic-audit)

    C->>F: POST /api/v1/credit-cards/{id}/transactions
    F->>F: TraceId/CorrelationId (MDC)
    F->>Corr: X-Correlation-Id
    Corr->>Ctrl: request
    Ctrl->>Svc: authorizeTransaction(cardId, request)
    Svc->>Asp: @ObservedMetric + @Auditable + @LogOperation
    Asp->>Repo: findById(cardId)
    Repo->>SecAsp: @AfterReturning find*(..)
    SecAsp->>SecAsp: descifra cardNumber/cvv (@Encrypt)
    Svc->>Client: evaluate(cardId, merchant, amount)
    Client->>Fraud: POST /internal/fraud-check
    Fraud-->>Client: riskScore
    Client-->>Svc: FraudCheckResponse
    Svc->>Repo: save(transaction) + save(card actualizado)
    Repo->>SecAsp: @Before save(..)
    SecAsp->>DB: UPDATE (valores cifrados)
    Asp->>Kafka: publica evento de auditoria (async)
    Asp-->>Svc: metrica registrada (Micrometer)
    Svc-->>Ctrl: TransactionResponse
    Ctrl-->>C: 201 Created {success, data, error, metadata}
```

## Estructura modular (Pattern 02)

Cada starter en `guia/05.-Starters/v2.0.0/<starter>` y en `andes-api-toolkit/` sigue su propia
variante *core* (con `AutoConfiguration.imports`, anotaciones propias y `properties` tipadas).
Este proyecto es el *consumidor* unico de todos ellos:

```
proyecto-final/
├── settings.gradle              # rootProject.name = bank-creditcard-service
├── build.gradle                  # BOMs Galaxy + Andes, dependencias, JaCoCo
├── contracts/
│   └── openapi-creditcard.yaml   # contrato API-first (Pattern 03)
├── src/main/java/.../creditcard/
│   ├── entity / repository        # persistencia + security-core
│   ├── service / service.impl     # logs-core + audit-core + observability-core
│   ├── fraud/                     # andes-api-client (cliente) + simulador (andes-api-server)
│   ├── controller                  # andes-api-server (envelope, correlation, errores)
│   └── dto / mapper / commons
└── src/test                       # pruebas unitarias + integracion
```
