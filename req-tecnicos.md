# Requerimientos técnicos — Andes API Toolkit

Documento pensado para que **otra IA (o desarrollador) en otro proyecto** pueda reimplementar o consumir cada módulo del toolkit sin ver el código original. Describe el contrato público, el comportamiento exacto y las reglas de cada módulo.

- **groupId**: `pe.andes.api` · **versión**: `1.0.0-SNAPSHOT` · **paquete base**: `pe.andes.api.*` / `pe.andes.lib.*`
- **Stack**: Java 21, Maven multi-módulo, Spring Boot 4.1.1, Jackson, RestClient (no WebClient), OpenAPI 3.0.3, `openapi-generator-maven-plugin` 7.11.0.
- **Tests**: JUnit 5.10.2, Mockito 5.11.0, WireMock 3.5.4 (`wiremock-standalone`), `spring-boot-starter-test`. Cobertura con JaCoCo 0.8.12.
- **Raíz del código**: `andes-api-toolkit/`.

---

## 1. Grafo de módulos y reglas de dependencia

```
andes-api-bom                      (solo versiones)
andes-api-common                   (Java puro + Jackson)
   ├─> andes-api-server            (Spring MVC)  ─> andes-api-server-spring-boot-starter
   └─> andes-api-client            (RestClient)  ─> andes-api-client-spring-boot-starter
andes-text-utils                   (Java puro, 0 deps)
andes-id-generator                 (Java puro, 0 deps) ─> andes-id-generator-spring-boot-starter
examples/{poc-server, poc-client, poc-integration, poc-classic-libs, poc-wrapper-demo}
```

Reglas:
1. `andes-api-common`, `andes-text-utils` y `andes-id-generator` **no pueden depender de Spring**.
2. Cada starter = librería + una única clase `@AutoConfiguration` + archivo de registro (ver §8).
3. Todas las versiones se gestionan en el `pom.xml` padre (`dependencyManagement`) y en el BOM; los módulos hijos declaran dependencias **sin versión**.
4. Orden de módulos en el reactor: bom, common, server, client, starters, text-utils, id-generator, id-generator-starter, ejemplos.

### 1.1 POM padre (`andes-api-toolkit/pom.xml`)
- `packaging=pom`, propiedades: `java.version=21`, `maven.compiler.release=21`, `spring-boot.version=4.1.1`, `project.build.sourceEncoding=UTF-8`, versiones de plugins y de test (ver arriba), `swagger-models.version=2.2.22`, `swagger-annotations.version=2.2.22`.
- `dependencyManagement`: importa `andes-api-bom` (`${project.version}`) y `spring-boot-dependencies` (`${spring-boot.version}`), más `junit-jupiter`, `mockito-core`, `wiremock-standalone` (scope test) y `io.swagger.core.v3:swagger-models`.
- `pluginManagement`: compiler (`release=21`, `parameters=true`), surefire, failsafe, javadoc (`doclint=none`, `failOnError=false`), source, openapi-generator, enforcer (Java `[21,)`, Maven `[3.9,)`, `dependencyConvergence`), jacoco (`prepare-agent` + `report` en fase `test`).
- Plugins activos por defecto: compiler, surefire, jacoco.
- Perfil `release`: adjunta `maven-source-plugin:jar-no-fork` y `maven-javadoc-plugin:jar`.
- `distributionManagement`: ids `nexus-releases` / `nexus-snapshots`, URLs por propiedad `nexus.releases.url` / `nexus.snapshots.url` (por defecto `file://${maven.multiModuleProjectDirectory}/.local-nexus-repo/...`).
- Metadatos requeridos para publicar: `licenses` (Apache 2.0), `developers`, `url`, `description`.

---

## 2. `andes-api-bom`

POM `packaging=pom` con `dependencyManagement` que lista todos los artefactos del ecosistema (`andes-api-common`, `-server`, `-client`, ambos starters, `andes-text-utils`, `andes-id-generator`, `andes-id-generator-spring-boot-starter`) con `${project.version}`. Consumidores externos lo importan con `<scope>import</scope><type>pom</type>`.

---

## 3. `andes-api-common` (Java puro)

**Dependencias**: `jackson-databind`, `jackson-datatype-jsr310`, `jackson-module-parameter-names`. Tests: JUnit, Mockito.
**Convención**: modelos `final`, inmutables, `Serializable` (`serialVersionUID=1L`), con getters, `equals/hashCode/toString`, y `Builder` estático donde se indica. Utilidades `final`, constructor privado, métodos `static`.

### 3.1 Modelos (`pe.andes.api.common.model`)

| Clase | Campos | Notas |
|---|---|---|
| `ApiResponse<T>` | `boolean success`, `T data`, `ApiError error`, `ApiMetadata metadata` | Fábricas: `success(data)`, `success(data, metadata)`, `error(ApiError)`, `error(ApiError, metadata)`. `success` ⇒ error=null; `error` ⇒ data=null. Getter `isSuccess()`. Es el **envelope estándar** `{success,data,error,metadata}`. |
| `ApiError` | `code`, `message`, `int httpStatus`, `traceId`, `Instant timestamp`, `List<ApiErrorDetail> details` | `timestamp` null ⇒ `Instant.now()`; `details` null ⇒ lista vacía; copia inmutable (`List.copyOf`). Tiene `builder()`. `equals/hashCode` ignoran `timestamp`. |
| `ApiErrorDetail` | `field`, `code`, `message`, `Object rejectedValue` | Fábricas `of(field, message)` y `of(field, code, message)`. |
| `ApiMetadata` | `traceId`, `correlationId`, `requestId`, `apiVersion`, `Instant timestamp` | `timestamp` null ⇒ now. `builder()`. `equals` ignora timestamp. |
| `ApiRequestMetadata` | `correlationId`, `requestId`, `apiVersion`, `Instant receivedAt` | `receivedAt` null ⇒ now. `builder()`. |
| `Pagination` | `int page` (base 0), `int size`, `long totalElements`, `int totalPages`, `boolean first`, `boolean last` | `Pagination.of(page,size,total)`: `totalPages = size==0 ? 0 : ceil(total/size)`, `first = page==0`, `last = page >= totalPages-1`. |
| `PageResponse<T>` | `List<T> content`, `Pagination pagination` | `PageResponse.of(content, page, size, totalElements)`. |

### 3.2 Excepciones (`pe.andes.api.common.exception`)

`AndesApiException` es **abstracta**, extiende `RuntimeException`. Constructor protegido `(String errorCode, int httpStatus, String message, List<ApiErrorDetail> details, String traceId, Throwable cause)`; getters `getErrorCode()`, `getHttpStatus()`, `getDetails()` (nunca null), `getTraceId()`.

| Subclase | `ERROR_CODE` | `HTTP_STATUS` |
|---|---|---|
| `AndesBadRequestException` | `BAD_REQUEST` | 400 |
| `AndesAuthenticationException` | `AUTHENTICATION_ERROR` | 401 |
| `AndesAuthorizationException` | `AUTHORIZATION_ERROR` | 403 |
| `AndesNotFoundException` | `NOT_FOUND` | 404 |
| `AndesConflictException` | `CONFLICT` | 409 |
| `AndesValidationException` | `VALIDATION_ERROR` | 422 |
| `AndesRemoteServiceException` | `REMOTE_SERVICE_ERROR` | 502 (+ campos `endpoint`, `remoteHttpStatus`) |

Cada subclase expone constantes públicas `ERROR_CODE` y `HTTP_STATUS` y constructores `(message)`, `(message, cause)` y `(message, details, traceId, cause)`. `AndesValidationException` añade `(message, details)`. `AndesRemoteServiceException` usa `(message, endpoint, remoteHttpStatus, cause)` y `(message, endpoint, remoteHttpStatus, details, traceId, cause)`.

### 3.3 HTTP (`pe.andes.api.common.http`)
- `AndesHeaders`: `X-Correlation-Id`, `X-Request-Id`, `X-Api-Version`, `X-Trace-Id`, `Content-Type`, `Accept`, `Authorization`.
- `AndesContentTypes`: `application/json`, `application/problem+json`, `application/xml`, `text/plain`.
- `AndesHttpStatus`: ints 200, 201, 204, 400, 401, 403, 404, 409, 422, 500, 502, 503, 504.
- `AndesApiConstants`: `CONFIG_PREFIX="andes.api"`, `MDC_CORRELATION_ID="correlationId"`, `MDC_REQUEST_ID="requestId"`, `MDC_TRACE_ID="traceId"`, `DEFAULT_API_VERSION="v1"`.

### 3.4 Utilidades (`pe.andes.api.common.util`)

| Clase | Métodos / comportamiento |
|---|---|
| `HeaderUtils` | Regex de ids `^[a-zA-Z0-9\-]{8,64}$`. `isValidCorrelationId`, `isValidRequestId` (null ⇒ false). `generateCorrelationId()`/`generateRequestId()` ⇒ `UUID.randomUUID().toString()`. `defaultIfInvalid(value, valid)` ⇒ valor o nuevo correlation id. |
| `ValidationUtils` | `isBlank`, `isNotBlank`, `isEmpty(Collection)` (null o vacía), `requireNonBlank(value, message)` (lanza `IllegalArgumentException`), `isValidEmail` con regex `^[\w.+-]+@[\w-]+\.[a-zA-Z]{2,}$`. |
| `JsonUtils` | `ObjectMapper` compartido: `JavaTimeModule` + `ParameterNamesModule`, `FAIL_ON_UNKNOWN_PROPERTIES=false`. `toJson(Object)`, `fromJson(String, Class<T>)` (errores ⇒ `IllegalStateException`), `sharedObjectMapper()`. |
| `ErrorUtils` | `toApiError(AndesApiException, traceId)` (copia code/message/httpStatus/details) y `toApiError(code, httpStatus, message, traceId)`. |
| `ApiResponseUtils` | `ok(data[, metadata])`, `fail(error[, metadata])`, `isSuccessful(ApiResponse<?>)`. |
| `OpenApiUtils` | `isValidOperationId` (`^[a-zA-Z][a-zA-Z0-9]*$`), `isValidSemanticVersion` (SemVer con pre-release/build), `normalizeApiVersion(raw, default)` (blank ⇒ default, si no `trim()`). |

---

## 4. `andes-api-server` (Spring MVC)

**Dependencias**: `andes-api-common`, `spring-web`, `spring-webmvc`, `spring-boot`, `spring-boot-autoconfigure`, `jakarta.validation-api`, `jakarta.servlet-api` (provided), `slf4j-api`, `swagger-models`.

### 4.1 `CorrelationIdFilter extends OncePerRequestFilter`
Constructor `(boolean generateIfMissing)`. Por request:
1. Lee `X-Correlation-Id` y `X-Request-Id`. Si el valor es válido (`HeaderUtils`) lo usa; si no, genera un UUID si `generateIfMissing`, de lo contrario deja el valor recibido.
2. Pone ambos en **MDC** (`correlationId`, `requestId`) y los devuelve como headers de respuesta.
3. Loguea `>> METHOD URI` y `<< METHOD URI -> status (N ms)`.
4. En `finally` limpia el MDC.

### 4.2 `GlobalExceptionHandler` (`@RestControllerAdvice`)
Constructor `(List<AndesExceptionMapper<?>> customMappers, boolean includeStackTrace)`. Todas las respuestas son `ResponseEntity<ApiResponse<Void>>` con `ApiResponse.error(...)`. `traceId` = `MDC["correlationId"]`.

| Excepción | Status | Detalle |
|---|---|---|
| `AndesApiException` | `ex.getHttpStatus()` | `ErrorUtils.toApiError(ex, traceId)`; log WARN |
| `MethodArgumentNotValidException` | 422 | code `VALIDATION_ERROR`, message `Request validation failed`, `details` = un `ApiErrorDetail(field, code, defaultMessage)` por field error |
| `ConstraintViolationException` | 422 | idem; detail `(propertyPath, message)` |
| `ServerWebInputException` | 400 | `BAD_REQUEST`, `"Malformed request: " + reason` |
| `Exception` (fallback) | 500 o del mapper | primero busca un `AndesExceptionMapper` cuyo `getExceptionType().isInstance(ex)`; si no, `INTERNAL_SERVER_ERROR` con mensaje `An unexpected error occurred` (o `ex.toString()` si `includeStackTrace`); log ERROR |

Extension point: `interface AndesExceptionMapper<E extends Throwable> { Class<E> getExceptionType(); int getHttpStatus(); ApiError map(E ex, String traceId); }`. Cualquier bean de este tipo se inyecta automáticamente y se evalúa en orden.

### 4.3 `AndesResponseBodyAdvice` (`ResponseBodyAdvice<Object>`)
- `supports`: true si el tipo de retorno del método **no** es `ApiResponse`.
- `beforeBodyWrite`: si el body ya es `ApiResponse` lo devuelve; si no, `ApiResponse.success(body, ApiMetadata{traceId=correlationId MDC, correlationId, requestId})`.
- **Caveat**: si el contrato OpenAPI ya define el envelope (clases generadas `XxxEnvelope`) hay que desactivarlo (`andes.api.server.response.wrap-enabled=false`) para evitar doble envoltura.

### 4.4 `AndesOpenApiFactory.build(AndesServerProperties.OpenApi)` → `io.swagger.v3.oas.models.OpenAPI`
Construye `Info` (title, description, version, contact opcional, license solo si hay `name`), `servers`, `tags` (solo si no vacíos) y `components.securitySchemes` (tipo/`in` convertidos con `valueOf(upperCase)`; campos `scheme`, `bearerFormat`, `name`).

### 4.5 `AndesServerProperties` (`@ConfigurationProperties("andes.api.server")`)

```yaml
andes.api.server:
  response:       { wrap-enabled: true }
  error-handling: { enabled: true, include-stack-trace: false }
  correlation:    { enabled: true, generate-if-missing: true }
  openapi:
    enabled: true
    title: "Andes API"        # defaults: title "Andes API", description "", version "1.0.0"
    description: ""
    version: 1.0.0
    contact: { name, email, url }
    license: { name, url }
    servers: [ { url, description } ]
    tags:    [ { name, description } ]
    security-schemes:
      bearerAuth: { type: http, scheme: bearer, bearer-format: JWT }   # también: in, name (apiKey)
```

---

## 5. `andes-api-server-spring-boot-starter`

- Dependencias: `andes-api-common`, `andes-api-server`, `spring-boot-autoconfigure`; `spring-boot-configuration-processor` (optional); `spring-boot-starter-web` y `spring-boot-starter-validation` (**provided**); `swagger-models` (optional).
- Clase `AndesApiServerAutoConfiguration`: `@AutoConfiguration`, `@ConditionalOnWebApplication(SERVLET)`, `@EnableConfigurationProperties(AndesServerProperties.class)`. Beans (todos `@ConditionalOnMissingBean`):

| Bean | Condición `@ConditionalOnProperty` (todos `matchIfMissing=true`) | Detalle |
|---|---|---|
| `FilterRegistrationBean<CorrelationIdFilter>` | `andes.api.server.correlation.enabled` | orden `Ordered.HIGHEST_PRECEDENCE`, URL pattern `/*` |
| `GlobalExceptionHandler` | `andes.api.server.error-handling.enabled` | recibe `List<AndesExceptionMapper<?>>` e `includeStackTrace` |
| `AndesResponseBodyAdvice` | `andes.api.server.response.wrap-enabled` | |
| `OpenAPI` | `andes.api.server.openapi.enabled` + `@ConditionalOnClass(OpenAPI.class)` | `AndesOpenApiFactory.build(props.getOpenapi())` |

- Registro: ver §8.

---

## 6. `andes-api-client` (Spring `RestClient`)

**Dependencias**: `andes-api-common`, `spring-web`, `spring-boot`, `spring-boot-autoconfigure`, `slf4j-api`; test: WireMock.

### 6.1 `AndesClientProperties` (`@ConfigurationProperties("andes.api.client")`)
`Map<String, ClientConfig> clients`. `ClientConfig`: `String baseUrl`, `Duration connectTimeout=2s`, `Duration readTimeout=5s`, `boolean correlationIdEnabled=true`, `boolean requestIdEnabled=true`, `Map<String,String> defaultHeaders`.

### 6.2 `AndesRestClientFactory.createClient(name, config, customizers)`
- `SimpleClientHttpRequestFactory` con `connectTimeout`/`readTimeout`, envuelto en `BufferingClientHttpRequestFactory` (permite releer el body en interceptor y error handler).
- `RestClient.builder()` con `baseUrl`, `requestFactory`, `AndesClientHeaderInterceptor(config)` y `defaultStatusHandler(HttpStatusCode::isError, ...)`: lee el body (UTF-8) y **lanza** `errorMapper.map(uri, status, body)`.
- Luego aplica cada `AndesRestClientCustomizer.customize(clientName, builder)` y devuelve `builder.build()`.

### 6.3 `AndesClientHeaderInterceptor` (`ClientHttpRequestInterceptor`)
Solo agrega un header si la request **no lo trae ya**:
1. `X-Correlation-Id` (si `correlationIdEnabled`): valor del MDC si es válido; si no, UUID nuevo.
2. `X-Request-Id` (si `requestIdEnabled`): UUID nuevo.
3. `Content-Type: application/json`.
4. Cada entrada de `defaultHeaders`.
5. Loguea `>> METHOD URI` y `<< METHOD URI -> status (N ms)`.

### 6.4 `AndesClientErrorMapper.map(endpoint, statusCode, responseBody) → AndesApiException`
1. Intenta parsear el body como `ApiError` con `JsonUtils`; si es nulo/blanco o falla ⇒ sin error remoto.
2. `message` = mensaje remoto, o `"Remote call to {endpoint} failed with status {status}"`; `details`/`traceId` se copian del error remoto si existe.
3. Mapeo: 400→`AndesBadRequestException`, 401→`AndesAuthenticationException`, 403→`AndesAuthorizationException`, 404→`AndesNotFoundException`, 409→`AndesConflictException`, 422→`AndesValidationException`, resto→`AndesRemoteServiceException(message, endpoint, status, details, traceId, null)`.

### 6.5 `AndesApiClient` (wrapper tipado)
Constructor `(String name, RestClient restClient)`. Métodos: `getName()`, `raw()` (acceso al `RestClient`), `get(uri, Class<T>)`, `get(uri, ParameterizedTypeReference<T>)`, `post(uri, body, Class<T>)`, `post(uri, body, ParameterizedTypeReference<T>)`, `put(uri, body, Class<T>)`, `patch(uri, body, Class<T>)`, `delete(uri)` (usa `toBodilessEntity`). Todos hacen `.retrieve().body(type)`; los errores salen como excepciones Andes por el status handler.

### 6.6 `AndesApiClientRegistry`
Constructor `(Map<String, AndesApiClient>)` (copia inmutable). `get(name)` lanza `IllegalArgumentException("No Andes API client configured with name: " + name)` si no existe; `getAll()`.

### 6.7 `AndesRestClientCustomizer`
`@FunctionalInterface void customize(String clientName, RestClient.Builder builder)`. Beans de este tipo se aplican a todos los clientes (filtrar por `clientName`). Útil para auth, interceptores extra, etc.

---

## 7. `andes-api-client-spring-boot-starter`

- Dependencias: `andes-api-common`, `andes-api-client`, `spring-boot-autoconfigure`, `spring-boot-configuration-processor` (optional), `spring-boot-starter` (provided).
- `AndesApiClientAutoConfiguration`: `@AutoConfiguration`, `@EnableConfigurationProperties(AndesClientProperties.class)`. Beans con `@ConditionalOnMissingBean`: `AndesClientErrorMapper`, `AndesRestClientFactory(errorMapper)`, `AndesApiClientRegistry(properties, factory, List<AndesRestClientCustomizer>)` — este último itera `properties.getClients()`, crea un `RestClient` por entrada y registra `new AndesApiClient(name, restClient)`.

Config e uso:

```yaml
andes.api.client.clients:
  orders:
    base-url: ${ORDERS_API_URL}
    connect-timeout: 2s
    read-timeout: 5s
    correlation-id-enabled: true
    request-id-enabled: true
    default-headers: { X-Api-Key: ${ORDERS_API_KEY} }
```
```java
this.ordersClient = registry.get("orders");
Order o = ordersClient.get("/v2/orders/" + id, Order.class);
```

---

## 8. Registro de autoconfiguración (patrón para los 3 starters)

Archivo `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` con **una línea por clase** (FQCN):

- server starter: `pe.andes.api.server.autoconfigure.AndesApiServerAutoConfiguration`
- client starter: `pe.andes.api.client.autoconfigure.AndesApiClientAutoConfiguration`
- id-generator starter: `pe.andes.lib.id.autoconfigure.AndesIdGeneratorAutoConfiguration`

(No usar `spring.factories`.) Los tests de starters usan `ApplicationContextRunner` / `WebApplicationContextRunner` verificando beans presentes, `@ConditionalOnMissingBean` y desactivación por propiedad.

---

## 9. `andes-text-utils` (librería clásica, 0 dependencias)

Paquete `pe.andes.lib.text`. Clases `final`, constructor privado que lanza `AssertionError`, solo métodos `static`.

- `SlugUtils.slugify(String)`: null/blank ⇒ `""`; normaliza NFD, elimina diacríticos (`\p{InCombiningDiacriticalMarks}+`), `toLowerCase(Locale.ROOT)`, reemplaza `[^a-z0-9]+` por `-`, recorta guiones en los extremos (`^-+|-+$`).
- `SlugUtils.truncate(input, maxLength, suffix)`: null ⇒ `""`; `maxLength<0` ⇒ `IllegalArgumentException`; si cabe devuelve igual; si no, `substring(0, max(0, maxLength - suffix.length())) + suffix` (suffix null ⇒ `""`).
- `MaskUtils.maskEmail(email)`: null/blank o sin `@` (o `@` en posición 0) ⇒ devuelve igual; si no, primer carácter + `*` × max(1, len(user)-1) + dominio (`ada@x.com` → `a**@x.com`).
- `MaskUtils.maskDigits(digits, visibleDigits)`: null/blank ⇒ igual; `visibleDigits<0` ⇒ `IllegalArgumentException`; reemplaza por `*` todo salvo los últimos `visibleDigits` (acotados a la longitud).

---

## 10. `andes-id-generator` (librería clásica, 0 dependencias)

Paquete `pe.andes.lib.id`. Mismas convenciones que §9.

- `UlidGenerator.generate()`: 26 caracteres. 10 chars de timestamp (`Clock.systemUTC().millis()`, 5 bits por char, alfabeto Crockford `0123456789ABCDEFGHJKMNPQRSTVWXYZ`, más significativo primero) + 16 chars de aleatoriedad (10 bytes de `SecureRandom` codificados en base32, truncado a 16). Ordenable lexicográficamente por tiempo.
- `UlidGenerator.withPrefix(prefix)`: `prefix + "-" + generate()`; prefix null/blank ⇒ solo el id.
- `ChecksumUtils.crc32(String)`: null/vacío ⇒ `0L`; CRC32 de bytes UTF-8 (`long`).
- `ChecksumUtils.sha256Hex(String)`: null ⇒ `""`; SHA-256 UTF-8 en hex minúscula de 64 chars.

---

## 11. `andes-id-generator-spring-boot-starter` (librería de envoltura)

Patrón: **envolver una librería estática en un bean inyectable sin modificarla**.
- Dependencias: `andes-id-generator`, `spring-boot-autoconfigure`, `spring-boot-configuration-processor` (optional), `spring-boot-starter` (provided).
- `IdGeneratorService` (POJO, sin anotaciones): `newId()` → `UlidGenerator.generate()`; `newId(String prefix)` → `UlidGenerator.withPrefix(prefix)`; `checksum(String)` → `ChecksumUtils.sha256Hex(value)`.
- `AndesIdGeneratorAutoConfiguration`: `@AutoConfiguration` con `@Bean @ConditionalOnMissingBean IdGeneratorService`.

---

## 12. Enfoque API-first (OpenAPI → código)

Los YAML de `contracts/` son la **fuente de verdad**; el código de modelos e interfaces se genera en cada build (`target/generated-sources/openapi`), nunca se copia a mano.

### 12.1 Contratos
- Versión `openapi: 3.0.3`.
- Todos los endpoints propios responden con el envelope: `XxxEnvelope { success:boolean, data:<Dto>, error:ApiError(nullable), metadata:ApiMetadata }`, más `ErrorEnvelope { success=false, data=null, error, metadata }`. Para listas: `XxxPageEnvelope` con `data: XxxPage { content:[Dto], pagination:Pagination }`.
- Esquemas comunes (idénticos a las clases de `andes-api-common`): `ApiError{code,message,httpStatus,traceId,timestamp,details[]}`, `ApiErrorDetail{field,code,message,rejectedValue}`, `ApiMetadata{traceId,correlationId,requestId,apiVersion,timestamp}`, `Pagination{page,size,totalElements,totalPages,first,last}`.
- Ejemplo `openapi-server.yaml`: `GET/POST /api/v1/customers` (`listCustomers`, `createCustomer`), `GET/DELETE /api/v1/customers/{id}` (`getCustomerById`, `deleteCustomer`), seguridad `bearerAuth` (http/bearer/JWT). `CustomerRequest` exige `fullName` (minLength 3) y `email`.
- Contratos de terceros: `openapi-client-a.yaml` (`GET /v2/orders/{orderId}`, `POST /v2/orders`; esquemas `Order`, `OrderItem`, `CreateOrderRequest`, `PartnerError`), `openapi-client-b.yaml` (inventario); `openapi-integration.yaml` (checkout propio, usado por `poc-integration`).

### 12.2 Plugin `openapi-generator-maven-plugin` — modo servidor (delegate)
```xml
<configuration>
    <inputSpec>${project.basedir}/../../contracts/openapi-server.yaml</inputSpec>
    <generatorName>spring</generatorName>
    <output>${project.build.directory}/generated-sources/openapi</output>
    <apiPackage>pe.andes.poc.server.generated.api</apiPackage>
    <modelPackage>pe.andes.poc.server.generated.model</modelPackage>
    <invokerPackage>pe.andes.poc.server.generated</invokerPackage>
    <generateApiTests>false</generateApiTests><generateModelTests>false</generateModelTests>
    <generateApiDocumentation>false</generateApiDocumentation><generateModelDocumentation>false</generateModelDocumentation>
    <modelsToGenerate>Customer,CustomerRequest,CustomerEnvelope,CustomerPageEnvelope,CustomerPage,ErrorEnvelope</modelsToGenerate>
    <supportingFilesToGenerate>ApiUtil.java,RFC3339DateFormat.java,openapi.yaml</supportingFilesToGenerate>
    <importMappings>   <!-- reutiliza las clases de andes-api-common, no regenerarlas -->
        <importMapping>ApiError=pe.andes.api.common.model.ApiError</importMapping>
        <importMapping>ApiErrorDetail=pe.andes.api.common.model.ApiErrorDetail</importMapping>
        <importMapping>ApiMetadata=pe.andes.api.common.model.ApiMetadata</importMapping>
        <importMapping>Pagination=pe.andes.api.common.model.Pagination</importMapping>
    </importMappings>
    <configOptions>
        <interfaceOnly>false</interfaceOnly><delegatePattern>true</delegatePattern>
        <useTags>true</useTags><useSpringBoot3>true</useSpringBoot3>
        <openApiNullable>false</openApiNullable><documentationProvider>none</documentationProvider>
    </configOptions>
</configuration>
```
Genera `XxxApi`, `XxxApiDelegate` y `XxxApiController` (`@RestController` que delega). El consumidor solo escribe un `@Service` que implementa `XxxApiDelegate`; el paquete base de la app debe cubrir el paquete generado para que el component scan lo detecte. Dependencias del módulo: `swagger-annotations-jakarta` (`${swagger-annotations.version}`), `spring-boot-starter-web`, `spring-boot-starter-validation`, el starter server y `andes-api-common`. Añadir `spring-boot-maven-plugin` (`repackage`, `finalName`).

### 12.3 Plugin — modo cliente (solo modelos)
Misma estructura pero con `generateApis=false`, `generateSupportingFiles=false`, `modelPackage=...generated.<x>.model`, `useBeanValidation=false`, `useSpringBoot3=true`, `openApiNullable=false`. Los DTOs se pasan como `Class<T>` a `AndesApiClient`. Si un módulo genera servidor **y** modelos de cliente, define dos `<execution>` con `<id>` distintos y el mismo `<output>`.

### 12.4 Patrón del delegate (cómo armar la respuesta)
```java
@Service
public class CustomerApiDelegateImpl implements CustomersApiDelegate {
    public ResponseEntity<CustomerEnvelope> getCustomerById(Long id) {
        return ResponseEntity.ok(new CustomerEnvelope().success(true).data(service.getById(id)).metadata(currentMetadata()));
    }
    private ApiMetadata currentMetadata() {
        String cid = MDC.get(AndesApiConstants.MDC_CORRELATION_ID);
        return ApiMetadata.builder().traceId(cid).correlationId(cid)
                .requestId(MDC.get(AndesApiConstants.MDC_REQUEST_ID)).build();
    }
}
```
- Defaults de paginación: `page=0`, `size=20`; usar `PageResponse`/`Pagination` y mapear a `XxxPage`.
- `POST` ⇒ `201 CREATED`; `DELETE` ⇒ `204 No Content`.
- Errores de negocio: lanzar `AndesNotFoundException`, `AndesConflictException`, etc.; `GlobalExceptionHandler` los convierte al `ErrorEnvelope`.
- Con delegate + envelope generado, configurar `andes.api.server.response.wrap-enabled=false`.

---

## 13. PoCs (`examples/`)

| PoC | Puerto | Qué demuestra | Config clave |
|---|---|---|---|
| `poc-server` | 8080 | API-first servidor con delegate sobre `openapi-server.yaml`; CRUD de customers en memoria (`CustomerService`) | `wrap-enabled: false`, `openapi.*` completo (contact, license, servers, tags, `bearerAuth`) |
| `poc-client` | 8082 | Consumo de `openapi-client-a/b.yaml` con modelos generados + `ExternalOrdersSimulatorController` (simula API externa) + `OrdersDemoController` | `andes.api.client.clients.orders.base-url=http://localhost:8082` |
| `poc-integration` | 8083 | Expone `openapi-integration.yaml` (delegate `CheckoutApiDelegateImpl`) y consume Orders API vía `OrdersRemoteService` (`registry.get("orders")` → `get("/v2/orders/{id}")`, `post("/v2/orders")`); `CheckoutService` mapea entre ambos contratos | ambos starters; `wrap-enabled: false` |
| `poc-classic-libs` | — | `main()` de consola sin Spring usando text-utils + id-generator | solo esas 2 deps |
| `poc-wrapper-demo` | 8084 | `IdGeneratorDemoController` inyecta `IdGeneratorService`: `GET /api/v1/ids[?prefix=ORD]`, `GET /api/v1/ids/{valor}/checksum` | starter id-generator |

Cada PoC tiene `application.yml` (`server.port`, `spring.application.name`, logging `pe.andes: DEBUG`) y tests de integración (`*IT`) con `spring-boot-starter-test`.

---

## 14. Calidad y pruebas (qué cubrir al reimplementar)

- `common`: `ApiResponseTest`, `PageResponseTest`, `ErrorUtilsTest`, `HeaderUtilsTest`, `JsonUtilsTest`, `ValidationUtilsTest` (JUnit puro).
- `server`: `GlobalExceptionHandlerTest` (cada tipo de excepción → status/code/details), `AndesOpenApiFactoryTest`; starter: test de autoconfiguración con context runner.
- `client`: `AndesClientErrorMapperTest` (todos los status, con y sin body `ApiError`), `AndesRestClientFactoryTest` (WireMock: headers, timeouts, errores); starter: autoconfiguración + registry.
- `text-utils`/`id-generator`: tests de bordes (null, blank, límites, longitud 26 del ULID, orden temporal, SHA-256 conocido).
- PoCs: ITs HTTP end-to-end (`CustomerControllerIT`, `OrdersDemoControllerIT`, `CheckoutControllerIT`) y `InventoryModelCodegenTest` (verifica que los modelos se generan).

Comandos: `mvn clean install` (build + tests), `mvn -q -DskipTests generate-sources compile` (codegen), `mvn -pl examples/<poc> spring-boot:run`.

---

## 15. Publicación y consumo desde otro proyecto

1. **Maven local**: `./scripts/publish-local.sh [--with-tests]` (`mvn [-DskipTests] clean install`) ⇒ artefactos en `~/.m2/repository/pe/andes/api/`.
2. **Nexus/Artifactory**: `scripts/publish-nexus.sh|ps1`; sobreescribir `-Dnexus.releases.url` / `-Dnexus.snapshots.url`; credenciales en `~/.m2/settings.xml` con ids `nexus-releases` y `nexus-snapshots`. Para releases usar el perfil `-Prelease` (fuentes + javadoc).
3. **JitPack**: `jitpack.yml` en la raíz del repo con `jdk: [openjdk21]`; `git tag vX.Y.Z && git push origin vX.Y.Z`; coordenadas JitPack del repo en lugar de `pe.andes.api`.
4. **Consumo**:
```xml
<dependencyManagement><dependencies>
    <dependency><groupId>pe.andes.api</groupId><artifactId>andes-api-bom</artifactId>
        <version>1.0.0-SNAPSHOT</version><type>pom</type><scope>import</scope></dependency>
</dependencies></dependencyManagement>
<dependencies>
<dependency><groupId>pe.andes.api</groupId><artifactId>andes-api-server-spring-boot-starter</artifactId></dependency>
<dependency><groupId>pe.andes.api</groupId><artifactId>andes-api-client-spring-boot-starter</artifactId></dependency>
<dependency><groupId>pe.andes.api</groupId><artifactId>andes-id-generator-spring-boot-starter</artifactId></dependency>
<dependency><groupId>pe.andes.api</groupId><artifactId>andes-text-utils</artifactId></dependency>
</dependencies>
```
El proyecto consumidor debe aportar `spring-boot-starter-web` (+ `spring-boot-starter-validation`), ya que los starters los declaran como `provided`.

---

## 16. Decisiones de diseño a respetar

- `RestClient` en lugar de `WebClient`: API síncrona, sin Reactor, compatible con virtual threads de Java 21.
- `andes-api-common` sin Spring para poder reutilizarse en cualquier runtime.
- Todo bean autoconfigurado es sustituible (`@ConditionalOnMissingBean`) y desactivable por propiedad.
- Los errores salen siempre en el envelope `ApiResponse` con `ApiError` (code estable + httpStatus + traceId + details); el cliente reconstruye la misma jerarquía de excepciones desde ese JSON.
- El `traceId` coincide con el correlation id (MDC `correlationId`); se propaga entre servicios vía `X-Correlation-Id`.
- Las librerías clásicas mantienen API puramente estática; la integración con Spring se hace con un wrapper separado, sin tocar la librería original.

---

## 17. Apéndice: publicación/consumo vía Nexus de los OMS Starters (Galaxy)

Documenta la implementación real (verificada end-to-end) de publicación y consumo en Nexus de
los **4 Galaxy Starters + su BOM** (`guia/05.-Starters/v2.0.0/`, ecosistema *hermano* de
`andes-api-toolkit` dentro del mismo Nexus, pero con otro `groupId`), tal como lo consume
`proyecto-final/bank-creditcard-service`.

### 17.1 Artefactos y coordenadas

- **groupId**: `pe.edu.galaxy.training.java` (starters) / `pe.edu.galaxy.training.java.bom` (BOM) · **versión**: `3.0.0` (migración a Spring Boot 4.1.1 / Java 21 + Azure Service Bus/Key Vault).
- Módulos: `oms-starter-bom-core` (Maven, `packaging=pom`), `oms-starter-logs-core`, `oms-starter-audit-core`, `oms-starter-security-core`, `oms-starter-observability-core` (los 4, Gradle).
- Mismo Nexus que `andes-api-toolkit` (`http://localhost:8089`), repos `maven-releases` / `maven-snapshots`, credenciales compartidas (`admin`/`admin1234` en entorno de desarrollo).

### 17.2 Publicación — starters Gradle (`build.gradle` de cada starter)

Cada starter Gradle agrega un bloque `publishing.repositories.maven` apuntando a Nexus, con
selección automática releases/snapshots según el sufijo de versión, y credenciales vía
`findProperty` (nunca hardcodeadas):

```groovy
publishing {
    publications {
        mavenJava(MavenPublication) { from components.java }
    }
    repositories {
        maven {
            name = 'nexus'
            def isSnapshot = project.version.toString().endsWith('-SNAPSHOT')
            url = isSnapshot
                    ? 'http://localhost:8089/repository/maven-snapshots/'
                    : 'http://localhost:8089/repository/maven-releases/'
            allowInsecureProtocol = true // solo porque es http
            credentials {
                username = findProperty('nexusUser')
                password = findProperty('nexusPassword')
            }
        }
        // mavenLocal() opcional, se puede mantener en paralelo para desarrollo offline
    }
}
```

Comando: `./gradlew publish` (no `publishToMavenLocal`) ejecuta `publish<Pub>PublicationToNexusRepository`
por cada starter.

### 17.3 Publicación — BOM (`oms-starter-bom-core/pom.xml`, Maven)

El BOM usa `<distributionManagement>` estándar de Maven; las credenciales **no** van en el POM,
sino en `~/.m2/settings.xml` con los mismos ids:

```xml
<distributionManagement>
    <repository>
        <id>nexus-releases</id>
        <url>http://localhost:8089/repository/maven-releases/</url>
    </repository>
    <snapshotRepository>
        <id>nexus-snapshots</id>
        <url>http://localhost:8089/repository/maven-snapshots/</url>
    </snapshotRepository>
</distributionManagement>
```
```xml
<!-- ~/.m2/settings.xml -->
<servers>
    <server><id>nexus-releases</id><username>...</username><password>...</password></server>
    <server><id>nexus-snapshots</id><username>...</username><password>...</password></server>
</servers>
```

Comando: `mvn deploy`. **Importante**: si la versión no es `-SNAPSHOT`, el deploy va a
`maven-releases`; algunos Nexus tienen "Disable redeploy" activo en ese repo, por lo que
redeployar la misma versión release puede fallar — bump de versión o repo configurado para
permitir redeploy en entornos de desarrollo.

### 17.4 Scripts de publicación

- `proyecto-final/scripts/publish-starters.ps1`: `gradlew publishToMavenLocal` + `mvn install`
  (solo Maven Local, para desarrollo offline).
- `proyecto-final/scripts/publish-starters-to-nexus.ps1` / `.sh`: equivalentes usando
  `gradlew publish` + `mvn deploy` (Nexus), mismo orden de starters, mismo manejo de errores
  (`$LASTEXITCODE` / `set -euo pipefail`).

### 17.5 Consumo desde un proyecto Gradle (Groovy DSL, caso real de `proyecto-final`)

```groovy
repositories {
    maven {
        url 'http://localhost:8089/repository/maven-releases/'
        allowInsecureProtocol = true
        credentials {
            username findProperty('nexusUser')
            password findProperty('nexusPassword')
        }
    }
    mavenCentral()
}

dependencies {
    implementation platform('pe.edu.galaxy.training.java.bom:oms-starter-bom-core:3.0.0')

    implementation 'pe.edu.galaxy.training.java:oms-starter-logs-core'
    implementation 'pe.edu.galaxy.training.java:oms-starter-audit-core'
    implementation 'pe.edu.galaxy.training.java:oms-starter-security-core'
    implementation 'pe.edu.galaxy.training.java:oms-starter-observability-core'
}
```

Notas de integración verificadas (ver `proyecto-final/docs/ISSUES-CONOCIDOS.md` para el detalle
completo de cada bug real encontrado y corregido durante la migración):

- `io.spring.dependency-management` **no** hornea versiones resueltas en el POM/Gradle Module
  Metadata publicado: cada consumidor debe importar sus propios BOMs transitivos (en este caso,
  `com.azure.spring:spring-cloud-azure-dependencies:7.4.0` para resolver los artefactos Azure sin
  versión usados por `oms-starter-security-core`/`oms-starter-audit-core`).
- Los beans que conectan contra Azure (`ServiceBusSenderClient`, `KeyClient`/`CryptographyClient`)
  validan la conexión **de forma eager** al construirse (a diferencia de `KafkaTemplate`/
  `VaultTemplate`, que son lazy) — ambos starters gatean esos beans con
  `@ConditionalOnProperty`/`@ConditionalOnBean` para no romper el arranque cuando la función está
  deshabilitada (tests, entornos sin Azure real).
- Verificación end-to-end: `gradle clean build` en `proyecto-final` → 11/11 tests, `BUILD SUCCESSFUL`,
  con los starters y el BOM resueltos 100% desde Nexus (sin `mavenLocal()`).
