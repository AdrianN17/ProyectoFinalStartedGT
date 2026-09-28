package pe.andes.api.common.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Standard error payload returned inside {@link ApiResponse#getError()}.
 */
public final class ApiError implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String code;
    private final String message;
    private final int httpStatus;
    private final String traceId;
    private final Instant timestamp;
    private final List<ApiErrorDetail> details;

    public ApiError(String code, String message, int httpStatus, String traceId,
                     Instant timestamp, List<ApiErrorDetail> details) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
        this.traceId = traceId;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.details = details != null ? List.copyOf(details) : Collections.emptyList();
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getTraceId() {
        return traceId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public List<ApiErrorDetail> getDetails() {
        return details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ApiError apiError)) {
            return false;
        }
        return httpStatus == apiError.httpStatus
                && Objects.equals(code, apiError.code)
                && Objects.equals(message, apiError.message)
                && Objects.equals(traceId, apiError.traceId)
                && Objects.equals(details, apiError.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, message, httpStatus, traceId, details);
    }

    @Override
    public String toString() {
        return "ApiError{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                ", httpStatus=" + httpStatus +
                ", traceId='" + traceId + '\'' +
                ", timestamp=" + timestamp +
                ", details=" + details +
                '}';
    }

    public static final class Builder {
        private String code;
        private String message;
        private int httpStatus;
        private String traceId;
        private Instant timestamp;
        private List<ApiErrorDetail> details;

        private Builder() {
        }

        public Builder code(String code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder httpStatus(int httpStatus) {
            this.httpStatus = httpStatus;
            return this;
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder details(List<ApiErrorDetail> details) {
            this.details = details;
            return this;
        }

        public ApiError build() {
            return new ApiError(code, message, httpStatus, traceId, timestamp, details);
        }
    }
}
