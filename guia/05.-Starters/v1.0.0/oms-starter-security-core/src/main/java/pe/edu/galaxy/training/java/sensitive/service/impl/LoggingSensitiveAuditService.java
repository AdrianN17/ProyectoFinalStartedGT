package pe.edu.galaxy.training.java.sensitive.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.edu.galaxy.training.java.sensitive.annotation.Sensitive;
import pe.edu.galaxy.training.java.sensitive.service.SensitiveAuditService;

public class LoggingSensitiveAuditService implements SensitiveAuditService {

    private static final Logger log = LoggerFactory.getLogger(LoggingSensitiveAuditService.class);

    @Override
    public void fieldAccessed(String className, String fieldName, Sensitive sensitive) {
        log.info("Sensitive field accessed class={} field={} level={} category={}",
                className,
                fieldName,
                sensitive.level(),
                sensitive.category());
    }
}
