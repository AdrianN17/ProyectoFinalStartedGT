<#
.SYNOPSIS
    Publica en Maven Local (~/.m2/repository) el BOM y los cuatro Galaxy Starters (v2.0.0)
    ubicados en guia/05.-Starters/v2.0.0, y el BOM + artefactos de andes-api-toolkit, para que
    puedan ser resueltos por bank-creditcard-service.

.DESCRIPTION
    Automatiza el Pattern 09 (Publicacion y Versionamiento) de la rubrica: en vez de ejecutar
    manualmente "gradlew publishToMavenLocal" / "mvn clean install" en cada starter, este
    script recorre todos ellos en el orden correcto (starters Galaxy, BOM Galaxy, andes-api-toolkit).
    No modifica ningun archivo dentro de guia/ ni de andes-api-toolkit/, solo ejecuta sus builds.

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

Write-Host "`n==> Publicando andes-api-toolkit (Maven) en Maven Local" -ForegroundColor Cyan
$andesPath = Join-Path $scriptDir "..\..\andes-api-toolkit" | Resolve-Path
Push-Location $andesPath
try {
    & mvn clean install -q -DskipTests
    if ($LASTEXITCODE -ne 0) {
        throw "Fallo publicando andes-api-toolkit (exit code $LASTEXITCODE)"
    }
}
finally {
    Pop-Location
}

Write-Host "`nStarters y BOMs (Galaxy + Andes) publicados correctamente en Maven Local." -ForegroundColor Green
