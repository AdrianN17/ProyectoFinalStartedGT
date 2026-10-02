package pe.edu.galaxy.training.java.logs.filter;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import pe.edu.galaxy.training.java.logs.properties.LogsProperties;
import pe.edu.galaxy.training.java.logs.util.LogConstants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class TraceFilterTest {

    private final LogsProperties properties = new LogsProperties();
    private final TraceFilter filter = new TraceFilter(properties);

    @Test
    void shouldNotFilterReturnsTrueForExcludedPaths() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/actuator/health");

        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void shouldNotFilterReturnsFalseForRegularPaths() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/credit-cards");

        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    @Test
    void doFilterInternalGeneratesTraceIdWhenHeaderMissingAndEchoesItInResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/credit-cards");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getHeader(properties.getTraceHeaderName())).isNotBlank();
        assertThat(response.getHeader(properties.getCorrelationHeaderName())).isNotBlank();
        verify(filterChain).doFilter(request, response);
        // MDC must be cleared after the request completes.
        assertThat(MDC.get(LogConstants.TRACE_ID)).isNull();
    }

    @Test
    void doFilterInternalReusesIncomingTraceAndCorrelationHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/credit-cards");
        request.addHeader(properties.getTraceHeaderName(), "trace-abc");
        request.addHeader(properties.getCorrelationHeaderName(), "correlation-xyz");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getHeader(properties.getTraceHeaderName())).isEqualTo("trace-abc");
        assertThat(response.getHeader(properties.getCorrelationHeaderName())).isEqualTo("correlation-xyz");
    }

    @Test
    void doFilterInternalClearsMdcEvenWhenChainThrows() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/credit-cards");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);
        doAnswer(invocation -> {
            throw new IllegalStateException("downstream failure");
        }).when(filterChain).doFilter(any(), any());

        try {
            filter.doFilterInternal(request, response, filterChain);
        } catch (IllegalStateException expected) {
            // expected to propagate
        }

        assertThat(MDC.get(LogConstants.TRACE_ID)).isNull();
        assertThat(MDC.get(LogConstants.CORRELATION_ID)).isNull();
        assertThat(MDC.get(LogConstants.SERVICE_NAME)).isNull();
    }
}
