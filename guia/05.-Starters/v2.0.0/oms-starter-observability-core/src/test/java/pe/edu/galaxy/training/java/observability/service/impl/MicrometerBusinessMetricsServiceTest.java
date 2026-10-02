package pe.edu.galaxy.training.java.observability.service.impl;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.edu.galaxy.training.java.observability.properties.ObservabilityProperties;

import static org.assertj.core.api.Assertions.assertThat;

class MicrometerBusinessMetricsServiceTest {

    private SimpleMeterRegistry meterRegistry;
    private MicrometerBusinessMetricsService metricsService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        ObservabilityProperties properties = new ObservabilityProperties();
        properties.setServiceName("credit-card-service");
        metricsService = new MicrometerBusinessMetricsService(meterRegistry, properties);
    }

    @Test
    void incrementCreatedRegistersCounterWithEntityAndOperationTags() {
        metricsService.incrementCreated("CreditCard");

        double count = meterRegistry.get("oms.business.created.total")
                .tag("service", "credit-card-service")
                .tag("entity", "CreditCard")
                .tag("operation", "created")
                .counter()
                .count();

        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void incrementUpdatedRegistersCounter() {
        metricsService.incrementUpdated("CreditCard");

        double count = meterRegistry.get("oms.business.updated.total")
                .tag("operation", "updated")
                .counter()
                .count();

        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void incrementDeletedRegistersCounter() {
        metricsService.incrementDeleted("CreditCard");

        double count = meterRegistry.get("oms.business.deleted.total")
                .tag("operation", "deleted")
                .counter()
                .count();

        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void incrementErrorRegistersCounterWithCustomOperation() {
        metricsService.incrementError("CreditCard", "charge");

        double count = meterRegistry.get("oms.business.errors.total")
                .tag("entity", "CreditCard")
                .tag("operation", "charge")
                .counter()
                .count();

        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void incrementCreatedAccumulatesMultipleCalls() {
        metricsService.incrementCreated("CreditCard");
        metricsService.incrementCreated("CreditCard");
        metricsService.incrementCreated("CreditCard");

        double count = meterRegistry.get("oms.business.created.total")
                .tag("entity", "CreditCard")
                .counter()
                .count();

        assertThat(count).isEqualTo(3.0);
    }

    @Test
    void recordExecutionTimeRegistersTimerWithElapsedDuration() {
        metricsService.recordExecutionTime("chargeCard", 150L);

        var timer = meterRegistry.get("oms.business.execution.time")
                .tag("service", "credit-card-service")
                .tag("operation", "chargeCard")
                .timer();

        assertThat(timer.count()).isEqualTo(1L);
        assertThat(timer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS)).isEqualTo(150.0);
    }
}
