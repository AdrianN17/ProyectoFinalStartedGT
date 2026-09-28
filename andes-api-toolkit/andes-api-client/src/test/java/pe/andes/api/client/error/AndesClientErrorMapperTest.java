package pe.andes.api.client.error;

import org.junit.jupiter.api.Test;
import pe.andes.api.common.exception.*;

import static org.junit.jupiter.api.Assertions.*;

class AndesClientErrorMapperTest {

    private final AndesClientErrorMapper mapper = new AndesClientErrorMapper();

    @Test
    void mapsKnownStatusCodesToDeclaredExceptions() {
        assertInstanceOf(AndesBadRequestException.class, mapper.map("GET /x", 400, null));
        assertInstanceOf(AndesAuthenticationException.class, mapper.map("GET /x", 401, null));
        assertInstanceOf(AndesAuthorizationException.class, mapper.map("GET /x", 403, null));
        assertInstanceOf(AndesNotFoundException.class, mapper.map("GET /x", 404, null));
        assertInstanceOf(AndesConflictException.class, mapper.map("GET /x", 409, null));
        assertInstanceOf(AndesValidationException.class, mapper.map("GET /x", 422, null));
        assertInstanceOf(AndesRemoteServiceException.class, mapper.map("GET /x", 503, null));
    }

    @Test
    void preservesRemoteApiErrorFields() {
        String body = """
                {"code":"CUSTOMER_NOT_FOUND","message":"Customer 42 not found","httpStatus":404,"traceId":"trace-1","details":[]}
                """;
        AndesApiException ex = mapper.map("GET /customers/42", 404, body);

        assertEquals("Customer 42 not found", ex.getMessage());
        assertEquals("trace-1", ex.getTraceId());
    }

    @Test
    void fallsBackToGenericMessageWhenBodyIsNotParseable() {
        AndesApiException ex = mapper.map("GET /x", 500, "not-json");
        assertTrue(ex.getMessage().contains("failed with status 500"));
    }

    @Test
    void remoteServiceExceptionKeepsEndpointAndRemoteStatus() {
        AndesRemoteServiceException ex = (AndesRemoteServiceException) mapper.map("GET /x", 503, null);
        assertEquals("GET /x", ex.getEndpoint());
        assertEquals(503, ex.getRemoteHttpStatus());
    }
}
