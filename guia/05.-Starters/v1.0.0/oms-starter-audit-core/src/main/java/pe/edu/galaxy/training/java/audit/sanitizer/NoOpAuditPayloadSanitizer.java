package pe.edu.galaxy.training.java.audit.sanitizer;

import java.util.logging.Logger;

public class NoOpAuditPayloadSanitizer
        implements AuditPayloadSanitizer {
    private static final Logger log =
            Logger.getLogger(NoOpAuditPayloadSanitizer.class.getName());
    @Override
    public Object sanitize(Object value) {
        log.info(">>> NoOpAuditPayloadSanitizer sanitize");
        return value;
    }
}
