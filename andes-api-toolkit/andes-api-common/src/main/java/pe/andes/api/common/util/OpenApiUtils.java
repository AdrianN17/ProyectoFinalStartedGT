package pe.andes.api.common.util;

import java.util.regex.Pattern;

/**
 * Pure helpers for OpenAPI-related conventions shared by Server and Client
 * (operationId format, semantic API version format, etc).
 */
public final class OpenApiUtils {

    private static final Pattern OPERATION_ID_PATTERN = Pattern.compile("^[a-zA-Z][a-zA-Z0-9]*$");
    private static final Pattern SEMVER_PATTERN =
            Pattern.compile("^\\d+\\.\\d+\\.\\d+(-[0-9A-Za-z.-]+)?(\\+[0-9A-Za-z.-]+)?$");

    private OpenApiUtils() {
    }

    public static boolean isValidOperationId(String operationId) {
        return operationId != null && OPERATION_ID_PATTERN.matcher(operationId).matches();
    }

    public static boolean isValidSemanticVersion(String version) {
        return version != null && SEMVER_PATTERN.matcher(version).matches();
    }

    public static String normalizeApiVersion(String rawVersion, String defaultVersion) {
        return ValidationUtils.isBlank(rawVersion) ? defaultVersion : rawVersion.trim();
    }
}
