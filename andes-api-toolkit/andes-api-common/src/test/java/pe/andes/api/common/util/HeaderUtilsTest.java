package pe.andes.api.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HeaderUtilsTest {

    @Test
    void generatedCorrelationIdIsValid() {
        String id = HeaderUtils.generateCorrelationId();
        assertTrue(HeaderUtils.isValidCorrelationId(id));
    }

    @Test
    void nullOrShortValuesAreInvalid() {
        assertFalse(HeaderUtils.isValidCorrelationId(null));
        assertFalse(HeaderUtils.isValidCorrelationId("short"));
    }

    @Test
    void defaultIfInvalidReturnsGeneratedValueWhenInvalid() {
        String result = HeaderUtils.defaultIfInvalid("bad", false);
        assertTrue(HeaderUtils.isValidCorrelationId(result));
    }

    @Test
    void defaultIfInvalidKeepsOriginalWhenValid() {
        String original = HeaderUtils.generateCorrelationId();
        assertEquals(original, HeaderUtils.defaultIfInvalid(original, true));
    }
}
