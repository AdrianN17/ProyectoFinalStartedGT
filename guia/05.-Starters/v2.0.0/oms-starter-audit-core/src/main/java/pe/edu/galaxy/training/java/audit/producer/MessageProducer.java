package pe.edu.galaxy.training.java.audit.producer;

import pe.edu.galaxy.training.java.audit.message.AuditLogMessage;

public interface MessageProducer {
    void send(AuditLogMessage auditLogMessage);
}
