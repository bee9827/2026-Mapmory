package com.mapmory.backend.common.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Objects;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class OperationTimer {

    public static final String METRIC_NAME = "mapmory.operation.duration";

    private final MeterRegistry meterRegistry;

    public OperationTimer(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public <T> T record(MonitoredOperation operation, Supplier<T> action) {
        Objects.requireNonNull(operation, "operation must not be null");
        Objects.requireNonNull(action, "action must not be null");

        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            T result = action.get();
            sample.stop(timer(operation, Outcome.SUCCESS));
            return result;
        } catch (RuntimeException | Error exception) {
            sample.stop(timer(operation, Outcome.FAILURE));
            throw exception;
        }
    }

    private Timer timer(MonitoredOperation operation, Outcome outcome) {
        return Timer.builder(METRIC_NAME)
                .description("Duration of important Mapmory internal operations")
                .tag("operation", operation.name())
                .tag("outcome", outcome.name())
                .register(meterRegistry);
    }

    private enum Outcome {
        SUCCESS,
        FAILURE
    }
}
