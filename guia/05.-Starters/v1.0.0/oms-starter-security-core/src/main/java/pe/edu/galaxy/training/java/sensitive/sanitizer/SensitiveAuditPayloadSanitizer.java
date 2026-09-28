package pe.edu.galaxy.training.java.sensitive.sanitizer;

import pe.edu.galaxy.training.java.audit.sanitizer.AuditPayloadSanitizer;
import pe.edu.galaxy.training.java.sensitive.processor.SensitiveFieldProcessor;

import java.util.logging.Logger;

public class SensitiveAuditPayloadSanitizer implements AuditPayloadSanitizer {
    private static final Logger log =
            Logger.getLogger(SensitiveAuditPayloadSanitizer.class.getName());
    private final SensitiveFieldProcessor sensitiveFieldProcessor;

    public SensitiveAuditPayloadSanitizer(
            SensitiveFieldProcessor sensitiveFieldProcessor) {
        this.sensitiveFieldProcessor = sensitiveFieldProcessor;
    }

    @Override
    public Object sanitize(Object value) {
        log.info(">>> SensitiveAuditPayloadSanitizer ejecutado");
        return sensitiveFieldProcessor.sanitizeForAudit(value);
    }
}