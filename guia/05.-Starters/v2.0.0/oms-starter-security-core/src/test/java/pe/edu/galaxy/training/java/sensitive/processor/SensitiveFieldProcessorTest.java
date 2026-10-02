package pe.edu.galaxy.training.java.sensitive.processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.edu.galaxy.training.java.sensitive.annotation.Encrypt;
import pe.edu.galaxy.training.java.sensitive.annotation.Mask;
import pe.edu.galaxy.training.java.sensitive.annotation.Sensitive;
import pe.edu.galaxy.training.java.sensitive.enums.EncryptionProvider;
import pe.edu.galaxy.training.java.sensitive.enums.MaskType;
import pe.edu.galaxy.training.java.sensitive.enums.SensitivityLevel;
import pe.edu.galaxy.training.java.sensitive.policy.SensitivePolicy;
import pe.edu.galaxy.training.java.sensitive.properties.SensitiveSecurityProperties;
import pe.edu.galaxy.training.java.sensitive.service.EncryptService;
import pe.edu.galaxy.training.java.sensitive.service.SensitiveAuditService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SensitiveFieldProcessorTest {

    private EncryptService encryptService;
    private SensitiveAuditService auditService;
    private SensitiveSecurityProperties properties;
    private SensitivePolicy sensitivePolicy;
    private SensitiveFieldProcessor processor;

    @BeforeEach
    void setUp() {
        encryptService = mock(EncryptService.class);
        auditService = mock(SensitiveAuditService.class);
        properties = new SensitiveSecurityProperties();
        sensitivePolicy = mock(SensitivePolicy.class);
        processor = new SensitiveFieldProcessor(encryptService, auditService, properties, sensitivePolicy);
    }

    @Test
    void encryptObjectEncryptsAnnotatedFieldsUsingSupportedProvider() {
        when(encryptService.supports(EncryptionProvider.DEFAULT)).thenReturn(true);
        when(encryptService.encrypt("4111111111111111")).thenReturn("ENC(4111111111111111)");
        CardHolder holder = new CardHolder("4111111111111111", "John Doe");

        processor.encryptObject(holder);

        assertThat(holder.cardNumber).isEqualTo("ENC(4111111111111111)");
        assertThat(holder.holderName).isEqualTo("John Doe");
    }

    @Test
    void decryptObjectDecryptsAnnotatedFields() {
        when(encryptService.supports(EncryptionProvider.DEFAULT)).thenReturn(true);
        when(encryptService.decrypt("ENC(4111111111111111)")).thenReturn("4111111111111111");
        CardHolder holder = new CardHolder("ENC(4111111111111111)", "John Doe");

        processor.decryptObject(holder);

        assertThat(holder.cardNumber).isEqualTo("4111111111111111");
    }

    @Test
    void processSkipsEncryptionWhenGloballyDisabled() {
        properties.getEncrypt().setEnabled(false);
        CardHolder holder = new CardHolder("4111111111111111", "John Doe");

        processor.encryptObject(holder);

        assertThat(holder.cardNumber).isEqualTo("4111111111111111");
        verifyNoInteractions(encryptService);
    }

    @Test
    void processSkipsFieldsWithNoneProvider() {
        NoneProviderHolder holder = new NoneProviderHolder("raw-value");

        processor.encryptObject(holder);

        assertThat(holder.value).isEqualTo("raw-value");
        verifyNoInteractions(encryptService);
    }

    @Test
    void processThrowsWhenProviderNotSupported() {
        when(encryptService.supports(EncryptionProvider.DEFAULT)).thenReturn(false);
        CardHolder holder = new CardHolder("4111111111111111", "John Doe");

        assertThatThrownBy(() -> processor.encryptObject(holder))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Unsupported encryption provider");
    }

    @Test
    void processSkipsBlankValues() {
        CardHolder holder = new CardHolder("", "John Doe");

        processor.encryptObject(holder);

        assertThat(holder.cardNumber).isEmpty();
        verifyNoInteractions(encryptService);
    }

    @Test
    void processNotifiesAuditServiceForSensitiveFieldsOnDecryptWhenAuditEnabled() {
        CardHolder holder = new CardHolder("4111111111111111", "John Doe");
        when(encryptService.supports(any())).thenReturn(true);

        processor.decryptObject(holder);

        verify(auditService).fieldAccessed(any(), org.mockito.ArgumentMatchers.eq("holderName"), any());
    }

    @Test
    void processDoesNotAuditOnEncryptMode() {
        CardHolder holder = new CardHolder("4111111111111111", "John Doe");
        when(encryptService.supports(any())).thenReturn(true);

        processor.encryptObject(holder);

        verifyNoInteractions(auditService);
    }

    @Test
    void decryptResultHandlesOptionalIterableAndPlainValue() {
        when(encryptService.supports(any())).thenReturn(true);
        when(encryptService.decrypt(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CardHolder single = new CardHolder("enc-1", "A");
        processor.decryptResult(java.util.Optional.of(single));
        processor.decryptResult(List.of(new CardHolder("enc-2", "B")));
        processor.decryptResult((Object) null);

        // No exception thrown for null, Optional, or Iterable branches.
        assertThat(single.holderName).isEqualTo("A");
    }

    @Test
    void sanitizeForAuditReturnsSimpleTypesUnchanged() {
        assertThat(processor.sanitizeForAudit("plain")).isEqualTo("plain");
        assertThat(processor.sanitizeForAudit(42)).isEqualTo(42);
        assertThat(processor.sanitizeForAudit(null)).isNull();
    }

    @Test
    void sanitizeForAuditAppliesPolicyToSensitiveFields() {
        properties.getAudit().setEnabled(true);
        when(sensitivePolicy.apply(org.mockito.ArgumentMatchers.eq("John Doe"),
                org.mockito.ArgumentMatchers.eq(SensitivityLevel.HIGH),
                any()))
                .thenReturn("J*** D**");
        CardHolder holder = new CardHolder("4111111111111111", "John Doe");

        Object sanitized = processor.sanitizeForAudit(holder);

        assertThat(sanitized).isInstanceOf(Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) sanitized;
        assertThat(payload.get("holderName")).isEqualTo("J*** D**");
        assertThat(payload.get("cardNumber")).isEqualTo("4111111111111111");
    }

    @Test
    void sanitizeForAuditWalksThroughIterablesAndMaps() {
        List<String> values = List.of("plain1", "plain2");

        Object sanitized = processor.sanitizeForAudit(values);

        assertThat(sanitized).isEqualTo(values);
    }

    private static class CardHolder {
        @Encrypt
        String cardNumber;

        @Sensitive(level = SensitivityLevel.HIGH)
        @Mask(type = MaskType.CARD)
        String holderName;

        CardHolder() {
        }

        CardHolder(String cardNumber, String holderName) {
            this.cardNumber = cardNumber;
            this.holderName = holderName;
        }
    }

    private static class NoneProviderHolder {
        @Encrypt(provider = EncryptionProvider.NONE)
        String value;

        NoneProviderHolder(String value) {
            this.value = value;
        }
    }
}
