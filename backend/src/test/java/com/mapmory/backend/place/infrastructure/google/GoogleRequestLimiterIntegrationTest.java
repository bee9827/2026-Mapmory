package com.mapmory.backend.place.infrastructure.google;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mapmory.backend.IntegrationTest;
import com.mapmory.backend.common.exception.BusinessException;
import io.github.bucket4j.mysql.MySQLSelectForUpdateBasedProxyManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GoogleRequestLimiterIntegrationTest extends IntegrationTest {

    @Autowired
    MySQLSelectForUpdateBasedProxyManager<String> bucketManager;

    @Test
    void 자동완성과_상세_조회의_하루_한도를_DB에_따로_센다() {
        GoogleRequestLimiter first = new GoogleRequestLimiter(bucketManager, 2, 1, 10_000);

        first.reserveAutocomplete();
        first.reserveDetails();

        GoogleRequestLimiter second = new GoogleRequestLimiter(bucketManager, 2, 1, 10_000);
        second.reserveAutocomplete();
        assertError(second::reserveAutocomplete, "PLACE_SEARCH_BUDGET_EXHAUSTED");
        assertError(second::reserveDetails, "PLACE_PROVIDER_BUDGET_EXHAUSTED");
    }

    private void assertError(Runnable action, String expectedCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode().code())
                        .isEqualTo(expectedCode));
    }
}
