package pe.edu.galaxy.training.java.sensitive.policy;

import org.junit.jupiter.api.Test;
import pe.edu.galaxy.training.java.sensitive.annotation.Mask;
import pe.edu.galaxy.training.java.sensitive.enums.SensitivityLevel;
import pe.edu.galaxy.training.java.sensitive.service.MaskService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DefaultSensitivePolicyTest {

    private final MaskService maskService = mock(MaskService.class);
    private final DefaultSensitivePolicy policy = new DefaultSensitivePolicy(maskService);

    @Test
    void applyReturnsNullWhenValueIsNull() {
        assertThat(policy.apply(null, SensitivityLevel.HIGH, null)).isNull();
        verifyNoInteractions(maskService);
    }

    @Test
    void applyReturnsValueUnchangedForLowSensitivity() {
        Object result = policy.apply("plain-value", SensitivityLevel.LOW, null);

        assertThat(result).isEqualTo("plain-value");
        verifyNoInteractions(maskService);
    }

    @Test
    void applyDelegatesToMaskServiceWhenMaskProvidedForMediumSensitivity() {
        Mask mask = mock(Mask.class);
        when(maskService.mask("secret", mask)).thenReturn("s****t");

        Object result = policy.apply("secret", SensitivityLevel.MEDIUM, mask);

        assertThat(result).isEqualTo("s****t");
    }

    @Test
    void applyFullyMasksHighSensitivityWhenNoMaskProvided() {
        Object result = policy.apply("secret", SensitivityLevel.HIGH, null);

        assertThat(result).isEqualTo("******");
        verifyNoInteractions(maskService);
    }

    @Test
    void applyReturnsFixedPlaceholderForCriticalSensitivity() {
        Mask mask = mock(Mask.class);

        Object result = policy.apply("top-secret", SensitivityLevel.CRITICAL, mask);

        assertThat(result).isEqualTo("[SENSITIVE]");
        verifyNoInteractions(maskService);
    }

    @Test
    void applyConvertsNonStringValueToStringBeforeMasking() {
        Object result = policy.apply(12345, SensitivityLevel.HIGH, null);

        assertThat(result).isEqualTo("*****");
    }
}
