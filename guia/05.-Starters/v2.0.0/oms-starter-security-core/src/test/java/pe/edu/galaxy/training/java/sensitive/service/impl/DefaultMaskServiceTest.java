package pe.edu.galaxy.training.java.sensitive.service.impl;

import org.junit.jupiter.api.Test;
import pe.edu.galaxy.training.java.sensitive.annotation.Mask;
import pe.edu.galaxy.training.java.sensitive.enums.MaskType;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultMaskServiceTest {

    private final DefaultMaskService maskService = new DefaultMaskService();

    @Test
    void maskReturnsNullOrBlankValueUnchanged() {
        Mask mask = maskFor("email");

        assertThat(maskService.mask(null, mask)).isNull();
        assertThat(maskService.mask("", mask)).isEmpty();
    }

    @Test
    void maskEmailKeepsFirstTwoCharsAndDomain() {
        Mask mask = maskFor("email");

        assertThat(maskService.mask("johndoe@example.com", mask)).isEqualTo("jo*****@example.com");
    }

    @Test
    void maskEmailWithShortLocalPartUsesPartialMasking() {
        Mask mask = maskFor("email");

        assertThat(maskService.mask("j@example.com", mask)).isEqualTo("j************");
    }

    @Test
    void maskPhoneKeepsVisibleEndCharacters() {
        Mask mask = maskFor("phone");

        assertThat(maskService.mask("987654321", mask)).isEqualTo("*****4321");
    }

    @Test
    void maskDocumentKeepsVisibleEndCharacters() {
        Mask mask = maskFor("document");

        assertThat(maskService.mask("12345678", mask)).isEqualTo("****5678");
    }

    @Test
    void maskCardRemovesSpacesAndKeepsLastFourDigits() {
        Mask mask = maskFor("card");

        assertThat(maskService.mask("4111 1111 1111 1234", mask)).isEqualTo("************1234");
    }

    @Test
    void maskFullMasksEveryCharacter() {
        Mask mask = maskFor("full");

        assertThat(maskService.mask("secret", mask)).isEqualTo("******");
    }

    @Test
    void maskPartialKeepsConfiguredVisibleStartAndEnd() {
        Mask mask = maskFor("partial");

        assertThat(maskService.mask("1234567890", mask)).isEqualTo("12****7890");
    }

    @Test
    void maskPartialMasksEntireValueWhenShorterThanVisibleWindow() {
        Mask mask = maskFor("partial");

        assertThat(maskService.mask("12", mask)).isEqualTo("**");
    }

    @Test
    void maskKeepEndMasksEntireValueWhenShorterThanVisibleEnd() {
        Mask mask = maskFor("phone");

        assertThat(maskService.mask("12", mask)).isEqualTo("**");
    }

    private Mask maskFor(String fieldName) {
        try {
            Field field = AnnotatedSample.class.getDeclaredField(fieldName);
            for (Annotation annotation : field.getAnnotations()) {
                if (annotation instanceof Mask mask) {
                    return mask;
                }
            }
            throw new IllegalStateException("Mask annotation not found on " + fieldName);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    private static class AnnotatedSample {
        @Mask(type = MaskType.EMAIL)
        String email;

        @Mask(type = MaskType.PHONE, visibleEnd = 4)
        String phone;

        @Mask(type = MaskType.DOCUMENT, visibleEnd = 4)
        String document;

        @Mask(type = MaskType.CARD)
        String card;

        @Mask(type = MaskType.FULL)
        String full;

        @Mask(type = MaskType.PARTIAL, visibleStart = 2, visibleEnd = 4)
        String partial;
    }
}
