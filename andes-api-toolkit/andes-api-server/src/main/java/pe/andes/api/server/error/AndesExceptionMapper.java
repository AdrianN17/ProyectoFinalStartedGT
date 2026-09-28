package pe.andes.api.server.error;

import pe.andes.api.common.model.ApiError;

/**
 * Extension point to register HTTP mapping for exceptions that are not part of
 * the {@code AndesApiException} hierarchy (e.g. exceptions thrown by third-party
 * libraries). Implementations are picked up as Spring beans by
 * {@link GlobalExceptionHandler}.
 *
 * @param <E> the exception type handled by this mapper
 */
public interface AndesExceptionMapper<E extends Throwable> {

    /**
     * @return the exception type this mapper is able to handle.
     */
    Class<E> getExceptionType();

    /**
     * @return the HTTP status code the mapped response should use.
     */
    int getHttpStatus();

    /**
     * Builds the {@link ApiError} payload for the given exception.
     *
     * @param exception the exception instance to map
     * @param traceId   the current trace id, may be {@code null}
     */
    ApiError map(E exception, String traceId);
}
