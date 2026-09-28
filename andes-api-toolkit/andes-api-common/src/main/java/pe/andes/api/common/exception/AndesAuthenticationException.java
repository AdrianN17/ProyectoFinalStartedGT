package pe.andes.api.common.exception;

import pe.andes.api.common.model.ApiErrorDetail;

import java.io.Serial;
import java.util.List;

/**
 * Thrown when authentication credentials are missing or invalid. Maps to HTTP 401.
 */
public class AndesAuthenticationException extends AndesApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ERROR_CODE = "AUTHENTICATION_ERROR";
    public static final int HTTP_STATUS = 401;

    public AndesAuthenticationException(String message) {
        this(message, null, null, null);
    }

    public AndesAuthenticationException(String message, Throwable cause) {
        this(message, null, null, cause);
    }

    public AndesAuthenticationException(String message, List<ApiErrorDetail> details, String traceId, Throwable cause) {
        super(ERROR_CODE, HTTP_STATUS, message, details, traceId, cause);
    }
}
