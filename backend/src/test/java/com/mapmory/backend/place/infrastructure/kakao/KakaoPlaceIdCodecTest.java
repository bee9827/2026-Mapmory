package com.mapmory.backend.place.infrastructure.kakao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mapmory.backend.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

class KakaoPlaceIdCodecTest {

    private final KakaoPlaceIdCodec codec = new KakaoPlaceIdCodec("test-signing-secret-0123456789");

    @Test
    void 장소_ID에_담은_이름과_좌표를_그대로_복원한다() {
        String placeId = codec.encode(new KakaoPlace("18619000", "경복궁", "서울 종로구 사직로 161", 37.5796, 126.977));

        assertThat(placeId).startsWith("kakao-").matches("[A-Za-z0-9_-]+");
        assertThat(codec.decode(placeId))
                .isEqualTo(new KakaoPlace("18619000", "경복궁", null, 37.5796, 126.977));
    }

    @Test
    void 서명이_맞지_않으면_거절한다() {
        String placeId = codec.encode(new KakaoPlace("18619000", "경복궁", null, 37.5796, 126.977));
        KakaoPlaceIdCodec otherServer = new KakaoPlaceIdCodec("another-secret-0123456789");

        assertThatThrownBy(() -> otherServer.decode(placeId))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode().code())
                        .isEqualTo("INVALID_PLACE_ID"));
        assertThatThrownBy(() -> codec.decode("kakao-AAAA" + "B".repeat(43)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 긴_이름은_DB_컬럼_길이에_맞게_줄인다() {
        String longName = "아주 긴 장소 이름".repeat(20);

        String placeId = codec.encode(new KakaoPlace("1", longName, null, 37.5, 127.0));

        assertThat(placeId).hasSizeLessThanOrEqualTo(255);
        assertThat(longName).startsWith(codec.decode(placeId).name());
    }
}
