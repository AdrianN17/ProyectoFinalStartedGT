package pe.edu.galaxy.training.java.audit.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.galaxy.training.java.audit.dto.AuditEvent;
import pe.edu.galaxy.training.java.audit.message.AuditLogMessage;
import pe.edu.galaxy.training.java.audit.properties.AuditProperties;
import pe.edu.galaxy.training.java.audit.sanitizer.AuditPayloadSanitizer;
import pe.edu.galaxy.training.java.audit.service.AuditService;

import java.time.LocalDateTime;

@Service
public class AuditServiceImpl implements AuditService {

    private final ObjectMapper objectMapper;
    private final AuditProperties properties;
    private final AuditPayloadSanitizer auditPayloadSanitizer;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AuditServiceImpl(@Qualifier("auditObjectMapper") ObjectMapper objectMapper,
                            AuditProperties properties,
                            AuditPayloadSanitizer auditPayloadSanitizer,
                            ApplicationEventPublisher applicationEventPublisher) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.auditPayloadSanitizer = auditPayloadSanitizer;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(AuditEvent event) {
        if (!properties.isEnabled()) {
            return;
        }

        AuditLogMessage message = new AuditLogMessage();

        message.setServiceName(properties.getServiceName());
        message.setOperation(event.getOperation());
        message.setEntityName(event.getEntityName());
        message.setDescription(event.getDescription());
        message.setStatus(event.getStatus());
        message.setExecutionTimeMs(event.getExecutionTimeMs());

        message.setCreatedAt(LocalDateTime.now());

        if (properties.isLogRequest()) {
            //message.setRequestPayload(toJson(event.getRequest()));
            message.setRequestPayload(
                    toJson(
                            auditPayloadSanitizer.sanitize(
                                    event.getRequest())));
        }
        if (properties.isLogResponse()) {
            //message.setResponsePayload(toJson(event.getResponse()));
            message.setResponsePayload(
                    toJson(
                            auditPayloadSanitizer.sanitize(
                                    event.getResponse())));
        }
        if (properties.isLogErrors()) {
            message.setErrorMessage(truncate(event.getErrorMessage()));
        }

        applicationEventPublisher.publishEvent(message);
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return truncate(objectMapper.writeValueAsString(value));
        } catch (Exception ex) {
            return "{\"serializationError\":\"" + truncate(ex.getMessage()) + "\"}";
        }
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        int max = properties.getMaxPayloadLength();
        return value.length() <= max ? value : value.substring(0, max);
    }
}
