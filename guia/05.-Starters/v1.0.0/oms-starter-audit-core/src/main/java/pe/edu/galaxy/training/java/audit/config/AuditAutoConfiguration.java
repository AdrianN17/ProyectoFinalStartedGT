package pe.edu.galaxy.training.java.audit.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import pe.edu.galaxy.training.java.audit.properties.AuditProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import pe.edu.galaxy.training.java.audit.sanitizer.AuditPayloadSanitizer;
import pe.edu.galaxy.training.java.audit.sanitizer.NoOpAuditPayloadSanitizer;

@AutoConfiguration
@ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
@EnableConfigurationProperties(AuditProperties.class)
@ComponentScan(basePackages = "pe.edu.galaxy.training.java.audit")
public class AuditAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditPayloadSanitizer auditPayloadSanitizer() {
        return new NoOpAuditPayloadSanitizer();
    }
}
