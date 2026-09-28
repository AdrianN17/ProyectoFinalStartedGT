package pe.edu.galaxy.training.java.observability.service.impl;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import pe.edu.galaxy.training.java.observability.properties.ObservabilityProperties;
import pe.edu.galaxy.training.java.observability.service.BusinessMetricsService;

import java.time.Duration;

public class MicrometerBusinessMetricsService implements BusinessMetricsService {

    private final MeterRegistry meterRegistry;
    private final ObservabilityProperties properties;

    public MicrometerBusinessMetricsService(MeterRegistry meterRegistry,
                                            ObservabilityProperties properties) {
        this.meterRegistry = meterRegistry;
        this.properties = properties;
    }

    @Override
    public void incrementCreated(String entity) {
        counter("oms.business.created.total", entity, "created").increment();
    }

    @Override
    public void incrementUpdated(String entity) {
        counter("oms.business.updated.total", entity, "updated").increment();
    }

    @Override
    public void incrementDeleted(String entity) {
        counter("oms.business.deleted.total", entity, "deleted").increment();
    }

    @Override
    public void incrementError(String entity, String operation) {
        counter("oms.business.errors.total", entity, operation).increment();
    }

    @Override
    public void recordExecutionTime(String operation, long milliseconds) {
        Timer.builder("oms.business.execution.time")
                .description("Business operation execution time")
                .tag("service", properties.getServiceName())
                .tag("operation", operation)
                .register(meterRegistry)
                .record(Duration.ofMillis(milliseconds));
    }

    private Counter counter(String name, String entity, String operation) {
        return Counter.builder(name)
                .tag("service", properties.getServiceName())
                .tag("entity", entity)
                .tag("operation", operation)
                .register(meterRegistry);
    }
}
