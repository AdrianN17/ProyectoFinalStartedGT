package pe.edu.galaxy.training.java.sensitive.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;
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
import pe.edu.galaxy.training.java.sensitive.service.impl.LoggingSensitiveAuditService;
import pe.edu.galaxy.training.java.sensitive.service.impl.VaultEncryptServiceImpl;

import java.net.URI;

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
    @ConditionalOnClass(VaultTemplate.class)
    public VaultTemplate vaultTemplate(SensitiveSecurityProperties properties) {
        VaultEndpoint endpoint = VaultEndpoint.from(URI.create(
                properties.getEncrypt().getVault().getUri()
        ));

        TokenAuthentication authentication = new TokenAuthentication(
                properties.getEncrypt().getVault().getToken()
        );

        return new VaultTemplate(endpoint, authentication);
    }

    @Bean
    @ConditionalOnMissingBean
    public EncryptService encryptService(VaultTemplate vaultTemplate,
                                         SensitiveSecurityProperties properties) {
        return new VaultEncryptServiceImpl(vaultTemplate, properties);
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
