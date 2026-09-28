package pe.andes.api.common.exception;

import pe.andes.api.common.model.ApiErrorDetail;

import java.io.Serial;
import java.util.List;

/**
 * Thrown when a request is malformed or violates a precondition. Maps to HTTP 400.
 */
public class AndesBadRequestException extends AndesApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ERROR_CODE = "BAD_REQUEST";
    public static final int HTTP_STATUS = 400;

    public AndesBadRequestException(String message) {
        this(message, null, null, null);
    }

    public AndesBadRequestException(String message, Throwable cause) {
        this(message, null, null, cause);
    }

    public AndesBadRequestException(String message, List<ApiErrorDetail> details, String traceId, Throwable cause) {
        super(ERROR_CODE, HTTP_STATUS, message, details, traceId, cause);
    }
}
