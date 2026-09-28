package pe.andes.api.common.util;

import pe.andes.api.common.model.ApiError;
import pe.andes.api.common.model.ApiMetadata;
import pe.andes.api.common.model.ApiResponse;

/**
 * Pure helper methods to build {@link ApiResponse} envelopes consistently.
 */
public final class ApiResponseUtils {

    private ApiResponseUtils() {
    }

    public static <T> ApiResponse<T> ok(T data, ApiMetadata metadata) {
        return ApiResponse.success(data, metadata);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.success(data);
    }

    public static <T> ApiResponse<T> fail(ApiError error, ApiMetadata metadata) {
        return ApiResponse.error(error, metadata);
    }

    public static <T> ApiResponse<T> fail(ApiError error) {
        return ApiResponse.error(error);
    }

    public static boolean isSuccessful(ApiResponse<?> response) {
        return response != null && response.isSuccess();
    }
}
