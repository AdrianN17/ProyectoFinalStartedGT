package pe.andes.api.common.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Metadata attached to every {@link ApiResponse}, used for tracing and diagnostics.
 */
public final class ApiMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String traceId;
    private final String correlationId;
    private final String requestId;
    private final String apiVersion;
    private final Instant timestamp;

    public ApiMetadata(String traceId, String correlationId, String requestId,
                        String apiVersion, Instant timestamp) {
        this.traceId = traceId;
        this.correlationId = correlationId;
        this.requestId = requestId;
        this.apiVersion = apiVersion;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getTraceId() {
        return traceId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ApiMetadata that)) {
            return false;
        }
        return Objects.equals(traceId, that.traceId)
                && Objects.equals(correlationId, that.correlationId)
                && Objects.equals(requestId, that.requestId)
                && Objects.equals(apiVersion, that.apiVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(traceId, correlationId, requestId, apiVersion);
    }

    @Override
    public String toString() {
        return "ApiMetadata{" +
                "traceId='" + traceId + '\'' +
                ", correlationId='" + correlationId + '\'' +
                ", requestId='" + requestId + '\'' +
                ", apiVersion='" + apiVersion + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    public static final class Builder {
        private String traceId;
        private String correlationId;
        private String requestId;
        private String apiVersion;
        private Instant timestamp;

        private Builder() {
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder correlationId(String correlationId) {
            this.correlationId = correlationId;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder apiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public ApiMetadata build() {
            return new ApiMetadata(traceId, correlationId, requestId, apiVersion, timestamp);
        }
    }
}
