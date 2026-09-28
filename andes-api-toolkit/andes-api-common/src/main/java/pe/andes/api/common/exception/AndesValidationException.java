package pe.andes.api.common.exception;

import pe.andes.api.common.model.ApiErrorDetail;

import java.io.Serial;
import java.util.List;

/**
 * Thrown when a request fails Bean Validation or business validation rules. Maps to HTTP 422.
 */
public class AndesValidationException extends AndesApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ERROR_CODE = "VALIDATION_ERROR";
    public static final int HTTP_STATUS = 422;

    public AndesValidationException(String message) {
        this(message, null, null, null);
    }

    public AndesValidationException(String message, List<ApiErrorDetail> details) {
        this(message, details, null, null);
    }

    public AndesValidationException(String message, List<ApiErrorDetail> details, String traceId, Throwable cause) {
        super(ERROR_CODE, HTTP_STATUS, message, details, traceId, cause);
    }
}
