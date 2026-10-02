# oms-starter-sensitive-security

Starter para Spring Boot 4.1.1 (Java 21) que provee:
- Cifrado con **Azure Key Vault** (operaciones criptográficas sobre una clave administrada) usando `@Encrypt`
- Enmascaramiento usando `@Mask`
- Clasificación de datos sensibles usando `@Sensitive`

## Publicar local

```bash
./gradlew clean publishToMavenLocal
```

## Consumir en un microservicio

```gradle
implementation 'pe.edu.galaxy.training.java:oms-starter-security-core:3.0.0'
```

## Configuración

```yaml
oms:
  sensitive:
    enabled: true
    encrypt:
      enabled: true
      provider: AZURE_KEY_VAULT
      azure-key-vault:
        vault-url: https://mi-keyvault.vault.azure.net
        key-name: oms-key
        algorithm: RSA-OAEP-256
    mask:
      enabled: true
      enabled-by-default: true
    audit:
      enabled: true
```

La autenticación contra Azure Key Vault se realiza mediante `DefaultAzureCredential`
(en desarrollo local: `az login` con Azure CLI; en Azure: Managed Identity, variables
de entorno, etc.), por lo que no es necesario almacenar tokens ni secretos en la
configuración del microservicio. Esta credencial se usa **exclusivamente** para Key
Vault; Azure Service Bus (ver `oms-starter-audit-core`) se autentica por separado con
connection string.

## Uso

```java
@Encrypt
@Mask(type = MaskType.EMAIL)
@Sensitive(level = SensitivityLevel.HIGH)
private String email;
```
