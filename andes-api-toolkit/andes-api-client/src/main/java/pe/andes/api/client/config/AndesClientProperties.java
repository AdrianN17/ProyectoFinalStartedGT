package pe.andes.api.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Root configuration properties for andes-api-client, bound under {@code andes.api.client}.
 *
 * <pre>{@code
 * andes:
 *   api:
 *     client:
 *       clients:
 *         customer:
 *           base-url: ${CUSTOMER_API_URL}
 *           connect-timeout: 2s
 *           read-timeout: 5s
 * }</pre>
 */
@ConfigurationProperties(prefix = "andes.api.client")
public class AndesClientProperties {

    /** Named client configurations, keyed by logical client name (e.g. "customer", "payment"). */
    private Map<String, ClientConfig> clients = new LinkedHashMap<>();

    public Map<String, ClientConfig> getClients() {
        return clients;
    }

    public void setClients(Map<String, ClientConfig> clients) {
        this.clients = clients;
    }

    public static class ClientConfig {
        private String baseUrl;
        private Duration connectTimeout = Duration.ofSeconds(2);
        private Duration readTimeout = Duration.ofSeconds(5);
        private boolean correlationIdEnabled = true;
        private boolean requestIdEnabled = true;
        private Map<String, String> defaultHeaders = new LinkedHashMap<>();

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public Duration getConnectTimeout() {
            return connectTimeout;
        }

        public void setConnectTimeout(Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        public Duration getReadTimeout() {
            return readTimeout;
        }

        public void setReadTimeout(Duration readTimeout) {
            this.readTimeout = readTimeout;
        }

        public boolean isCorrelationIdEnabled() {
            return correlationIdEnabled;
        }

        public void setCorrelationIdEnabled(boolean correlationIdEnabled) {
            this.correlationIdEnabled = correlationIdEnabled;
        }

        public boolean isRequestIdEnabled() {
            return requestIdEnabled;
        }

        public void setRequestIdEnabled(boolean requestIdEnabled) {
            this.requestIdEnabled = requestIdEnabled;
        }

        public Map<String, String> getDefaultHeaders() {
            return defaultHeaders;
        }

        public void setDefaultHeaders(Map<String, String> defaultHeaders) {
            this.defaultHeaders = defaultHeaders;
        }
    }
}
