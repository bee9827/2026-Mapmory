package com.mapmory.backend.place.infrastructure.google;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.place.application.PlaceErrorCode;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.BucketExceptions.BucketExecutionException;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import java.time.YearMonth;
import java.time.ZoneOffset;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Google 호출 직전에 한도를 확인한다. Google은 월 무료 사용량을 넘으면 과금되므로 Geoapify 한도와 따로 센다.
 * 회원별 검색·선택 한도는 기존 {@code PlaceRateLimitPort}가 그대로 적용한다.
 *
 * <p>버킷은 {@link GoogleRateLimitConfig}가 만든다. 분당 한도는 서버 메모리에서, 월 한도는 DB에서 센다.
 * 그래서 분당 한도에 걸린 요청은 커넥션 없이 바로 거절된다.
 */
@Component
public class GoogleRequestLimiter {

    private static final String AUTOCOMPLETE_BUDGET = "google:autocomplete:";
    private static final String DETAILS_BUDGET = "google:details:";

    private final ProxyManager<String> bucketManager;
    private final Bucket requestPace;
    private final BucketConfiguration autocompleteBudget;
    private final BucketConfiguration detailsBudget;

    public GoogleRequestLimiter(
            ProxyManager<String> bucketManager,
            @Qualifier(GoogleRateLimitConfig.REQUEST_PACE) Bucket requestPace,
            @Qualifier(GoogleRateLimitConfig.AUTOCOMPLETE_BUDGET) BucketConfiguration autocompleteBudget,
            @Qualifier(GoogleRateLimitConfig.DETAILS_BUDGET) BucketConfiguration detailsBudget
    ) {
        this.bucketManager = bucketManager;
        this.requestPace = requestPace;
        this.autocompleteBudget = autocompleteBudget;
        this.detailsBudget = detailsBudget;
    }

    // 분당 한도를 먼저 확인한다. 거절된 요청이 DB에 가지 않고 월 한도도 쓰지 않게 하기 위해서다.
    public void reserveAutocomplete() {
        reservePace();
        consume(AUTOCOMPLETE_BUDGET + thisMonth(), autocompleteBudget, PlaceErrorCode.PLACE_SEARCH_BUDGET_EXHAUSTED);
    }

    public void reserveDetails() {
        reservePace();
        consume(DETAILS_BUDGET + thisMonth(), detailsBudget, PlaceErrorCode.PLACE_PROVIDER_BUDGET_EXHAUSTED);
    }

    private void reservePace() {
        if (!requestPace.tryConsume(1)) {
            throw new BusinessException(PlaceErrorCode.PLACE_RATE_LIMITED);
        }
    }

    private void consume(String key, BucketConfiguration configuration, PlaceErrorCode exhaustedError) {
        try {
            if (!bucketManager.getProxy(key, () -> configuration).tryConsume(1)) {
                throw new BusinessException(exhaustedError);
            }
        } catch (BucketExecutionException exception) {
            // 한도 저장소가 장애일 때 외부 API를 호출하면 사용량을 통제할 수 없다.
            throw new BusinessException(PlaceErrorCode.PLACE_PROVIDER_UNAVAILABLE);
        }
    }

    // Google 월 사용량은 태평양 시간 기준이라 UTC 월과 몇 시간 어긋난다. 월 한도를 무료 사용량보다 작게 둔 이유다.
    private static String thisMonth() {
        return YearMonth.now(ZoneOffset.UTC).toString();
    }
}
