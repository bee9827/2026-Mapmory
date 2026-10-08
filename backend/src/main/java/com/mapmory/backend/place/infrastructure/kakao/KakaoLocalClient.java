package com.mapmory.backend.place.infrastructure.kakao;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.place.application.PlaceErrorCode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.JsonNode;

/** Kakao 로컬 키워드 검색으로 국내 장소를 찾는다. 한글 앞부분만 입력해도 장소명을 찾을 수 있다. */
@Component
public class KakaoLocalClient {

    private static final int SIZE = 10;

    private final RestClient restClient;
    private final String apiKey;

    public KakaoLocalClient(
            RestClient.Builder builder,
            @Value("${kakao.local.base-url:https://dapi.kakao.com}") String baseUrl,
            @Value("${kakao.local.rest-api-key:}") String apiKey
    ) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public List<KakaoPlace> search(String query) {
        if (!isConfigured()) {
            throw new BusinessException(PlaceErrorCode.PLACE_PROVIDER_UNAVAILABLE,
                    "KAKAO_REST_API_KEY가 설정되지 않았습니다.");
        }
        JsonNode body;
        try {
            body = restClient.get()
                    .uri(uri -> uri.path("/v2/local/search/keyword.json")
                            .queryParam("query", query)
                            .queryParam("size", SIZE)
                            .build())
                    .header("Authorization", "KakaoAK " + apiKey)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException exception) {
            throw new BusinessException(PlaceErrorCode.PLACE_PROVIDER_UNAVAILABLE);
        }
        if (body == null || body.isNull()) {
            throw new BusinessException(PlaceErrorCode.PLACE_PROVIDER_UNAVAILABLE);
        }
        List<KakaoPlace> places = new ArrayList<>();
        for (JsonNode document : body.path("documents")) {
            KakaoPlace place = place(document);
            if (place != null) {
                places.add(place);
            }
        }
        return List.copyOf(places);
    }

    private KakaoPlace place(JsonNode document) {
        String id = text(document, "id");
        String name = text(document, "place_name");
        Double longitude = number(document, "x");
        Double latitude = number(document, "y");
        if (id == null || !id.matches("[0-9]{1,20}") || name == null || longitude == null || latitude == null) {
            return null;
        }
        String address = text(document, "road_address_name");
        return new KakaoPlace(id, name, address == null ? text(document, "address_name") : address,
                latitude, longitude);
    }

    private static String text(JsonNode node, String key) {
        JsonNode value = node.path(key);
        return value.isString() && !value.asString().isBlank() ? value.asString() : null;
    }

    private static Double number(JsonNode node, String key) {
        String value = text(node, key);
        if (value == null) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
