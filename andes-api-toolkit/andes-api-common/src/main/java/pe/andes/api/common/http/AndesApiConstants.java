package pe.andes.api.common.http;

/**
 * Miscellaneous constants shared by Server and Client modules (default values,
 * MDC keys, configuration prefixes, etc).
 */
public final class AndesApiConstants {

    private AndesApiConstants() {
    }

    public static final String CONFIG_PREFIX = "andes.api";
    public static final String MDC_CORRELATION_ID = "correlationId";
    public static final String MDC_REQUEST_ID = "requestId";
    public static final String MDC_TRACE_ID = "traceId";
    public static final String DEFAULT_API_VERSION = "v1";
}
