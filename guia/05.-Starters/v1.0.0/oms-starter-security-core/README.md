# oms-starter-sensitive-security

Starter para:
- Cifrado con Vault Transit usando `@Encrypt`
- Enmascaramiento usando `@Mask`
- Clasificación de datos sensibles usando `@Sensitive`

## Publicar local

```bash
./gradlew clean publishToMavenLocal
```

## Consumir en un microservicio

```gradle
implementation 'pe.edu.galaxy.training.java:oms-starter-sensitive-security:1.0.0'
```

## Configuración

```yaml
oms:
  sensitive:
    enabled: true
    encrypt:
      enabled: true
      provider: VAULT
      vault:
        uri: http://localhost:8200
        token: root
        transit-key: oms-key
    mask:
      enabled: true
      enabled-by-default: true
    audit:
      enabled: true
```

## Uso

```java
@Encrypt
@Mask(type = MaskType.EMAIL)
@Sensitive(level = SensitivityLevel.HIGH)
private String email;
```
