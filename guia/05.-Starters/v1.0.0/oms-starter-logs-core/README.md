# oms-starter-logs-core

Starter corporativo de logging para microservicios Spring Boot.

Incluye:

- Logging JSON con Logback + logstash-logback-encoder.
- TraceId y CorrelationId usando MDC.
- Filtro HTTP para registrar requests y responses.
- Aspecto AOP para registrar operaciones de negocio.
- Configuración dinámica por `application.yml`.

## Instalación local

```bash
./gradlew publishToMavenLocal
```

## Uso en un microservicio

```gradle
implementation 'pe.edu.galaxy.training.java:oms-starter-logs-core:1.0.0'
```

## Configuración

```yaml
spring:
  application:
    name: ms-business-management-clients

oms:
  logs:
    enabled: true
    service-name: ms-business-management-clients
    trace-header-name: X-Trace-Id
    correlation-header-name: X-Correlation-Id
    include-headers: true
    excluded-paths:
      - /actuator/health
      - /swagger-ui
      - /v3/api-docs
```

## Uso de operaciones de negocio

```java
@LogOperation(value = "CLIENT_CREATE", businessKey = "client")
public ClientResponse create(ClientRequest request) {
    return service.create(request);
}
```

## Headers soportados

- `X-Trace-Id`
- `X-Correlation-Id`

Si el cliente no envía los headers, el starter los genera automáticamente.

## Formato JSON esperado

```json
{
  "@timestamp": "2026-05-13T10:00:00.000Z",
  "level": "INFO",
  "logger_name": "...",
  "message": "HTTP_REQUEST method=POST uri=/api/v1/clients",
  "traceId": "...",
  "correlationId": "...",
  "serviceName": "ms-business-management-clients",
  "application": "ms-business-management-clients"
}
```
