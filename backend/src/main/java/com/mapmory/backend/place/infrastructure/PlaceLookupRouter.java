package com.mapmory.backend.place.infrastructure;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.place.application.model.PlaceCandidate;
import com.mapmory.backend.place.application.model.PlaceDetails;
import com.mapmory.backend.place.application.port.PlaceLookupPort;
import com.mapmory.backend.place.infrastructure.geoapify.GeoapifyClient;
import com.mapmory.backend.place.infrastructure.kakao.KakaoLocalClient;
import com.mapmory.backend.place.infrastructure.kakao.KakaoPlace;
import com.mapmory.backend.place.infrastructure.kakao.KakaoPlaceIdCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * 한글 검색어는 국내 장소를 Kakao에서, 해외 장소를 Geoapify에서 찾아 합친다.
 * Geoapify 자동완성은 한글 앞부분("경복")으로 장소명("경복궁")을 찾지 못하기 때문이다.
 * 그 밖의 검색어와 Kakao 키가 없을 때는 Geoapify만 사용한다.
 */
@Primary
@Component
public class PlaceLookupRouter implements PlaceLookupPort {

    static final String KAKAO_PROVIDER_CODE = "KAKAO";
    static final String KAKAO_ATTRIBUTION = "© Kakao";
    static final String KAKAO_ATTRIBUTION_URL = "https://map.kakao.com";

    private static final Pattern HANGUL = Pattern.compile("[\\uAC00-\\uD7A3\\u3131-\\u318E]");
    private static final int MAX_CANDIDATES = 10;
    private static final int MAX_OVERSEAS_CANDIDATES = 3;
    private static final String KOREA = "KR";

    private final GeoapifyClient geoapify;
    private final KakaoLocalClient kakao;
    private final KakaoPlaceIdCodec kakaoPlaceIds;

    public PlaceLookupRouter(GeoapifyClient geoapify, KakaoLocalClient kakao, KakaoPlaceIdCodec kakaoPlaceIds) {
        this.geoapify = geoapify;
        this.kakao = kakao;
        this.kakaoPlaceIds = kakaoPlaceIds;
    }

    @Override
    public String providerCode(String placeId) {
        return KakaoPlaceIdCodec.isKakaoPlaceId(placeId) ? KAKAO_PROVIDER_CODE : geoapify.providerCode(placeId);
    }

    @Override
    public List<PlaceCandidate> search(String query) {
        if (!kakao.isConfigured() || !HANGUL.matcher(query).find()) {
            return geoapify.search(query);
        }
        List<PlaceCandidate> domestic = kakaoCandidates(query);
        if (domestic.isEmpty()) {
            return geoapify.search(query);
        }
        List<PlaceCandidate> overseas = overseasCandidates(query);
        // 해외 후보(예: "마카오")를 먼저 두고, 같은 이름의 국내 상호가 목록을 채운다.
        List<PlaceCandidate> merged = new ArrayList<>(overseas);
        domestic.stream().limit(MAX_CANDIDATES - overseas.size()).forEach(merged::add);
        return List.copyOf(merged);
    }

    @Override
    public PlaceDetails findById(String placeId) {
        if (!KakaoPlaceIdCodec.isKakaoPlaceId(placeId)) {
            return geoapify.findById(placeId);
        }
        KakaoPlace place = kakaoPlaceIds.decode(placeId);
        return new PlaceDetails(placeId, place.name(), KOREA, place.latitude(), place.longitude(),
                KAKAO_ATTRIBUTION, KAKAO_ATTRIBUTION_URL);
    }

    private List<PlaceCandidate> kakaoCandidates(String query) {
        try {
            return kakao.search(query).stream()
                    .map(place -> new PlaceCandidate(kakaoPlaceIds.encode(place), place.name(), place.address(),
                            KAKAO_ATTRIBUTION, KAKAO_ATTRIBUTION_URL))
                    .toList();
        } catch (BusinessException exception) {
            // Kakao 장애 시 기존 Geoapify 검색으로 대신한다.
            return List.of();
        }
    }

    private List<PlaceCandidate> overseasCandidates(String query) {
        try {
            return geoapify.searchExcludingCountry(query, KOREA).stream()
                    .limit(MAX_OVERSEAS_CANDIDATES)
                    .toList();
        } catch (BusinessException exception) {
            // 국내 결과는 이미 있으므로 Geoapify 한도 초과나 장애로 검색 전체를 실패시키지 않는다.
            return List.of();
        }
    }
}
