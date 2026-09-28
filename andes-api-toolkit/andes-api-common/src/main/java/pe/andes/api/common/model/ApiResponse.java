package pe.andes.api.common.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Standard envelope for every response produced by Server controllers or
 * consumed by Client components.
 *
 * <pre>{@code
 * {
 *   "success": true,
 *   "data": {},
 *   "error": null,
 *   "metadata": { "traceId": "..." }
 * }
 * }</pre>
 *
 * @param <T> the type of the {@code data} payload
 */
public final class ApiResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final T data;
    private final ApiError error;
    private final ApiMetadata metadata;

    public ApiResponse(boolean success, T data, ApiError error, ApiMetadata metadata) {
        this.success = success;
        this.data = data;
        this.error = error;
        this.metadata = metadata;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> success(T data, ApiMetadata metadata) {
        return new ApiResponse<>(true, data, null, metadata);
    }

    public static <T> ApiResponse<T> error(ApiError error) {
        return new ApiResponse<>(false, null, error, null);
    }

    public static <T> ApiResponse<T> error(ApiError error, ApiMetadata metadata) {
        return new ApiResponse<>(false, null, error, metadata);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public ApiError getError() {
        return error;
    }

    public ApiMetadata getMetadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ApiResponse<?> that)) {
            return false;
        }
        return success == that.success
                && Objects.equals(data, that.data)
                && Objects.equals(error, that.error)
                && Objects.equals(metadata, that.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, data, error, metadata);
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", data=" + data +
                ", error=" + error +
                ", metadata=" + metadata +
                '}';
    }
}
