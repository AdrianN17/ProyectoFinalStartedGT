# Prueba rápida con Vault

## 1. Levantar Vault

```bash
docker run --cap-add=IPC_LOCK \
  -e VAULT_DEV_ROOT_TOKEN_ID=root \
  -e VAULT_DEV_LISTEN_ADDRESS=0.0.0.0:8200 \
  -p 8200:8200 \
  hashicorp/vault
```

## 2. Configurar Transit

```bash
docker exec -it <container_id> sh
vault login root
vault secrets enable transit
vault write -f transit/keys/oms-key
```

## 3. Publicar starter

```bash
./gradlew clean publishToMavenLocal
```

## 4. Configurar microservicio

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
    audit:
      enabled: true
```

## 5. Resultado esperado

En BD:

```text
vault:vault:v1:xxxxxx
```

En API:

```json
{
  "email": "ju***@gmail.com",
  "phone": "*****8777",
  "documentNumber": "****1234"
}
```
