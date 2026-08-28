package com.mapmory.backend.common.monitoring;

import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class MetricsConfiguration {

    static final int MAX_HTTP_SERVER_URI_TAG_VALUES = 50;

    @Bean
    MeterFilter httpServerUriCardinalityLimit() {
        return MeterFilter.maximumAllowableTags(
                "http.server.requests",
                "uri",
                MAX_HTTP_SERVER_URI_TAG_VALUES,
                MeterFilter.deny()
        );
    }
}
