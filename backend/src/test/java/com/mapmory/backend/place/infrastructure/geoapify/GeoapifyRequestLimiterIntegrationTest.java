package com.mapmory.backend.place.infrastructure.geoapify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mapmory.backend.IntegrationTest;
import com.mapmory.backend.common.exception.BusinessException;
import io.github.bucket4j.mysql.MySQLSelectForUpdateBasedProxyManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GeoapifyRequestLimiterIntegrationTest extends IntegrationTest {

    @Autowired
    MySQLSelectForUpdateBasedProxyManager<String> bucketManager;

    @Test
    void 회원_검색_한도와_전체_한도는_DB에_저장되고_상세_조회_몫은_남긴다() {
        GeoapifyRateLimitProperties limits = new GeoapifyRateLimitProperties(2, 2, 3, 10_000);
        GeoapifyRequestLimiter first = new GeoapifyRequestLimiter(bucketManager, limits);

        first.checkSearch(1L);
        first.checkSearch(1L);
        assertError(() -> first.checkSearch(1L), "PLACE_RATE_LIMITED");
        first.checkSearch(2L);

        first.reserveSearch();
        first.reserveSearch();

        GeoapifyRequestLimiter second = new GeoapifyRequestLimiter(bucketManager, limits);
        assertError(second::reserveSearch, "PLACE_SEARCH_BUDGET_EXHAUSTED");
        second.reserveDetails();
        assertError(second::reserveDetails, "PLACE_PROVIDER_BUDGET_EXHAUSTED");
    }

    private void assertError(Runnable action, String expectedCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode().code())
                        .isEqualTo(expectedCode));
    }
}
