package pe.edu.galaxy.training.java.audit.producer.impl;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pe.edu.galaxy.training.java.audit.message.AuditLogMessage;
import pe.edu.galaxy.training.java.audit.producer.MessageProducer;
import pe.edu.galaxy.training.java.audit.properties.AuditProperties;
import java.util.UUID;

//@RequiredArgsConstructor
@Slf4j
@Component
@ConditionalOnProperty(prefix = "oms.audit", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MessageProducerImpl implements MessageProducer {

    private final ServiceBusSenderClient serviceBusSenderClient;
    private final ObjectMapper objectMapper;
    private final AuditProperties properties;

    @Override
    public void send(AuditLogMessage auditLogMessage) {
        try {
            String json= objectMapper.writeValueAsString(auditLogMessage);
            log.info("QueueName =>{}", properties.getServiceBus().getQueueName());
            log.info("json =>{}",json);

            ServiceBusMessage message = new ServiceBusMessage(json);
            message.setMessageId(UUID.randomUUID().toString());
            message.setContentType("application/json");

            serviceBusSenderClient.sendMessage(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    public MessageProducerImpl(@Qualifier("auditServiceBusSenderClient") ServiceBusSenderClient serviceBusSenderClient,
                               @Qualifier("auditObjectMapper") ObjectMapper objectMapper,
                               AuditProperties properties) {
        this.serviceBusSenderClient = serviceBusSenderClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }
}
