package pe.edu.galaxy.training.java.observability.aspect;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import pe.edu.galaxy.training.java.observability.annotation.ObservedMetric;
import pe.edu.galaxy.training.java.observability.properties.ObservabilityProperties;

import java.time.Duration;

@Aspect
public class ObservedMetricAspect {

    private final MeterRegistry meterRegistry;
    private final ObservabilityProperties properties;

    public ObservedMetricAspect(MeterRegistry meterRegistry,
                                ObservabilityProperties properties) {
        this.meterRegistry = meterRegistry;
        this.properties = properties;
    }

    @Around("@annotation(observedMetric)")
    public Object observe(ProceedingJoinPoint joinPoint,
                          ObservedMetric observedMetric) throws Throwable {
        long start = System.currentTimeMillis();

        Counter.builder(observedMetric.name() + ".calls")
                .description(observedMetric.description())
                .tag("service", properties.getServiceName())
                .tag("operation", observedMetric.operation())
                .register(meterRegistry)
                .increment();

        try {
            Object result = joinPoint.proceed();

            Counter.builder(observedMetric.name() + ".success")
                    .tag("service", properties.getServiceName())
                    .tag("operation", observedMetric.operation())
                    .register(meterRegistry)
                    .increment();

            return result;
        } catch (Throwable ex) {
            Counter.builder(observedMetric.name() + ".errors")
                    .tag("service", properties.getServiceName())
                    .tag("operation", observedMetric.operation())
                    .tag("exception", ex.getClass().getSimpleName())
                    .register(meterRegistry)
                    .increment();

            throw ex;
        } finally {
            long duration = System.currentTimeMillis() - start;

            Timer.builder(observedMetric.name() + ".duration")
                    .description("Observed method duration")
                    .tag("service", properties.getServiceName())
                    .tag("operation", observedMetric.operation())
                    .register(meterRegistry)
                    .record(Duration.ofMillis(duration));
        }
    }
}
