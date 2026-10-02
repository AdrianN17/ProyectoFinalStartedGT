package pe.edu.galaxy.training.java.logs.util;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TraceIdGeneratorTest {

    @Test
    void generateReturnsNonBlankValue() {
        String traceId = TraceIdGenerator.generate();

        assertThat(traceId).isNotNull().isNotBlank();
    }

    @Test
    void generateReturnsValidUuid() {
        String traceId = TraceIdGenerator.generate();

        // Should not throw: confirms the generated value is a well-formed UUID.
        assertThat(UUID.fromString(traceId)).isNotNull();
    }

    @Test
    void generateReturnsUniqueValuesOnEachCall() {
        String first = TraceIdGenerator.generate();
        String second = TraceIdGenerator.generate();

        assertThat(first).isNotEqualTo(second);
    }
}
