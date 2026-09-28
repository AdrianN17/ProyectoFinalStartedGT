package pe.edu.galaxy.training.java.sensitive.service.impl;

import org.springframework.vault.core.VaultTemplate;
import pe.edu.galaxy.training.java.sensitive.enums.EncryptionProvider;
import pe.edu.galaxy.training.java.sensitive.properties.SensitiveSecurityProperties;
import pe.edu.galaxy.training.java.sensitive.service.EncryptService;

public class VaultEncryptServiceImpl implements EncryptService {

    private static final String PREFIX = "vault:";

    private final VaultTemplate vaultTemplate;
    private final SensitiveSecurityProperties properties;

    public VaultEncryptServiceImpl(VaultTemplate vaultTemplate,
                                   SensitiveSecurityProperties properties) {
        this.vaultTemplate = vaultTemplate;
        this.properties = properties;
    }

    @Override
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return plainText;
        }

        if (plainText.startsWith(PREFIX)) {
            return plainText;
        }

        String cipherText = vaultTemplate.opsForTransit()
                .encrypt(properties.getEncrypt().getVault().getTransitKey(), plainText);

        return PREFIX + cipherText;
    }

    @Override
    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isBlank()) {
            return cipherText;
        }

        if (!cipherText.startsWith(PREFIX)) {
            return cipherText;
        }

        String value = cipherText.substring(PREFIX.length());

        return vaultTemplate.opsForTransit()
                .decrypt(properties.getEncrypt().getVault().getTransitKey(), value);
    }

    @Override
    public boolean supports(EncryptionProvider provider) {
        return EncryptionProvider.VAULT.equals(provider)
                || EncryptionProvider.DEFAULT.equals(provider);
    }
}
