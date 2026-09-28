package pe.andes.api.server.web;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import pe.andes.api.common.model.ApiMetadata;
import pe.andes.api.common.model.ApiResponse;
import pe.andes.api.common.http.AndesApiConstants;
import org.slf4j.MDC;

/**
 * Automatically wraps successful controller return values into the standard
 * {@link ApiResponse} envelope, unless the value is already an {@code ApiResponse}
 * or the response type is explicitly excluded (e.g. binary payloads, {@code String}
 * bodies handled by {@code StringHttpMessageConverter}, or {@link ResponseStatusException}).
 */
@RestControllerAdvice
public class AndesResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> type = returnType.getParameterType();
        return !ApiResponse.class.isAssignableFrom(type);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                   Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                   ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof ApiResponse<?>) {
            return body;
        }
        ApiMetadata metadata = ApiMetadata.builder()
                .traceId(MDC.get(AndesApiConstants.MDC_CORRELATION_ID))
                .correlationId(MDC.get(AndesApiConstants.MDC_CORRELATION_ID))
                .requestId(MDC.get(AndesApiConstants.MDC_REQUEST_ID))
                .build();
        return ApiResponse.success(body, metadata);
    }
}
