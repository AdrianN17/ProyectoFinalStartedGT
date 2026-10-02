package pe.edu.galaxy.training.java.sensitive.service.impl;

import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import pe.edu.galaxy.training.java.sensitive.enums.EncryptionProvider;
import pe.edu.galaxy.training.java.sensitive.properties.SensitiveSecurityProperties;
import pe.edu.galaxy.training.java.sensitive.service.EncryptService;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Implementacion de {@link EncryptService} que delega el cifrado/descifrado
 * en Azure Key Vault (operaciones criptograficas sobre una clave administrada),
 * en reemplazo del motor Transit de HashiCorp Vault usado previamente.
 */
public class KeyVaultEncryptServiceImpl implements EncryptService {

    private static final String PREFIX = "akv:";

    private final CryptographyClient cryptographyClient;
    private final SensitiveSecurityProperties properties;

    public KeyVaultEncryptServiceImpl(CryptographyClient cryptographyClient,
                                       SensitiveSecurityProperties properties) {
        this.cryptographyClient = cryptographyClient;
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

        EncryptionAlgorithm algorithm = algorithm();

        EncryptResult result = cryptographyClient.encrypt(
                algorithm,
                plainText.getBytes(StandardCharsets.UTF_8));

        String cipherText = Base64.getEncoder().encodeToString(result.getCipherText());

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

        byte[] rawCipherText = Base64.getDecoder().decode(cipherText.substring(PREFIX.length()));

        DecryptResult result = cryptographyClient.decrypt(algorithm(), rawCipherText);

        return new String(result.getPlainText(), StandardCharsets.UTF_8);
    }

    @Override
    public boolean supports(EncryptionProvider provider) {
        return EncryptionProvider.AZURE_KEY_VAULT.equals(provider)
                || EncryptionProvider.DEFAULT.equals(provider);
    }

    private EncryptionAlgorithm algorithm() {
        return EncryptionAlgorithm.fromString(
                properties.getEncrypt().getAzureKeyVault().getAlgorithm());
    }
}
