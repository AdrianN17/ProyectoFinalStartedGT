# Prueba rápida con Azure Key Vault

## 1. Crear el Key Vault y la clave (Azure CLI)

```bash
az group create --name rg-oms-demo --location eastus

az keyvault create \
  --name mi-keyvault-oms \
  --resource-group rg-oms-demo \
  --location eastus

az keyvault key create \
  --vault-name mi-keyvault-oms \
  --name oms-key \
  --kty RSA \
  --size 2048 \
  --ops encrypt decrypt
```

## 2. Asignar permisos (RBAC) a la identidad que ejecutará el microservicio

```bash
az role assignment create \
  --role "Key Vault Crypto User" \
  --assignee <client-id-o-managed-identity> \
  --scope $(az keyvault show --name mi-keyvault-oms --query id -o tsv)
```

## 3. Autenticación local para pruebas

Para desarrollo local, `DefaultAzureCredential` puede autenticarse con Azure CLI:

```bash
az login
```

O bien, definiendo variables de entorno de un Service Principal:

```bash
export AZURE_CLIENT_ID=xxxx
export AZURE_TENANT_ID=xxxx
export AZURE_CLIENT_SECRET=xxxx
```

## 4. Publicar starter

```bash
./gradlew clean publishToMavenLocal
```

## 5. Configurar microservicio

```yaml
oms:
  sensitive:
    enabled: true
    encrypt:
      enabled: true
      provider: AZURE_KEY_VAULT
      azure-key-vault:
        vault-url: https://mi-keyvault-oms.vault.azure.net
        key-name: oms-key
        algorithm: RSA-OAEP-256
    mask:
      enabled: true
    audit:
      enabled: true
```

## 6. Resultado esperado

En BD:

```text
akv:xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx==
```

En API:

```json
{
  "email": "ju***@gmail.com",
  "phone": "*****8777",
  "documentNumber": "****1234"
}
```
