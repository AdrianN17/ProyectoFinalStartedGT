package pe.andes.api.common.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void successResponseCarriesData() {
        ApiResponse<String> response = ApiResponse.success("payload");
        assertTrue(response.isSuccess());
        assertEquals("payload", response.getData());
        assertNull(response.getError());
    }

    @Test
    void errorResponseCarriesError() {
        ApiError error = ApiError.builder().code("NOT_FOUND").httpStatus(404).message("missing").build();
        ApiResponse<Object> response = ApiResponse.error(error);
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals(error, response.getError());
    }
}
