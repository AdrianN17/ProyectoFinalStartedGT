package pe.andes.api.common.http;

/**
 * Centralized names of HTTP headers used across Server and Client modules.
 */
public final class AndesHeaders {

    private AndesHeaders() {
    }

    public static final String CORRELATION_ID = "X-Correlation-Id";
    public static final String REQUEST_ID = "X-Request-Id";
    public static final String API_VERSION = "X-Api-Version";
    public static final String TRACE_ID = "X-Trace-Id";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String ACCEPT = "Accept";
    public static final String AUTHORIZATION = "Authorization";
}
