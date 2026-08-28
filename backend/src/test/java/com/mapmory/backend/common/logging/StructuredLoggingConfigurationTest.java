package com.mapmory.backend.common.logging;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

class StructuredLoggingConfigurationTest {

    private final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

    @Test
    void 운영_환경은_Logstash_JSON_로그를_사용한다() throws IOException {
        List<PropertySource<?>> sources = loader.load(
                "application-prod",
                new ClassPathResource("application-prod.yaml")
        );

        assertThat(property(sources, "logging.structured.format.console")).isEqualTo("logstash");
        assertThat(property(sources, "logging.structured.json.add.service"))
                .isEqualTo("${spring.application.name}");
        assertThat(property(sources, "logging.structured.json.add.environment")).isEqualTo("prod");
        assertThat(property(sources, "logging.structured.json.add.version"))
                .isEqualTo("${APP_VERSION:unknown}");
    }

    @Test
    void 로컬_환경은_구조화_로그를_활성화하지_않는다() throws IOException {
        List<PropertySource<?>> sources = loader.load(
                "application-local",
                new ClassPathResource("application-local.yaml")
        );

        assertThat(property(sources, "logging.structured.format.console")).isNull();
    }

    private static Object property(List<PropertySource<?>> sources, String name) {
        return sources.stream()
                .map(source -> source.getProperty(name))
                .filter(value -> value != null)
                .findFirst()
                .orElse(null);
    }
}
