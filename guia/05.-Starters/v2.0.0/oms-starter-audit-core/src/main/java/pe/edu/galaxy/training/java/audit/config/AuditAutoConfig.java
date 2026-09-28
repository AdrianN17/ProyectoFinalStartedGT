package pe.edu.galaxy.training.java.audit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import pe.edu.galaxy.training.java.audit.properties.AuditProperties;
import pe.edu.galaxy.training.java.audit.sanitizer.AuditPayloadSanitizer;
import pe.edu.galaxy.training.java.audit.sanitizer.NoOpAuditPayloadSanitizer;

@AutoConfiguration
@ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
@EnableConfigurationProperties(AuditProperties.class)
@ComponentScan(basePackages = "pe.edu.galaxy.training.java.audit")
public class AuditAutoConfig {

    @Bean("auditObjectMapper")
    public ObjectMapper auditObjectMapper() {
        return JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }
    @Bean
    @ConditionalOnMissingBean
    public AuditPayloadSanitizer auditPayloadSanitizer() {
        return new NoOpAuditPayloadSanitizer();
    }
}
