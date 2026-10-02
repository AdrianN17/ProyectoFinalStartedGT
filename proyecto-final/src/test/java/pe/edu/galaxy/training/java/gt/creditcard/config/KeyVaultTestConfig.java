package pe.edu.galaxy.training.java.gt.creditcard.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Base64;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;

/**
 * Simula Azure Key Vault en pruebas, sin necesitar una suscripcion/Key Vault real ni
 * {@code az login}: registra un {@link CryptographyClient} mockeado con Mockito cuyo
 * {@code encrypt}/{@code decrypt} hacen una transformacion reversible local (Base64) en vez
 * de llamar al servicio remoto.
 *
 * <p>Gracias al gating de {@code oms-starter-security-core} (ver
 * {@code SensitiveSecurityAutoConfiguration}: {@code cryptographyClient} es
 * {@code @ConditionalOnMissingBean}), al registrar este bean el starter deja de construir su
 * propio {@code CryptographyClient} real (que intentaria conectarse a
 * {@code https://CHANGE-ME.vault.azure.net} y fallaria con {@code UnknownHostException}), pero
 * SI sigue creando su {@code KeyVaultEncryptServiceImpl} de produccion usando este mock
 * ({@code encryptService} es {@code @ConditionalOnBean(CryptographyClient.class)}), validando
 * asi el flujo completo de cifrado/descifrado de principio a fin.</p>
 */
@TestConfiguration
public class KeyVaultTestConfig {

    @Bean
    public CryptographyClient cryptographyClient() {
        CryptographyClient client = mock(CryptographyClient.class);

        when(client.encrypt(any(EncryptionAlgorithm.class), any(byte[].class)))
                .thenAnswer(invocation -> {
                    byte[] plainText = invocation.getArgument(1);
                    byte[] cipherText = Base64.getEncoder().encode(plainText);
                    return new EncryptResult(cipherText, invocation.getArgument(0), "test-key");
                });

        when(client.decrypt(any(EncryptionAlgorithm.class), any(byte[].class)))
                .thenAnswer(invocation -> {
                    byte[] cipherText = invocation.getArgument(1);
                    byte[] plainText = Base64.getDecoder().decode(cipherText);
                    return new DecryptResult(plainText, invocation.getArgument(0), "test-key");
                });

        return client;
    }
}
