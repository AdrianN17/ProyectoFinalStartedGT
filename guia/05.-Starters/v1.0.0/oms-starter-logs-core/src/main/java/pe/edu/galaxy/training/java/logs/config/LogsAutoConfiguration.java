package pe.edu.galaxy.training.java.logs.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import pe.edu.galaxy.training.java.logs.aspect.OperationLoggingAspect;
import pe.edu.galaxy.training.java.logs.filter.TraceFilter;
import pe.edu.galaxy.training.java.logs.properties.LogsProperties;

@AutoConfiguration
@EnableConfigurationProperties(LogsProperties.class)
@ConditionalOnProperty(prefix = "oms.logs", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LogsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TraceFilter traceFilter(LogsProperties properties) {
        return new TraceFilter(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public OperationLoggingAspect operationLoggingAspect() {
        return new OperationLoggingAspect();
    }
}
