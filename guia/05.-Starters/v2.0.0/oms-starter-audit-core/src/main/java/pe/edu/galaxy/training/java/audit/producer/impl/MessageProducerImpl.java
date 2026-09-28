package pe.edu.galaxy.training.java.audit.producer.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pe.edu.galaxy.training.java.audit.message.AuditLogMessage;
import pe.edu.galaxy.training.java.audit.producer.MessageProducer;
import pe.edu.galaxy.training.java.audit.properties.AuditProperties;
import java.util.UUID;

//@RequiredArgsConstructor
@Slf4j
@Component
public class MessageProducerImpl implements MessageProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final AuditProperties properties;

    @Override
    public void send(AuditLogMessage auditLogMessage) {
        try {
            String json= objectMapper.writeValueAsString(auditLogMessage);
            log.info("TopicName =>{}",properties.getKafka().getTopicName());
            log.info("json =>{}",json);
            kafkaTemplate.send(properties.getKafka().getTopicName(),UUID.randomUUID().toString(),json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    public MessageProducerImpl(@Qualifier("auditKafkaTemplate") KafkaTemplate<String, String> kafkaTemplate,
                               @Qualifier("auditObjectMapper") ObjectMapper objectMapper,
                               AuditProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }
}
