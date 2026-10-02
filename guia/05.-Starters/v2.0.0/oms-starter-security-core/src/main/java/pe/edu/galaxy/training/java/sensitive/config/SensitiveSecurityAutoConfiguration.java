package pe.edu.galaxy.training.java.sensitive.config;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jackson2.autoconfigure.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import pe.edu.galaxy.training.java.audit.sanitizer.AuditPayloadSanitizer;
import pe.edu.galaxy.training.java.sensitive.aspect.SensitiveRepositoryAspect;
import pe.edu.galaxy.training.java.sensitive.jackson.SensitiveBeanSerializerModifier;
import pe.edu.galaxy.training.java.sensitive.policy.DefaultSensitivePolicy;
import pe.edu.galaxy.training.java.sensitive.policy.SensitivePolicy;
import pe.edu.galaxy.training.java.sensitive.processor.SensitiveFieldProcessor;
import pe.edu.galaxy.training.java.sensitive.properties.SensitiveSecurityProperties;
import pe.edu.galaxy.training.java.sensitive.sanitizer.SensitiveAuditPayloadSanitizer;
import pe.edu.galaxy.training.java.sensitive.service.EncryptService;
import pe.edu.galaxy.training.java.sensitive.service.MaskService;
import pe.edu.galaxy.training.java.sensitive.service.SensitiveAuditService;
import pe.edu.galaxy.training.java.sensitive.service.impl.DefaultMaskService;
import pe.edu.galaxy.training.java.sensitive.service.impl.KeyVaultEncryptServiceImpl;
import pe.edu.galaxy.training.java.sensitive.service.impl.LoggingSensitiveAuditService;
import pe.edu.galaxy.training.java.sensitive.service.impl.NoOpEncryptServiceImpl;

@AutoConfiguration
@EnableConfigurationProperties(SensitiveSecurityProperties.class)
@ConditionalOnProperty(
        prefix = "oms.sensitive",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class SensitiveSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TokenCredential azureKeyVaultCredential() {
        return new DefaultAzureCredentialBuilder().build();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(CryptographyClient.class)
    @ConditionalOnProperty(prefix = "oms.sensitive.encrypt", name = "enabled", havingValue = "true", matchIfMissing = true)
    public CryptographyClient cryptographyClient(SensitiveSecurityProperties properties,
                                                  TokenCredential azureKeyVaultCredential) {

        SensitiveSecurityProperties.AzureKeyVault keyVaultProperties = properties.getEncrypt().getAzureKeyVault();

        KeyClient keyClient = new KeyClientBuilder()
                .vaultUrl(keyVaultProperties.getVaultUrl())
                .credential(azureKeyVaultCredential)
                .buildClient();

        KeyVaultKey key = keyVaultProperties.getKeyVersion() != null && !keyVaultProperties.getKeyVersion().isBlank()
                ? keyClient.getKey(keyVaultProperties.getKeyName(), keyVaultProperties.getKeyVersion())
                : keyClient.getKey(keyVaultProperties.getKeyName());

        return new CryptographyClientBuilder()
                .keyIdentifier(key.getId())
                .credential(azureKeyVaultCredential)
                .buildClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(CryptographyClient.class)
    public EncryptService encryptService(CryptographyClient cryptographyClient,
                                         SensitiveSecurityProperties properties) {
        return new KeyVaultEncryptServiceImpl(cryptographyClient, properties);
    }

    /**
     * Respaldo activo cuando {@code oms.sensitive.encrypt.enabled=false} (o no hay
     * {@code CryptographyClient} disponible): evita construir una conexion eager
     * a Azure Key Vault en escenarios donde el cifrado esta deshabilitado
     * (p.ej. pruebas sin credenciales de {@code az login}).
     */
    @Bean
    @ConditionalOnMissingBean(EncryptService.class)
    public EncryptService noOpEncryptService() {
        return new NoOpEncryptServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public MaskService maskService() {
        return new DefaultMaskService();
    }

    @Bean
    @ConditionalOnMissingBean
    public SensitiveAuditService sensitiveAuditService() {
        return new LoggingSensitiveAuditService();
    }

    @Bean
    @ConditionalOnMissingBean
    public SensitiveFieldProcessor sensitiveFieldProcessor(
            EncryptService encryptService,
            SensitiveAuditService auditService,
            SensitiveSecurityProperties properties,
            SensitivePolicy sensitivePolicy) {

        return new SensitiveFieldProcessor(
                encryptService,
                auditService,
                properties,
                sensitivePolicy
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public SensitiveRepositoryAspect sensitiveRepositoryAspect(SensitiveFieldProcessor processor) {
        return new SensitiveRepositoryAspect(processor);
    }

    @Bean
    public Module sensitiveJacksonModule(MaskService maskService,
                                         SensitiveSecurityProperties properties) {
        SimpleModule module = new SimpleModule("sensitive-security-module");
        module.setSerializerModifier(new SensitiveBeanSerializerModifier(maskService, properties));
        return module;
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer sensitiveJacksonCustomizer(
            @Qualifier("sensitiveJacksonModule") Module sensitiveJacksonModule) {

        return builder -> builder.modules(sensitiveJacksonModule);
    }

    @Bean
    @ConditionalOnMissingBean
    public SensitivePolicy sensitivePolicy(
            MaskService maskService) {

        return new DefaultSensitivePolicy(maskService);
    }
    @Bean
    @Primary
    public AuditPayloadSanitizer sensitiveAuditPayloadSanitizer(
            SensitiveFieldProcessor processor) {

        return new SensitiveAuditPayloadSanitizer(processor);
    }
}
