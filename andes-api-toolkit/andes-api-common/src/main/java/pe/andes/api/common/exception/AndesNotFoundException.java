package pe.andes.api.common.exception;

import pe.andes.api.common.model.ApiErrorDetail;

import java.io.Serial;
import java.util.List;

/**
 * Thrown when a requested resource does not exist. Maps to HTTP 404.
 */
public class AndesNotFoundException extends AndesApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ERROR_CODE = "NOT_FOUND";
    public static final int HTTP_STATUS = 404;

    public AndesNotFoundException(String message) {
        this(message, null, null, null);
    }

    public AndesNotFoundException(String message, Throwable cause) {
        this(message, null, null, cause);
    }

    public AndesNotFoundException(String message, List<ApiErrorDetail> details, String traceId, Throwable cause) {
        super(ERROR_CODE, HTTP_STATUS, message, details, traceId, cause);
    }
}
