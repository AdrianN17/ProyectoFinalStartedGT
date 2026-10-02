# Issues conocidos (no relacionados con la migración a Spring Boot 4.1.1 / Java 21 / Azure)

Este documento registra 3 fallas de test detectadas durante la verificación posterior a la
migración de `proyecto-final` a Spring Boot 4.1.1, Java 21 e integración con Azure
(Service Bus + Key Vault) y Nexus (`andes-api-toolkit`). Las 3 son **preexistentes**: no las
introdujo la migración, y se manifiestan porque la migración, al arreglar el arranque del
contexto de Spring (ver `CreditCardServiceApplicationTests`), permitió que estos tests
finalmente se ejecutaran hasta el final en vez de fallar antes por errores de bootstrap.

Ejecutar: `cd proyecto-final && gradle clean build` → `11 tests completed, 3 failed`.

## ✅ Actualización: las 3 fallas fueron resueltas sin tocar `andes-api-toolkit`

1. **Key Vault simulado en tests**: `proyecto-final/src/test/java/.../config/KeyVaultTestConfig.java`
   registra un `CryptographyClient` mockeado con Mockito (`encrypt`/`decrypt` reversibles via
   Base64 local, sin red). El gating `@ConditionalOnMissingBean`/`@ConditionalOnBean` en
   `SensitiveSecurityAutoConfiguration` hace que el starter arme su `KeyVaultEncryptServiceImpl`
   de producción alrededor del mock, validando el flujo real de cifrado sin Azure/`az login`.
2. **Wrapping de `andes-api-toolkit` resuelto del lado del cliente**: `FraudCheckClient` ahora
   deserializa a `ApiResponse<FraudCheckResponse>` (clase pública de `andes-api-common`) y
   extrae `.getData()` en vez de mapear directo a `FraudCheckResponse`. No requirió cambios en
   la librería.

Resultado: `gradle clean build` → **11/11 tests pasan, 0 fallos**.

El análisis de causa raíz original se conserva abajo para referencia histórica.

---

## 1. `issueCardMasksCardNumberAndCvvInResponse()` — requiere Key Vault real

**Síntoma:**
```
java.lang.AssertionError: JSON path "$.data.cardNumber"
Expected: not "4111111111111111"
     but: was "4111111111111111"
```

**Causa raíz:** el test espera que `cardNumber`/`cvv` salgan transformados en la respuesta.
Esa transformación la produce `SensitiveRepositoryAspect` (`oms-starter-security-core`), que
**cifra** el campo anotado con `@Encrypt` justo antes de `repository.save(...)` (ver
`SensitiveFieldProcessor.process()`), y como el aspecto no vuelve a descifrar después del
`save()` (solo lo hace tras `find*`), la entidad que llega al mapper/response conserva el
valor **cifrado** (prefijo `akv:...`). Es decir: lo que el test llama "masking" en realidad
depende de que el cifrado contra Azure Key Vault esté **habilitado y funcionando**.

En el perfil de test (`src/test/resources/application-test.yml`) se deshabilita el cifrado
(`oms.sensitive.encrypt.enabled: false`) porque no hay infraestructura de Key Vault disponible
en CI/local, así que el campo nunca se transforma y el test falla. Esto ya ocurría de forma
idéntica con la implementación anterior basada en HashiCorp Vault (mismo código en
`SensitiveFieldProcessor`, sin cambios por la migración).

**Posibles soluciones:**
1. **Levantar un Azure Key Vault real (o emulado) para tests** y habilitar
   `oms.sensitive.encrypt.enabled=true` en el perfil de test, autenticando con
   `az login` (requiere una suscripción/Key Vault de pruebas accesible desde CI).
2. **Mockear `EncryptService`/`CryptographyClient` en el test** (p. ej. con
   `@MockitoBean` o un `@TestConfiguration` que registre un `EncryptService` fake que SÍ
   transforme el valor, p. ej. invirtiendo el string), para probar el flujo de
   cifrado/mapeo sin depender de infraestructura real, sin tocar el starter.
3. **Separar el concepto de "mask para mostrar" del de "cifrado para persistir"**: agregar un
   verdadero enmascarado a nivel de serialización Jackson (vía `@Mask` +
   `SensitiveBeanSerializerModifier`, ya existente en el starter) aplicado también sobre
   `CreditCardResponse` (actualmente ese DTO no tiene anotaciones `@Mask`/`@Sensitive`; solo
   las tiene `CreditCardEntity`), de modo que el enmascarado visual no dependa de si el
   cifrado está habilitado.

---

## 2. `lowAmountTransactionIsApprovedThroughFraudCheckSimulator()` y `highAmountTransactionIsRejectedThroughFraudCheckSimulator()` — bug de wrapping en `andes-api-toolkit`

**Síntoma:** `500 Internal Server Error` al autorizar una transacción.

**Log relevante:**
```
BUSINESS_OPERATION_ERROR operation=TRANSACTION_AUTHORIZE ... error=Error while extracting
response for type [...FraudCheckResponse] and content type [application/json]
Caused by: tools.jackson.databind.exc.MismatchedInputException:
Cannot map `null` into type `int` (set `DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES`
to 'false' to allow)
(through reference chain: ...FraudCheckResponse["riskScore"])
```

**Causa raíz:** `andes-api-server-spring-boot-starter` registra un
`ResponseBodyAdvice` global (`AndesResponseBodyAdvice`, ver
`/mnt/extra/proyectos/ProyectoFinalLibreriaGT/andes-api-toolkit/andes-api-server/src/main/java/pe/andes/api/server/web/AndesResponseBodyAdvice.java`)
que envuelve **todas** las respuestas de **todos** los controladores del proceso en un sobre
`ApiResponse<T>` (`{"success":true,"data":{...},"metadata":{...}}`), sin forma de excluir
controladores puntuales (no existe anotación `@NoWrap`/`@RawResponse` ni condición por
property).

`FraudCheckSimulatorController` es un controlador **interno** del propio
`bank-creditcard-service` (simula un servicio externo de scoring, patrón tomado de
`andes-api-toolkit:examples/poc-client`, para que el proyecto sea autocontenido). Al estar en
el mismo proceso, su respuesta también pasa por el advice global y queda envuelta.

Sin embargo, `FraudCheckClient` (que usa `andes-api-client-spring-boot-starter`, concretamente
`AndesApiClient.post(uri, body, FraudCheckResponse.class)`, ver
`/mnt/extra/proyectos/ProyectoFinalLibreriaGT/andes-api-toolkit/andes-api-client/src/main/java/pe/andes/api/client/AndesApiClient.java`)
deserializa la respuesta **directamente** a `FraudCheckResponse`, sin desenvolver el sobre
`ApiResponse`. Como el JSON real es `{"success":true,"data":{"riskScore":90},...}` y no
`{"riskScore":90}`, Jackson no encuentra `riskScore` en el nivel raíz, lo resuelve como `null`
y falla al mapearlo al `int` primitivo del record `FraudCheckResponse`.

Este bug es **anterior** a la migración de `proyecto-final`: la configuración
`andes.api.server.response.wrap-enabled: true` ya estaba así en el `application.yml` original,
y el código de `AndesResponseBodyAdvice`/`AndesApiClient` no fue tocado por esta migración.
Antes simplemente no se detectaba porque el test ni siquiera llegaba a ejecutarse (el contexto
de Spring no arrancaba, ver items previos sobre `fraudCheck` client y gating de Service
Bus/Key Vault).

**Posibles soluciones (en `andes-api-toolkit`, repo separado en `ProyectoFinalLibreriaGT`):**
1. **Agregar un mecanismo de exclusión** al advice, p. ej. una anotación
   `@NoWrap`/`@RawResponse` a nivel de método o clase que `AndesResponseBodyAdvice.supports(...)`
   respete (`return !ApiResponse.class.isAssignableFrom(type) && !hasNoWrapAnnotation(returnType)`).
2. **Agregar soporte de "unwrap automático" en `AndesApiClient`**: que el cliente detecte el
   sobre `ApiResponse` (p. ej. deserializando primero a `ApiResponse<T>` vía
   `ParameterizedTypeReference` y extrayendo `.data()`), de forma transparente para el
   código de negocio que ya llama `client.post(uri, body, MiTipo.class)`.
3. **Solución puntual en `proyecto-final`** (sin tocar la librería): mover
   `FraudCheckSimulatorController` a un `@RestControllerAdvice`/paquete excluido del
   component-scan del advice, o hacer que `FraudCheckClient` deserialice a
   `ApiResponse<FraudCheckResponse>` (vía `ParameterizedTypeReference`) y luego llame
   `.data()`, replicando manualmente la lógica de desenvuelto mientras no exista soporte
   nativo en el starter.

---

## Resumen

| Test | Causa | ¿Culpa de la migración SB4/Azure? |
|---|---|---|
| `issueCardMasksCardNumberAndCvvInResponse` | Requiere Key Vault real habilitado | No — mismo comportamiento existía con HashiCorp Vault |
| `lowAmountTransactionIsApprovedThroughFraudCheckSimulator` | Bug de wrapping en `andes-api-toolkit` | No — config/código sin cambios por esta migración |
| `highAmountTransactionIsRejectedThroughFraudCheckSimulator` | Idem anterior | No |

La migración a Spring Boot 4.1.1 / Java 21, el reemplazo de Kafka→Azure Service Bus y
Vault→Azure Key Vault, y la integración con Nexus para `andes-api-toolkit` quedaron
**verificadas y funcionando** (compilación completa, resolución de dependencias, arranque de
contexto Spring). Estas 3 fallas de test son defectos preexistentes de diseño/infraestructura
del proyecto de ejemplo, documentados aquí para seguimiento posterior.
