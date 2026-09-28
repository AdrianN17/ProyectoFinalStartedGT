package pe.andes.api.common.util;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Pure validation helper methods with no external dependencies.
 */
public final class ValidationUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private ValidationUtils() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static String requireNonBlank(String value, String message) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    public static boolean isValidEmail(String value) {
        return isNotBlank(value) && EMAIL_PATTERN.matcher(value).matches();
    }
}
