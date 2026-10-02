package pe.edu.galaxy.training.java.observability.health;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import pe.edu.galaxy.training.java.observability.properties.ObservabilityProperties;

public class ObservabilityHealthIndicator implements HealthIndicator {

    private final ObservabilityProperties properties;

    public ObservabilityHealthIndicator(ObservabilityProperties properties) {
        this.properties = properties;
    }

    @Override
    public Health health() {
        if (!properties.isEnabled()) {
            return Health.down()
                    .withDetail("observability", "disabled")
                    .build();
        }

        return Health.up()
                .withDetail("observability", "enabled")
                .withDetail("serviceName", properties.getServiceName())
                .withDetail("businessMetricsEnabled", properties.isBusinessMetricsEnabled())
                .withDetail("methodMetricsEnabled", properties.isMethodMetricsEnabled())
                .build();
    }
}
