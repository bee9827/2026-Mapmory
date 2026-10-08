package com.mapmory.backend.place.infrastructure.google;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.place.application.PlaceErrorCode;
import com.mapmory.backend.place.application.model.PlaceCandidate;
import com.mapmory.backend.place.application.model.PlaceDetails;
import com.mapmory.backend.place.infrastructure.geoapify.GeoapifyRequestLimiter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;

/**
 * Google Places API (New)의 Autocomplete와 Place Details를 호출한다.
 *
 * <p>검색은 사진을 찾기 위한 도구다. Google이 준 이름과 좌표는 응답과 지역 추천에만 쓰고 저장하지 않는다.
 * 저장용 조회({@link #findById})는 Essentials 필드만 요청해 이름 없이 국가와 좌표만 받는다.
 */
@Component
public class GooglePlacesClient {

    static final String ATTRIBUTION = "Google Maps";
    static final String ATTRIBUTION_URL = "https://www.google.com/maps";

    // Place Details Essentials: 좌표와 국가만. displayName을 넣으면 Pro SKU로 과금된다.
    private static final String ESSENTIALS_FIELDS = "id,location,addressComponents";
    private static final String SELECTION_FIELDS = ESSENTIALS_FIELDS + ",displayName";
    private static final String LANGUAGE = "ko";

    private final RestClient restClient;
    private final String apiKey;
    private final GeoapifyRequestLimiter requestLimiter;
    private final GooglePlaceMapper mapper = new GooglePlaceMapper();

    public GooglePlacesClient(
            RestClient.Builder builder,
            @Value("${google.places.base-url:https://places.googleapis.com}") String baseUrl,
            @Value("${google.places.api-key:}") String apiKey,
            GeoapifyRequestLimiter requestLimiter
    ) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.requestLimiter = requestLimiter;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public List<PlaceCandidate> search(String query, String sessionToken) {
        requireConfigured();
        requestLimiter.reserveSearch();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("input", query);
        body.put("languageCode", LANGUAGE);
        if (sessionToken != null) {
            body.put("sessionToken", sessionToken);
        }
        JsonNode response = call(() -> restClient.post()
                .uri("/v1/places:autocomplete")
                .header("X-Goog-Api-Key", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class));
        return mapper.candidates(response);
    }

    public PlaceDetails findForSelection(String placeId, String sessionToken) {
        return details(placeId, SELECTION_FIELDS, sessionToken);
    }

    public PlaceDetails findById(String placeId) {
        return details(placeId, ESSENTIALS_FIELDS, null);
    }

    private PlaceDetails details(String placeId, String fieldMask, String sessionToken) {
        if (placeId == null || !placeId.matches("[A-Za-z0-9_-]{1,255}")) {
            throw new BusinessException(PlaceErrorCode.INVALID_PLACE_ID);
        }
        requireConfigured();
        requestLimiter.reserveDetails();
        JsonNode response = call(() -> restClient.get()
                .uri(uri -> {
                    uri.path("/v1/places/{placeId}").queryParam("languageCode", LANGUAGE);
                    if (sessionToken != null) {
                        uri.queryParam("sessionToken", sessionToken);
                    }
                    return uri.build(placeId);
                })
                .header("X-Goog-Api-Key", apiKey)
                .header("X-Goog-FieldMask", fieldMask)
                .retrieve()
                .body(JsonNode.class));
        return mapper.details(response, placeId)
                .orElseThrow(() -> new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND));
    }

    private static JsonNode call(Supplier<JsonNode> request) {
        try {
            JsonNode body = request.get();
            if (body == null || body.isNull()) {
                throw new BusinessException(PlaceErrorCode.PLACE_PROVIDER_UNAVAILABLE);
            }
            return body;
        } catch (RestClientResponseException exception) {
            int status = exception.getStatusCode().value();
            if (status == 400 || status == 404) {
                throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
            }
            throw new BusinessException(PlaceErrorCode.PLACE_PROVIDER_UNAVAILABLE);
        } catch (RestClientException exception) {
            throw new BusinessException(PlaceErrorCode.PLACE_PROVIDER_UNAVAILABLE);
        }
    }

    private void requireConfigured() {
        if (!isConfigured()) {
            throw new BusinessException(PlaceErrorCode.PLACE_PROVIDER_UNAVAILABLE,
                    "GOOGLE_PLACES_API_KEY가 설정되지 않았습니다.");
        }
    }
}
