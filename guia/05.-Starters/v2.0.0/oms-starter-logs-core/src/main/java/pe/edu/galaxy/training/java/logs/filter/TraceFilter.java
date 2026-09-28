package pe.edu.galaxy.training.java.logs.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.edu.galaxy.training.java.logs.properties.LogsProperties;
import pe.edu.galaxy.training.java.logs.util.LogConstants;
import pe.edu.galaxy.training.java.logs.util.TraceIdGenerator;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
public class TraceFilter extends OncePerRequestFilter {

    private final LogsProperties properties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return Arrays.stream(properties.getExcludedPaths()).anyMatch(uri::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String traceId = this.getOrCreateTraceId(request.getHeader(properties.getTraceHeaderName()));
        String correlationId = this.getOrCreateTraceId(request.getHeader(properties.getCorrelationHeaderName()));

        long start = System.currentTimeMillis();

        try {
            MDC.put(LogConstants.TRACE_ID, traceId);
            MDC.put(LogConstants.CORRELATION_ID, correlationId);
            MDC.put(LogConstants.SERVICE_NAME, properties.getServiceName());

            response.setHeader(properties.getTraceHeaderName(), traceId);
            response.setHeader(properties.getCorrelationHeaderName(), correlationId);

            log.info("HTTP_REQUEST:: method={} uri={} query={} remoteAddress={}", request.getMethod(), request.getRequestURI(), request.getQueryString(), request.getRemoteAddr());

            filterChain.doFilter(request, response);

            log.info("HTTP_RESPONSE:: method={} uri={} status={} elapsedMs={}", request.getMethod(), request.getRequestURI(), response.getStatus(), System.currentTimeMillis() - start);
        } finally {
            MDC.clear();
        }
    }

    private String getOrCreateTraceId(String value) {
        return value == null || value.isBlank() ? TraceIdGenerator.generate() : value;
    }
}
