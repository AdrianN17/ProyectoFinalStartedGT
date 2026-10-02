package pe.edu.galaxy.training.java.audit.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import pe.edu.galaxy.training.java.audit.dto.AuditEvent;
import pe.edu.galaxy.training.java.audit.message.AuditLogMessage;
import pe.edu.galaxy.training.java.audit.properties.AuditProperties;
import pe.edu.galaxy.training.java.audit.sanitizer.AuditPayloadSanitizer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AuditServiceImplTest {

    private ObjectMapper objectMapper;
    private AuditProperties properties;
    private AuditPayloadSanitizer sanitizer;
    private ApplicationEventPublisher publisher;
    private AuditServiceImpl auditService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        properties = new AuditProperties();
        properties.setServiceName("credit-card-service");
        sanitizer = mock(AuditPayloadSanitizer.class);
        when(sanitizer.sanitize(any())).thenAnswer(invocation -> invocation.getArgument(0));
        publisher = mock(ApplicationEventPublisher.class);
        auditService = new AuditServiceImpl(objectMapper, properties, sanitizer, publisher);
    }

    @Test
    void saveDoesNothingWhenAuditDisabled() {
        properties.setEnabled(false);

        auditService.save(AuditEvent.builder().operation("CREATE").entityName("CreditCard").build());

        verifyNoInteractions(publisher);
    }

    @Test
    void savePublishesEventWithRequestAndResponseWhenEnabled() {
        AuditEvent event = AuditEvent.builder()
                .operation("CREATE")
                .entityName("CreditCard")
                .description("Creates a credit card")
                .status("SUCCESS")
                .executionTimeMs(120L)
                .request(java.util.Map.of("id", 1))
                .response(java.util.Map.of("status", "ok"))
                .build();

        auditService.save(event);

        ArgumentCaptor<AuditLogMessage> captor = ArgumentCaptor.forClass(AuditLogMessage.class);
        verify(publisher).publishEvent(captor.capture());
        AuditLogMessage message = captor.getValue();

        assertThat(message.getServiceName()).isEqualTo("credit-card-service");
        assertThat(message.getOperation()).isEqualTo("CREATE");
        assertThat(message.getEntityName()).isEqualTo("CreditCard");
        assertThat(message.getStatus()).isEqualTo("SUCCESS");
        assertThat(message.getRequestPayload()).contains("\"id\":1");
        assertThat(message.getResponsePayload()).contains("ok");
        assertThat(message.getCreatedAt()).isNotNull();
    }

    @Test
    void saveSkipsRequestAndResponseWhenDisabledInProperties() {
        properties.setLogRequest(false);
        properties.setLogResponse(false);

        auditService.save(AuditEvent.builder()
                .operation("UPDATE")
                .entityName("CreditCard")
                .request("req")
                .response("resp")
                .build());

        ArgumentCaptor<AuditLogMessage> captor = ArgumentCaptor.forClass(AuditLogMessage.class);
        verify(publisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getRequestPayload()).isNull();
        assertThat(captor.getValue().getResponsePayload()).isNull();
    }

    @Test
    void saveTruncatesPayloadsLongerThanMaxLength() {
        properties.setMaxPayloadLength(5);
        String longValue = "123456789";

        auditService.save(AuditEvent.builder()
                .operation("CREATE")
                .entityName("CreditCard")
                .request(longValue)
                .build());

        ArgumentCaptor<AuditLogMessage> captor = ArgumentCaptor.forClass(AuditLogMessage.class);
        verify(publisher).publishEvent(captor.capture());
        // The JSON-serialized request ("123456789") is truncated to maxPayloadLength characters.
        assertThat(captor.getValue().getRequestPayload()).hasSize(5);
    }

    @Test
    void saveTruncatesErrorMessageWhenLogErrorsEnabled() {
        properties.setMaxPayloadLength(4);

        auditService.save(AuditEvent.builder()
                .operation("CREATE")
                .entityName("CreditCard")
                .errorMessage("some long error message")
                .build());

        ArgumentCaptor<AuditLogMessage> captor = ArgumentCaptor.forClass(AuditLogMessage.class);
        verify(publisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getErrorMessage()).hasSize(4);
    }

    @Test
    void saveDoesNotLogErrorsWhenDisabled() {
        properties.setLogErrors(false);

        auditService.save(AuditEvent.builder()
                .operation("CREATE")
                .entityName("CreditCard")
                .errorMessage("failure")
                .build());

        ArgumentCaptor<AuditLogMessage> captor = ArgumentCaptor.forClass(AuditLogMessage.class);
        verify(publisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getErrorMessage()).isNull();
    }

    @Test
    void saveHandlesSerializationFailureGracefully() throws JsonProcessingException {
        ObjectMapper failingMapper = mock(ObjectMapper.class);
        when(failingMapper.writeValueAsString(any())).thenThrow(new RuntimeException("serialization failed"));
        AuditServiceImpl serviceWithFailingMapper =
                new AuditServiceImpl(failingMapper, properties, sanitizer, publisher);

        serviceWithFailingMapper.save(AuditEvent.builder()
                .operation("CREATE")
                .entityName("CreditCard")
                .request("payload")
                .build());

        ArgumentCaptor<AuditLogMessage> captor = ArgumentCaptor.forClass(AuditLogMessage.class);
        verify(publisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getRequestPayload()).contains("serializationError");
    }

}
