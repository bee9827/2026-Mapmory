package com.mapmory.backend.common.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

class OperationTimerTest {

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final OperationTimer operationTimer = new OperationTimer(meterRegistry);

    @Test
    void 성공한_작업의_시간을_기록한다() {
        String result = operationTimer.record(MonitoredOperation.MEDIA_SYNC, () -> "result");

        assertThat(result).isEqualTo("result");
        assertThat(timer(MonitoredOperation.MEDIA_SYNC, "SUCCESS").count()).isEqualTo(1L);
    }

    @Test
    void 실패한_작업의_시간을_기록하고_예외를_다시_던진다() {
        assertThatThrownBy(() -> operationTimer.record(MonitoredOperation.S3_PRESIGN, () -> {
            throw new IllegalStateException("presign failed");
        })).isInstanceOf(IllegalStateException.class)
                .hasMessage("presign failed");

        assertThat(timer(MonitoredOperation.S3_PRESIGN, "FAILURE").count()).isEqualTo(1L);
    }

    private Timer timer(MonitoredOperation operation, String outcome) {
        return meterRegistry.get(OperationTimer.METRIC_NAME)
                .tag("operation", operation.name())
                .tag("outcome", outcome)
                .timer();
    }
}
