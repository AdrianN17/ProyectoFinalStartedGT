package pe.edu.galaxy.training.java.audit.sanitizer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoOpAuditPayloadSanitizerTest {

    private final NoOpAuditPayloadSanitizer sanitizer = new NoOpAuditPayloadSanitizer();

    @Test
    void sanitizeReturnsSameValueUnmodified() {
        Object payload = new Object();

        assertThat(sanitizer.sanitize(payload)).isSameAs(payload);
    }

    @Test
    void sanitizeHandlesNullValue() {
        assertThat(sanitizer.sanitize(null)).isNull();
    }
}
