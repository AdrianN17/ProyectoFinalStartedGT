<#
.SYNOPSIS
    Publica en Maven Local (~/.m2/repository) el BOM y los cuatro Galaxy Starters (v3.0.0)
    ubicados en guia/05.-Starters/v2.0.0, para que puedan ser resueltos por
    bank-creditcard-service.

.DESCRIPTION
    Automatiza el Pattern 09 (Publicacion y Versionamiento) de la rubrica: en vez de ejecutar
    manualmente "gradlew publishToMavenLocal" / "mvn clean install" en cada starter, este
    script recorre todos ellos en el orden correcto (starters Galaxy, BOM Galaxy).
    No modifica ningun archivo dentro de guia/, solo ejecuta sus builds.

    El toolkit "andes-api-toolkit" (pe.andes.api:*) YA NO se construye ni publica localmente:
    se resuelve directamente desde el repositorio corporativo Nexus
    (http://localhost:8089/repository/maven-releases|maven-snapshots), configurado en
    build.gradle. Para que la resolucion funcione, define las credenciales de Nexus en
    ~/.gradle/gradle.properties (nexusUser/nexusPassword) o como variables de entorno
    ORG_GRADLE_PROJECT_nexusUser / ORG_GRADLE_PROJECT_nexusPassword. Ver README.md.

.EXAMPLE
    ./scripts/publish-starters.ps1
#>

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$startersRoot = Join-Path $scriptDir "..\..\guia\05.-Starters\v2.0.0" | Resolve-Path

$gradleStarters = @(
    "oms-starter-logs-core",
    "oms-starter-audit-core",
    "oms-starter-security-core",
    "oms-starter-observability-core"
)

Write-Host "==> Publicando starters Gradle en Maven Local" -ForegroundColor Cyan
foreach ($starter in $gradleStarters) {
    $path = Join-Path $startersRoot $starter
    Write-Host "`n--- $starter ---" -ForegroundColor Yellow
    Push-Location $path
    try {
        & "$path\gradlew.bat" publishToMavenLocal --console=plain
        if ($LASTEXITCODE -ne 0) {
            throw "Fallo publicando $starter (exit code $LASTEXITCODE)"
        }
    }
    finally {
        Pop-Location
    }
}

Write-Host "`n==> Publicando oms-starter-bom-core (Maven) en Maven Local" -ForegroundColor Cyan
$bomPath = Join-Path $startersRoot "oms-starter-bom-core"
Push-Location $bomPath
try {
    & mvn clean install -q
    if ($LASTEXITCODE -ne 0) {
        throw "Fallo publicando oms-starter-bom-core (exit code $LASTEXITCODE)"
    }
}
finally {
    Pop-Location
}

Write-Host "`nStarters y BOM Galaxy publicados correctamente en Maven Local." -ForegroundColor Green
Write-Host "andes-api-toolkit (pe.andes.api:*) se resuelve desde Nexus; no requiere build local." -ForegroundColor Green
