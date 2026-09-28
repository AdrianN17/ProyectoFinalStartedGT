package pe.edu.galaxy.training.java.logs.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "oms.logs")
public class LogsProperties {
    private boolean enabled = true;
    private String serviceName = "unknown-service";
    private String traceHeaderName = "X-Trace-Id";
    private String correlationHeaderName = "X-Correlation-Id";
    private boolean logRequestBody = false;
    private boolean logResponseBody = false;
    private boolean includeHeaders = true;
    private String[] excludedPaths = {"/actuator/health", "/actuator/prometheus", "/swagger-ui", "/v3/api-docs"};
}
