package pe.andes.api.common.util;

import pe.andes.api.common.exception.AndesApiException;
import pe.andes.api.common.model.ApiError;

import java.time.Instant;

/**
 * Pure helper methods to translate {@link AndesApiException} instances into {@link ApiError} payloads.
 */
public final class ErrorUtils {

    private ErrorUtils() {
    }

    public static ApiError toApiError(AndesApiException ex, String traceId) {
        return ApiError.builder()
                .code(ex.getErrorCode())
                .message(ex.getMessage())
                .httpStatus(ex.getHttpStatus())
                .traceId(traceId != null ? traceId : ex.getTraceId())
                .timestamp(Instant.now())
                .details(ex.getDetails())
                .build();
    }

    public static ApiError toApiError(String code, int httpStatus, String message, String traceId) {
        return ApiError.builder()
                .code(code)
                .message(message)
                .httpStatus(httpStatus)
                .traceId(traceId)
                .timestamp(Instant.now())
                .build();
    }
}
