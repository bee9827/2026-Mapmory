package com.mapmory.backend.common.monitoring;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

class MetricsConfigurationTest {

    @Test
    void HTTP_응답_시간은_제한된_SLO_버킷으로_수집한다() throws IOException {
        List<PropertySource<?>> sources = new YamlPropertySourceLoader().load(
                "application",
                new ClassPathResource("application.yaml")
        );

        assertThat(property(sources, "management.metrics.distribution.percentiles-histogram.http.server.requests"))
                .isNull();
        assertThat(property(sources, "management.metrics.distribution.slo.http.server.requests"))
                .isEqualTo("100ms,300ms,500ms,1s,2s,3s,5s");
    }

    @Test
    void HTTP_URI_태그는_허용된_개수까지만_등록한다() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        registry.config().meterFilter(new MetricsConfiguration().httpServerUriCardinalityLimit());

        for (int index = 0; index <= MetricsConfiguration.MAX_HTTP_SERVER_URI_TAG_VALUES; index++) {
            Timer.builder("http.server.requests")
                    .tag("uri", "/test/" + index)
                    .register(registry);
        }

        assertThat(registry.find("http.server.requests").timers())
                .hasSize(MetricsConfiguration.MAX_HTTP_SERVER_URI_TAG_VALUES);
    }

    private static Object property(List<PropertySource<?>> sources, String name) {
        return sources.stream()
                .map(source -> source.getProperty(name))
                .filter(value -> value != null)
                .findFirst()
                .orElse(null);
    }
}
