#!/usr/bin/env bash
#
# Publica el BOM y los cuatro Galaxy Starters (v3.0.0, guia/05.-Starters/v2.0.0) en el
# repositorio corporativo Nexus (maven-releases / maven-snapshots), en vez de (o ademas de)
# Maven Local.
#
# Complementa a publish-starters.ps1 (que publica solo en Maven Local): este script ejecuta
# "./gradlew publish" en cada starter Gradle (logs, audit, security, observability) -que sube al
# repositorio Nexus configurado en su bloque `publishing.repositories.maven` de build.gradle- y
# "mvn deploy" en el BOM Maven (oms-starter-bom-core), usando el <distributionManagement>
# agregado a su pom.xml.
#
# Requiere credenciales de Nexus configuradas previamente (NUNCA se piden ni se escriben aqui):
#   - Para los starters Gradle: `nexusUser` / `nexusPassword` en ~/.gradle/gradle.properties,
#     o variables de entorno ORG_GRADLE_PROJECT_nexusUser / ORG_GRADLE_PROJECT_nexusPassword.
#   - Para el BOM Maven: un <server> con id "nexus-releases" y "nexus-snapshots" en
#     ~/.m2/settings.xml (usuario/clave de Nexus).
#
# La URL de Nexus (http://localhost:8089) se asume un Nexus local de desarrollo; si tu Nexus
# corporativo tiene otra URL, ajusta el bloque `publishing.repositories` en cada build.gradle y
# el <distributionManagement> del pom.xml del BOM.
#
# Uso:
#   ./scripts/publish-starters-to-nexus.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
STARTERS_ROOT="$(cd "$SCRIPT_DIR/../../guia/05.-Starters/v2.0.0" && pwd)"

GRADLE_STARTERS=(
    "oms-starter-logs-core"
    "oms-starter-audit-core"
    "oms-starter-security-core"
    "oms-starter-observability-core"
)

echo "==> Publicando starters Gradle en Nexus (maven-releases / maven-snapshots)"
for starter in "${GRADLE_STARTERS[@]}"; do
    path="$STARTERS_ROOT/$starter"
    echo ""
    echo "--- $starter ---"
    (
        cd "$path"
        ./gradlew publish --console=plain
    )
done

echo ""
echo "==> Publicando oms-starter-bom-core (Maven) en Nexus"
bom_path="$STARTERS_ROOT/oms-starter-bom-core"
(
    cd "$bom_path"
    mvn deploy -q
)

echo ""
echo "Starters y BOM Galaxy publicados correctamente en Nexus."
