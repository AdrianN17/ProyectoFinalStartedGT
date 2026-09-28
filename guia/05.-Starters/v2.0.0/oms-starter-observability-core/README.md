# oms-starter-observability

Starter para observabilidad en microservicios Spring Boot.

Incluye:

- Spring Boot Actuator
- Micrometer
- Prometheus endpoint
- Health checks personalizados
- Métricas HTTP y de negocio
- Anotación `@ObservedMetric`
- Demo local con Prometheus y Grafana

## Publicar local

```bash
./gradlew clean publishToMavenLocal
```

## Consumir en microservicio

```gradle
implementation 'pe.edu.galaxy.training.java:oms-starter-observability:1.0.0'
```

## application.yml

```yaml
oms:
  observability:
    enabled: true
    service-name: ms-business-management-clients
    business-metrics-enabled: true
    method-metrics-enabled: true
    health-enabled: true

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  prometheus:
    metrics:
      export:
        enabled: true
```

## Endpoints

```text
/actuator/health
/actuator/info
/actuator/metrics
/actuator/prometheus
```
