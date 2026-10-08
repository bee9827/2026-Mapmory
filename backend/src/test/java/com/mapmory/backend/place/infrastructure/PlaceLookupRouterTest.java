package com.mapmory.backend.place.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.place.application.PlaceErrorCode;
import com.mapmory.backend.place.application.model.PlaceCandidate;
import com.mapmory.backend.place.application.model.PlaceDetails;
import com.mapmory.backend.place.infrastructure.geoapify.GeoapifyClient;
import com.mapmory.backend.place.infrastructure.kakao.KakaoLocalClient;
import com.mapmory.backend.place.infrastructure.kakao.KakaoPlace;
import com.mapmory.backend.place.infrastructure.kakao.KakaoPlaceIdCodec;
import java.util.List;
import org.junit.jupiter.api.Test;

class PlaceLookupRouterTest {

    private final GeoapifyClient geoapify = mock(GeoapifyClient.class);
    private final KakaoLocalClient kakao = mock(KakaoLocalClient.class);
    private final KakaoPlaceIdCodec codec = new KakaoPlaceIdCodec("test-signing-secret-0123456789");
    private final PlaceLookupRouter router = new PlaceLookupRouter(geoapify, kakao, codec);

    private static final PlaceCandidate MACAU = new PlaceCandidate("mo-1", "마카오", "마카오, 중국", null, null);

    @Test
    void 한글_검색어는_해외_후보를_먼저_두고_Kakao_국내_후보를_붙인다() {
        when(kakao.isConfigured()).thenReturn(true);
        when(kakao.search("경복")).thenReturn(List.of(new KakaoPlace("1", "경복궁", "서울 종로구", 37.5796, 126.977)));
        when(geoapify.searchExcludingCountry("경복", "KR")).thenReturn(List.of(MACAU));

        List<PlaceCandidate> result = router.search("경복");

        assertThat(result).extracting(PlaceCandidate::name).containsExactly("마카오", "경복궁");
        assertThat(result.get(1).placeId()).startsWith("kakao-");
        assertThat(result.get(1).attribution()).isEqualTo("© Kakao");
    }

    @Test
    void 영문_검색어나_Kakao_키가_없으면_Geoapify만_쓴다() {
        when(kakao.isConfigured()).thenReturn(true);
        when(geoapify.search("Tumon")).thenReturn(List.of(MACAU));

        assertThat(router.search("Tumon")).containsExactly(MACAU);
        verify(kakao, never()).search(anyString());

        when(kakao.isConfigured()).thenReturn(false);
        when(geoapify.search("경복")).thenReturn(List.of());
        assertThat(router.search("경복")).isEmpty();
    }

    @Test
    void Kakao가_실패하거나_결과가_없으면_기존_Geoapify_검색을_쓴다() {
        when(kakao.isConfigured()).thenReturn(true);
        when(kakao.search("괌")).thenThrow(new BusinessException(PlaceErrorCode.PLACE_PROVIDER_UNAVAILABLE));
        when(geoapify.search("괌")).thenReturn(List.of(MACAU));

        assertThat(router.search("괌")).containsExactly(MACAU);
    }

    @Test
    void Geoapify_해외_검색이_실패해도_국내_결과는_돌려준다() {
        when(kakao.isConfigured()).thenReturn(true);
        when(kakao.search("경복")).thenReturn(List.of(new KakaoPlace("1", "경복궁", null, 37.5796, 126.977)));
        when(geoapify.searchExcludingCountry("경복", "KR"))
                .thenThrow(new BusinessException(PlaceErrorCode.PLACE_SEARCH_BUDGET_EXHAUSTED));

        assertThat(router.search("경복")).extracting(PlaceCandidate::name).containsExactly("경복궁");
    }

    @Test
    void Kakao_장소_ID는_다시_호출하지_않고_한국_장소로_복원한다() {
        String placeId = codec.encode(new KakaoPlace("1", "경복궁", null, 37.5796, 126.977));

        assertThat(router.findById(placeId)).isEqualTo(new PlaceDetails(
                placeId, "경복궁", "KR", 37.5796, 126.977, "© Kakao", "https://map.kakao.com"));
        assertThat(router.providerCode(placeId)).isEqualTo("KAKAO");
        verify(geoapify, never()).findById(anyString());
    }

    @Test
    void 그_밖의_장소_ID는_Geoapify로_조회한다() {
        when(geoapify.providerCode("park-1")).thenReturn("GEOAPIFY");

        router.findById("park-1");

        verify(geoapify).findById("park-1");
        assertThat(router.providerCode("park-1")).isEqualTo("GEOAPIFY");
    }
}
