package pe.andes.api.server.error;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pe.andes.api.common.exception.AndesNotFoundException;
import pe.andes.api.common.model.ApiResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(List.of(), false);

    @Test
    void mapsAndesApiExceptionToItsDeclaredHttpStatus() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleAndesApiException(new AndesNotFoundException("missing"));

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("NOT_FOUND", response.getBody().getError().getCode());
    }

    @Test
    void genericExceptionMapsTo500WithoutStackTraceByDefault() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleGenericException(new RuntimeException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().getError().getMessage());
    }

    @Test
    void customMapperIsUsedWhenRegistered() {
        AndesExceptionMapper<IllegalStateException> mapper = new AndesExceptionMapper<>() {
            @Override
            public Class<IllegalStateException> getExceptionType() {
                return IllegalStateException.class;
            }

            @Override
            public int getHttpStatus() {
                return 418;
            }

            @Override
            public pe.andes.api.common.model.ApiError map(IllegalStateException exception, String traceId) {
                return pe.andes.api.common.model.ApiError.builder()
                        .code("TEAPOT")
                        .message(exception.getMessage())
                        .httpStatus(418)
                        .build();
            }
        };
        GlobalExceptionHandler handlerWithMapper = new GlobalExceptionHandler(List.of(mapper), false);

        ResponseEntity<ApiResponse<Void>> response =
                handlerWithMapper.handleGenericException(new IllegalStateException("teapot"));

        assertEquals(418, response.getStatusCode().value());
        assertEquals("TEAPOT", response.getBody().getError().getCode());
    }
}
