package com.mapmory.backend.upload.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mapmory.backend.common.monitoring.MonitoredOperation;
import com.mapmory.backend.common.monitoring.OperationTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

class S3PresignedUrlProviderTest {

    private final S3Presigner s3Presigner = mock(S3Presigner.class);
    private final MeterRegistry meterRegistry = new SimpleMeterRegistry();
    private S3PresignedUrlProvider provider;

    @BeforeEach
    void setUp() {
        provider = new S3PresignedUrlProvider(
                s3Presigner,
                new S3StorageProperties("mapmory-test", "ap-northeast-2"),
                new OperationTimer(meterRegistry)
        );
    }

    @Test
    void Presigned_URL_생성_성공_시간을_기록한다() throws Exception {
        PresignedPutObjectRequest result = mock(PresignedPutObjectRequest.class);
        when(result.url()).thenReturn(URI.create("https://upload.example/object-key").toURL());
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class))).thenReturn(result);

        URI uri = provider.createPresignedPutUrl(
                "object-key",
                "image/jpeg",
                1024L,
                Duration.ofMinutes(5)
        );

        assertThat(uri).isEqualTo(URI.create("https://upload.example/object-key"));
        assertThat(timerCount("SUCCESS")).isEqualTo(1L);
    }

    @Test
    void Presigned_URL_생성_실패_시간을_기록하고_예외를_전파한다() {
        when(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .thenThrow(new IllegalStateException("s3 failure"));

        assertThatThrownBy(() -> provider.createPresignedPutUrl(
                "object-key",
                "image/jpeg",
                1024L,
                Duration.ofMinutes(5)
        )).isInstanceOf(IllegalStateException.class)
                .hasMessage("s3 failure");

        assertThat(timerCount("FAILURE")).isEqualTo(1L);
    }

    private long timerCount(String outcome) {
        return meterRegistry.get(OperationTimer.METRIC_NAME)
                .tag("operation", MonitoredOperation.S3_PRESIGN.name())
                .tag("outcome", outcome)
                .timer()
                .count();
    }
}
