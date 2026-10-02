# oms-starter-audit-core

Starter de auditoría y trazabilidad para microservicios Spring Boot 4.1.1 (Java 21).

Los eventos de auditoría se publican de forma asíncrona en una **cola de Azure Service Bus**
(en reemplazo de Apache Kafka usado en versiones anteriores).

## Publicar en Maven local

```bash
./gradlew publishToMavenLocal
```

## Dependencia en el microservicio

```gradle
implementation 'pe.edu.galaxy.training.java:oms-starter-audit-core:3.0.3'
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
    service-bus:
      # Obligatorio: connection string del namespace/cola (clave compartida SAS).
      # Service Bus NO usa DefaultAzureCredential/az login en este starter
      # (reservado exclusivamente para Azure Key Vault en oms-starter-security-core).
      connection-string: ${AZURE_SERVICEBUS_CONNECTION_STRING:}
      queue-name: oms-audit-queue
```

## Uso

```java
@Auditable(operation = "CREATE", entity = "CLIENT", description = "Crear cliente")
public ClientResponse create(ClientRequest request) {
    return service.create(request);
}
```

El starter registra automáticamente una fila en la tabla `audit_logs` para operaciones exitosas y con error,
y publica cada evento como mensaje JSON en la cola de Azure Service Bus configurada.
