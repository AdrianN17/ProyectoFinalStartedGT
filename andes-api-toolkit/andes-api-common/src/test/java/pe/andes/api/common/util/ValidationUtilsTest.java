package pe.andes.api.common.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Test
    void blankDetection() {
        assertTrue(ValidationUtils.isBlank(null));
        assertTrue(ValidationUtils.isBlank("   "));
        assertFalse(ValidationUtils.isBlank("value"));
    }

    @Test
    void emptyCollectionDetection() {
        assertTrue(ValidationUtils.isEmpty(null));
        assertTrue(ValidationUtils.isEmpty(List.of()));
        assertFalse(ValidationUtils.isEmpty(List.of("a")));
    }

    @Test
    void requireNonBlankThrowsOnBlank() {
        assertThrows(IllegalArgumentException.class, () -> ValidationUtils.requireNonBlank(" ", "required"));
    }

    @Test
    void validEmailFormats() {
        assertTrue(ValidationUtils.isValidEmail("user@example.com"));
        assertFalse(ValidationUtils.isValidEmail("invalid-email"));
    }
}
