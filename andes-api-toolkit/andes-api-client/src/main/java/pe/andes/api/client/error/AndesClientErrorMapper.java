package pe.andes.api.client.error;

import pe.andes.api.common.exception.AndesApiException;
import pe.andes.api.common.exception.AndesAuthenticationException;
import pe.andes.api.common.exception.AndesAuthorizationException;
import pe.andes.api.common.exception.AndesBadRequestException;
import pe.andes.api.common.exception.AndesConflictException;
import pe.andes.api.common.exception.AndesNotFoundException;
import pe.andes.api.common.exception.AndesRemoteServiceException;
import pe.andes.api.common.exception.AndesValidationException;
import pe.andes.api.common.model.ApiError;
import pe.andes.api.common.model.ApiErrorDetail;
import pe.andes.api.common.util.JsonUtils;

import java.util.List;

/**
 * Maps HTTP error responses from remote APIs to the common Andes exception hierarchy:
 *
 * <pre>
 * 404 -&gt; AndesNotFoundException        401 -&gt; AndesAuthenticationException
 * 403 -&gt; AndesAuthorizationException    409 -&gt; AndesConflictException
 * 400 -&gt; AndesBadRequestException       422 -&gt; AndesValidationException
 * 5xx / other -&gt; AndesRemoteServiceException
 * </pre>
 *
 * When the remote body matches the Andes {@link ApiError} contract, its code/message/details/traceId
 * are preserved; otherwise a best-effort generic message is built from the raw response body.
 */
public class AndesClientErrorMapper {

    public AndesApiException map(String endpoint, int statusCode, String responseBody) {
        ApiError remoteError = tryParse(responseBody);
        String message = remoteError != null && remoteError.getMessage() != null
                ? remoteError.getMessage()
                : "Remote call to " + endpoint + " failed with status " + statusCode;
        List<ApiErrorDetail> details = remoteError != null ? remoteError.getDetails() : List.of();
        String traceId = remoteError != null ? remoteError.getTraceId() : null;

        return switch (statusCode) {
            case 400 -> new AndesBadRequestException(message, details, traceId, null);
            case 401 -> new AndesAuthenticationException(message, details, traceId, null);
            case 403 -> new AndesAuthorizationException(message, details, traceId, null);
            case 404 -> new AndesNotFoundException(message, details, traceId, null);
            case 409 -> new AndesConflictException(message, details, traceId, null);
            case 422 -> new AndesValidationException(message, details, traceId, null);
            default -> new AndesRemoteServiceException(message, endpoint, statusCode, details, traceId, null);
        };
    }

    private ApiError tryParse(String body) {
        if (body == null || body.isBlank()) {
            return null;
        }
        try {
            return JsonUtils.fromJson(body, ApiError.class);
        } catch (Exception e) {
            return null;
        }
    }
}
