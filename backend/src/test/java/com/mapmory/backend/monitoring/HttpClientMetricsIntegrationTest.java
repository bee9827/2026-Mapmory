package com.mapmory.backend.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.mapmory.backend.IntegrationTest;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class HttpClientMetricsIntegrationTest extends IntegrationTest {

    private static final String TEST_URI = "https://example.test/users/1";

    @Autowired
    private RestClient.Builder restClientBuilder;

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    void 자동_구성된_RestClient는_외부_HTTP_호출_메트릭을_기록한다() {
        MockRestServiceServer server = MockRestServiceServer.bindTo(restClientBuilder).build();
        RestClient restClient = restClientBuilder.build();
        server.expect(requestTo(TEST_URI))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        restClient.get()
                .uri(TEST_URI)
                .retrieve()
                .toBodilessEntity();

        server.verify();

        Collection<Timer> timers = meterRegistry.find("http.client.requests").timers();
        assertThat(timers)
                .anySatisfy(timer -> {
                    assertThat(timer.count()).isPositive();
                    assertThat(timer.getId().getTag("service")).isEqualTo("mapmory-backend");
                    assertThat(timer.getId().getTag("environment")).isEqualTo("local");
                });
    }
}
