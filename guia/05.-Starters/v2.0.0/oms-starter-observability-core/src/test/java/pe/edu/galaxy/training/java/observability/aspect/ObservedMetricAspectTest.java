package pe.edu.galaxy.training.java.observability.aspect;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.edu.galaxy.training.java.observability.annotation.ObservedMetric;
import pe.edu.galaxy.training.java.observability.properties.ObservabilityProperties;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ObservedMetricAspectTest {

    private SimpleMeterRegistry meterRegistry;
    private ObservedMetricAspect aspect;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        ObservabilityProperties properties = new ObservabilityProperties();
        properties.setServiceName("credit-card-service");
        aspect = new ObservedMetricAspect(meterRegistry, properties);
    }

    @Test
    void observeRegistersCallsAndSuccessCountersAndReturnsResult() throws Throwable {
        ObservedMetric annotation = annotationFor("chargeCard");
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.observe(joinPoint, annotation);

        assertThat(result).isEqualTo("ok");
        assertThat(meterRegistry.get("oms.card.calls").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("oms.card.success").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("oms.card.duration").timer().count()).isEqualTo(1L);
    }

    @Test
    void observeRegistersErrorCounterAndRethrowsExceptionWithExceptionTag() throws Throwable {
        ObservedMetric annotation = annotationFor("chargeCard");
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        RuntimeException failure = new IllegalArgumentException("invalid card");
        when(joinPoint.proceed()).thenThrow(failure);

        assertThatThrownBy(() -> aspect.observe(joinPoint, annotation)).isSameAs(failure);

        assertThat(meterRegistry.get("oms.card.calls").counter().count()).isEqualTo(1.0);
        double errorCount = meterRegistry.get("oms.card.errors")
                .tag("exception", "IllegalArgumentException")
                .counter()
                .count();
        assertThat(errorCount).isEqualTo(1.0);
        // duration timer must still be recorded even when the join point throws.
        assertThat(meterRegistry.get("oms.card.duration").timer().count()).isEqualTo(1L);
    }

    private ObservedMetric annotationFor(String methodName) throws NoSuchMethodException {
        Method method = AnnotatedSample.class.getDeclaredMethod(methodName);
        for (Annotation annotation : method.getAnnotations()) {
            if (annotation instanceof ObservedMetric observedMetric) {
                return observedMetric;
            }
        }
        throw new IllegalStateException("ObservedMetric annotation not found on " + methodName);
    }

    private static class AnnotatedSample {
        @ObservedMetric(name = "oms.card", description = "Charges a credit card", operation = "charge")
        void chargeCard() {
        }
    }
}
