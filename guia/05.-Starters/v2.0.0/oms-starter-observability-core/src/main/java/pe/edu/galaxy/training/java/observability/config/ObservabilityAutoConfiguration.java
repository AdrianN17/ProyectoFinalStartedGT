package pe.edu.galaxy.training.java.observability.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import pe.edu.galaxy.training.java.observability.aspect.ObservedMetricAspect;
import pe.edu.galaxy.training.java.observability.health.ObservabilityHealthIndicator;
import pe.edu.galaxy.training.java.observability.properties.ObservabilityProperties;
import pe.edu.galaxy.training.java.observability.service.BusinessMetricsService;
import pe.edu.galaxy.training.java.observability.service.impl.MicrometerBusinessMetricsService;

@AutoConfiguration
@EnableConfigurationProperties(ObservabilityProperties.class)
@ConditionalOnProperty(
        prefix = "oms.observability",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class ObservabilityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(MeterRegistry.class)
    public BusinessMetricsService businessMetricsService(MeterRegistry meterRegistry,
                                                         ObservabilityProperties properties) {
        return new MicrometerBusinessMetricsService(meterRegistry, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(MeterRegistry.class)
    public ObservedMetricAspect observedMetricAspect(MeterRegistry meterRegistry,
                                                     ObservabilityProperties properties) {
        return new ObservedMetricAspect(meterRegistry, properties);
    }

    @Bean("omsObservabilityHealthIndicator")
    @ConditionalOnMissingBean(name = "omsObservabilityHealthIndicator")
    public HealthIndicator observabilityHealthIndicator(ObservabilityProperties properties) {
        return new ObservabilityHealthIndicator(properties);
    }

    @Bean
    public InfoContributor omsInfoContributor(ObservabilityProperties properties) {
        return builder -> builder
                .withDetail("oms-observability", "enabled")
                .withDetail("serviceName", properties.getServiceName());
    }

    @Bean
    @ConditionalOnBean(MeterRegistry.class)
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(
            ObservabilityProperties properties) {

        return registry -> registry.config().commonTags(
                "service", properties.getServiceName(),
                "starter", "oms-starter-observability"
        );
    }
}
