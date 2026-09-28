package pe.andes.api.common.util;

import org.junit.jupiter.api.Test;
import pe.andes.api.common.exception.AndesNotFoundException;
import pe.andes.api.common.model.ApiError;

import static org.junit.jupiter.api.Assertions.*;

class ErrorUtilsTest {

    @Test
    void translatesExceptionToApiError() {
        AndesNotFoundException ex = new AndesNotFoundException("Customer not found");
        ApiError error = ErrorUtils.toApiError(ex, "trace-123");

        assertEquals(AndesNotFoundException.ERROR_CODE, error.getCode());
        assertEquals(404, error.getHttpStatus());
        assertEquals("Customer not found", error.getMessage());
        assertEquals("trace-123", error.getTraceId());
    }

    @Test
    void fallsBackToExceptionTraceIdWhenNoneProvided() {
        AndesNotFoundException ex = new AndesNotFoundException("msg", null, "exception-trace", null);
        ApiError error = ErrorUtils.toApiError(ex, null);
        assertEquals("exception-trace", error.getTraceId());
    }
}
