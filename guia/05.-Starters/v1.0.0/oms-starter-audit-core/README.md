# oms-starter-audit-core

Starter de auditoría y trazabilidad para microservicios Spring Boot 3.

## Publicar en Maven local

```bash
./gradlew publishToMavenLocal
```

## Dependencia en el microservicio

```gradle
implementation 'pe.edu.galaxy.training.java:oms-starter-audit-core:1.0.0'
```

## Configuración

```yaml
oms:
  audit:
    enabled: true
    service-name: ms-business-management-clients
    log-request: true
    log-response: true
    log-errors: true
    max-payload-length: 8000
```

## Uso

```java
@Auditable(operation = "CREATE", entity = "CLIENT", description = "Crear cliente")
public ClientResponse create(ClientRequest request) {
    return service.create(request);
}
```

El starter registra automáticamente una fila en la tabla `audit_logs` para operaciones exitosas y con error.
