# OMS Service Template

Base Spring Boot microservice template with REST APIs, JPA, validation, Swagger/OpenAPI, MySQL and Actuator.

## Technologies

- Java 21
- Spring Boot 3.5.6
- Gradle
- Spring Web
- Spring Data JPA
- Validation
- MySQL
- Lombok
- Springdoc OpenAPI
- Actuator

## Build

```bash
./gradlew clean build
```

On Windows:

```bash
gradlew.bat clean build
```

## Run

```bash
./gradlew bootRun
```

On Windows:

```bash
gradlew.bat bootRun
```

Default URL:

```txt
http://localhost:8081
```

## Profiles

Run with a specific profile:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Available profile files:

```txt
application.yml
application-dev.yml
application-qa.yml
application-prod.yml
```

## Swagger

Swagger UI:

```txt
http://localhost:8081/swagger-ui.html
```

OpenAPI JSON:

```txt
http://localhost:8081/v3/api-docs
```

## Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/products` | Get all products |
| GET | `/api/v1/products/paging?page=0&size=10&sort=name,asc` | Get paginated products |
| GET | `/api/v1/products/{id}` | Get product by ID |
| POST | `/api/v1/products` | Create product |
| PUT | `/api/v1/products/{id}` | Update product |
| DELETE | `/api/v1/products/{id}` | Delete product |

## Sample Request

```json
{
  "name": "Laptop Lenovo ThinkPad",
  "price": 3500.00,
  "stock": 15
}
```

## Docker

Build project first:

```bash
./gradlew clean build
```

Build Docker image:

```bash
docker build -t oms-service-template:1.0.0 .
```

Run Docker container:

```bash
docker run -p 8081:8081 oms-service-template:1.0.0
```

## Actuator

Health:

```txt
http://localhost:8081/actuator/health
```

Metrics:

```txt
http://localhost:8081/actuator/metrics
```
