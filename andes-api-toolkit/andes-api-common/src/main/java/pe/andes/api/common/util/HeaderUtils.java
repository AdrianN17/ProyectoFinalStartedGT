package pe.andes.api.common.util;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Pure utility methods to validate and generate correlation/request identifiers.
 */
public final class HeaderUtils {

    private static final Pattern ID_PATTERN = Pattern.compile("^[a-zA-Z0-9\\-]{8,64}$");

    private HeaderUtils() {
    }

    public static boolean isValidCorrelationId(String value) {
        return value != null && ID_PATTERN.matcher(value).matches();
    }

    public static boolean isValidRequestId(String value) {
        return value != null && ID_PATTERN.matcher(value).matches();
    }

    public static String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    public static String generateRequestId() {
        return UUID.randomUUID().toString();
    }

    public static String defaultIfInvalid(String value, boolean valid) {
        return valid ? value : generateCorrelationId();
    }
}
