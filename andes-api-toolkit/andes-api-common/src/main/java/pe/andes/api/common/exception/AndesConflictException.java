package pe.andes.api.common.exception;

import pe.andes.api.common.model.ApiErrorDetail;

import java.io.Serial;
import java.util.List;

/**
 * Thrown when a request conflicts with the current state of a resource. Maps to HTTP 409.
 */
public class AndesConflictException extends AndesApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ERROR_CODE = "CONFLICT";
    public static final int HTTP_STATUS = 409;

    public AndesConflictException(String message) {
        this(message, null, null, null);
    }

    public AndesConflictException(String message, Throwable cause) {
        this(message, null, null, cause);
    }

    public AndesConflictException(String message, List<ApiErrorDetail> details, String traceId, Throwable cause) {
        super(ERROR_CODE, HTTP_STATUS, message, details, traceId, cause);
    }
}
