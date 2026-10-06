<#
.SYNOPSIS
    Publica el BOM y los cuatro Galaxy Starters (v3.0.0, guia/05.-Starters/v2.0.0) en el
    repositorio corporativo Nexus (maven-releases / maven-snapshots), en vez de (o ademas de)
    Maven Local.

.DESCRIPTION
    Complementa a publish-starters.ps1 (que publica solo en Maven Local): este script ejecuta
    "gradlew publish" en cada starter Gradle (logs, audit, security, observability) -que sube al
    repositorio Nexus configurado en su bloque `publishing.repositories.maven` de build.gradle- y
    "mvn deploy" en el BOM Maven (oms-starter-bom-core), usando el <distributionManagement>
    agregado a su pom.xml.

    Requiere credenciales de Nexus configuradas previamente (NUNCA se piden ni se escriben aqui):
      - Para los starters Gradle: `nexusUser` / `nexusPassword` en ~/.gradle/gradle.properties,
        o variables de entorno ORG_GRADLE_PROJECT_nexusUser / ORG_GRADLE_PROJECT_nexusPassword.
      - Para el BOM Maven: un <server> con id "nexus-releases" y "nexus-snapshots" en
        ~/.m2/settings.xml (usuario/clave de Nexus).

    La URL de Nexus (http://localhost:8089) se asume un Nexus local de desarrollo; si tu Nexus
    corporativo tiene otra URL, ajusta el bloque `publishing.repositories` en cada build.gradle y
    el <distributionManagement> del pom.xml del BOM.

.EXAMPLE
    ./scripts/publish-starters-to-nexus.ps1
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

Write-Host "==> Publicando starters Gradle en Nexus (maven-releases / maven-snapshots)" -ForegroundColor Cyan
foreach ($starter in $gradleStarters) {
    $path = Join-Path $startersRoot $starter
    Write-Host "`n--- $starter ---" -ForegroundColor Yellow
    Push-Location $path
    try {
        & "$path\gradlew.bat" publish --console=plain
        if ($LASTEXITCODE -ne 0) {
            throw "Fallo publicando $starter en Nexus (exit code $LASTEXITCODE)"
        }
    }
    finally {
        Pop-Location
    }
}

Write-Host "`n==> Publicando oms-starter-bom-core (Maven) en Nexus" -ForegroundColor Cyan
$bomPath = Join-Path $startersRoot "oms-starter-bom-core"
Push-Location $bomPath
try {
    & mvn deploy -q
    if ($LASTEXITCODE -ne 0) {
        throw "Fallo publicando oms-starter-bom-core en Nexus (exit code $LASTEXITCODE)"
    }
}
finally {
    Pop-Location
}

Write-Host "`nStarters y BOM Galaxy publicados correctamente en Nexus." -ForegroundColor Green
