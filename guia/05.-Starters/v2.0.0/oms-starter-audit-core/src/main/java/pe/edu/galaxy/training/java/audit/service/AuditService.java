package pe.edu.galaxy.training.java.audit.service;

import pe.edu.galaxy.training.java.audit.dto.AuditEvent;

public interface AuditService {
    void save(AuditEvent event);
}
