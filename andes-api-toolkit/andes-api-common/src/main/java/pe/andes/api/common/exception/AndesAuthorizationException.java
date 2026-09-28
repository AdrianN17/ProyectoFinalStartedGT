package pe.andes.api.common.exception;

import pe.andes.api.common.model.ApiErrorDetail;

import java.io.Serial;
import java.util.List;

/**
 * Thrown when an authenticated caller lacks permission for the requested operation. Maps to HTTP 403.
 */
public class AndesAuthorizationException extends AndesApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ERROR_CODE = "AUTHORIZATION_ERROR";
    public static final int HTTP_STATUS = 403;

    public AndesAuthorizationException(String message) {
        this(message, null, null, null);
    }

    public AndesAuthorizationException(String message, Throwable cause) {
        this(message, null, null, cause);
    }

    public AndesAuthorizationException(String message, List<ApiErrorDetail> details, String traceId, Throwable cause) {
        super(ERROR_CODE, HTTP_STATUS, message, details, traceId, cause);
    }
}
