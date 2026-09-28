package pe.edu.galaxy.training.java.observability.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oms.observability")
public class ObservabilityProperties {

    private boolean enabled = true;
    private String serviceName = "unknown-service";
    private boolean businessMetricsEnabled = true;
    private boolean methodMetricsEnabled = true;
    private boolean healthEnabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public boolean isBusinessMetricsEnabled() {
        return businessMetricsEnabled;
    }

    public void setBusinessMetricsEnabled(boolean businessMetricsEnabled) {
        this.businessMetricsEnabled = businessMetricsEnabled;
    }

    public boolean isMethodMetricsEnabled() {
        return methodMetricsEnabled;
    }

    public void setMethodMetricsEnabled(boolean methodMetricsEnabled) {
        this.methodMetricsEnabled = methodMetricsEnabled;
    }

    public boolean isHealthEnabled() {
        return healthEnabled;
    }

    public void setHealthEnabled(boolean healthEnabled) {
        this.healthEnabled = healthEnabled;
    }
}
