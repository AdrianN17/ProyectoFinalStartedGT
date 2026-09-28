package pe.edu.galaxy.training.java.audit.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pe.edu.galaxy.training.java.audit.message.AuditLogMessage;
import pe.edu.galaxy.training.java.audit.producer.MessageProducer;

@RequiredArgsConstructor
@Component
public class AuditLogMessageListener {

    private final MessageProducer messageProducer;

    @EventListener
    @Async
    public void handleAsyncEvent(AuditLogMessage event) {
        messageProducer.send(event);
    }
}
