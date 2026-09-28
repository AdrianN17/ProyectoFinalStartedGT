package pe.andes.api.common.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Metadata extracted from an incoming or outgoing request, used to propagate
 * tracing headers consistently across Server and Client modules.
 */
public final class ApiRequestMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String correlationId;
    private final String requestId;
    private final String apiVersion;
    private final Instant receivedAt;

    public ApiRequestMetadata(String correlationId, String requestId, String apiVersion, Instant receivedAt) {
        this.correlationId = correlationId;
        this.requestId = requestId;
        this.apiVersion = apiVersion;
        this.receivedAt = receivedAt != null ? receivedAt : Instant.now();
    }

    public static Builder builder() {
        return new Builder();
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

    public Instant getReceivedAt() {
        return receivedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ApiRequestMetadata that)) {
            return false;
        }
        return Objects.equals(correlationId, that.correlationId)
                && Objects.equals(requestId, that.requestId)
                && Objects.equals(apiVersion, that.apiVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(correlationId, requestId, apiVersion);
    }

    @Override
    public String toString() {
        return "ApiRequestMetadata{" +
                "correlationId='" + correlationId + '\'' +
                ", requestId='" + requestId + '\'' +
                ", apiVersion='" + apiVersion + '\'' +
                ", receivedAt=" + receivedAt +
                '}';
    }

    public static final class Builder {
        private String correlationId;
        private String requestId;
        private String apiVersion;
        private Instant receivedAt;

        private Builder() {
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

        public Builder receivedAt(Instant receivedAt) {
            this.receivedAt = receivedAt;
            return this;
        }

        public ApiRequestMetadata build() {
            return new ApiRequestMetadata(correlationId, requestId, apiVersion, receivedAt);
        }
    }
}
