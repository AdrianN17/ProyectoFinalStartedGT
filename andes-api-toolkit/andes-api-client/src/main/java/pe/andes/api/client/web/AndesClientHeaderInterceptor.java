package pe.andes.api.client.web;

import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import pe.andes.api.client.config.AndesClientProperties;
import pe.andes.api.common.http.AndesApiConstants;
import pe.andes.api.common.http.AndesContentTypes;
import pe.andes.api.common.http.AndesHeaders;
import pe.andes.api.common.util.HeaderUtils;

import java.io.IOException;
import java.util.Map;

/**
 * Adds correlation id, request id, content type and default headers to every outgoing
 * request of a named client. When invoked within a Server request already carrying a
 * correlation id in {@link MDC} (see {@code CorrelationIdFilter} in andes-api-server),
 * that id is propagated instead of generating a new one.
 */
public class AndesClientHeaderInterceptor implements ClientHttpRequestInterceptor {

    private final AndesClientProperties.ClientConfig config;

    public AndesClientHeaderInterceptor(AndesClientProperties.ClientConfig config) {
        this.config = config;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        HttpHeaders headers = request.getHeaders();

        if (config.isCorrelationIdEnabled() && !headers.containsKey(AndesHeaders.CORRELATION_ID)) {
            String correlationId = MDC.get(AndesApiConstants.MDC_CORRELATION_ID);
            headers.add(AndesHeaders.CORRELATION_ID,
                    HeaderUtils.isValidCorrelationId(correlationId) ? correlationId : HeaderUtils.generateCorrelationId());
        }
        if (config.isRequestIdEnabled() && !headers.containsKey(AndesHeaders.REQUEST_ID)) {
            headers.add(AndesHeaders.REQUEST_ID, HeaderUtils.generateRequestId());
        }
        if (!headers.containsKey(AndesHeaders.CONTENT_TYPE)) {
            headers.add(AndesHeaders.CONTENT_TYPE, AndesContentTypes.APPLICATION_JSON);
        }
        for (Map.Entry<String, String> header : config.getDefaultHeaders().entrySet()) {
            if (!headers.containsKey(header.getKey())) {
                headers.add(header.getKey(), header.getValue());
            }
        }

        return execution.execute(request, body);
    }
}
