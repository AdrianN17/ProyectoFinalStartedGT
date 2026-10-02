package pe.edu.galaxy.training.java.sensitive.service.impl;

import org.junit.jupiter.api.Test;
import pe.edu.galaxy.training.java.sensitive.enums.EncryptionProvider;

import static org.assertj.core.api.Assertions.assertThat;

class NoOpEncryptServiceImplTest {

    private final NoOpEncryptServiceImpl encryptService = new NoOpEncryptServiceImpl();

    @Test
    void encryptReturnsSamePlainText() {
        assertThat(encryptService.encrypt("plain-text")).isEqualTo("plain-text");
    }

    @Test
    void decryptReturnsSameCipherText() {
        assertThat(encryptService.decrypt("cipher-text")).isEqualTo("cipher-text");
    }

    @Test
    void supportsAlwaysReturnsTrueRegardlessOfProvider() {
        assertThat(encryptService.supports(EncryptionProvider.AZURE_KEY_VAULT)).isTrue();
        assertThat(encryptService.supports(EncryptionProvider.NONE)).isTrue();
        assertThat(encryptService.supports(EncryptionProvider.DEFAULT)).isTrue();
    }
}
