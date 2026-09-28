package pe.andes.api.common.exception;

import pe.andes.api.common.model.ApiErrorDetail;

import java.io.Serial;
import java.util.Collections;
import java.util.List;

/**
 * Root of the Andes API exception hierarchy. Every subtype carries an error
 * code, a default HTTP status (mapped without any Spring dependency) and an
 * optional list of {@link ApiErrorDetail}.
 *
 * <pre>
 * AndesApiException
 * |- AndesValidationException
 * |- AndesAuthenticationException
 * |- AndesAuthorizationException
 * |- AndesNotFoundException
 * |- AndesConflictException
 * |- AndesBadRequestException
 * `- AndesRemoteServiceException
 * </pre>
 */
public abstract class AndesApiException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String errorCode;
    private final int httpStatus;
    private final List<ApiErrorDetail> details;
    private final String traceId;

    protected AndesApiException(String errorCode, int httpStatus, String message,
                                 List<ApiErrorDetail> details, String traceId, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = details != null ? List.copyOf(details) : Collections.emptyList();
        this.traceId = traceId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public List<ApiErrorDetail> getDetails() {
        return details;
    }

    public String getTraceId() {
        return traceId;
    }
}
