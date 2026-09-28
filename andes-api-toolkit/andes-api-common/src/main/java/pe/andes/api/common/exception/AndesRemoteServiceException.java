package pe.andes.api.common.exception;

import pe.andes.api.common.model.ApiErrorDetail;

import java.io.Serial;
import java.util.List;

/**
 * Thrown by andes-api-client when a downstream/remote service fails (5xx or transport error).
 * Preserves the remote endpoint and status to aid diagnostics. Maps to HTTP 502 by default.
 */
public class AndesRemoteServiceException extends AndesApiException {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ERROR_CODE = "REMOTE_SERVICE_ERROR";
    public static final int HTTP_STATUS = 502;

    private final String endpoint;
    private final int remoteHttpStatus;

    public AndesRemoteServiceException(String message, String endpoint, int remoteHttpStatus, Throwable cause) {
        this(message, endpoint, remoteHttpStatus, null, null, cause);
    }

    public AndesRemoteServiceException(String message, String endpoint, int remoteHttpStatus,
                                        List<ApiErrorDetail> details, String traceId, Throwable cause) {
        super(ERROR_CODE, HTTP_STATUS, message, details, traceId, cause);
        this.endpoint = endpoint;
        this.remoteHttpStatus = remoteHttpStatus;
    }

    public String getEndpoint() {
        return endpoint;
    }

    /**
     * The HTTP status returned by the remote service, distinct from {@link #getHttpStatus()}
     * which is the status this exception maps to on our own API surface.
     */
    public int getRemoteHttpStatus() {
        return remoteHttpStatus;
    }
}
