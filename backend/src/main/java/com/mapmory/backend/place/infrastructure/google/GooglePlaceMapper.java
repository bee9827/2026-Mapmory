package com.mapmory.backend.place.infrastructure.google;

import com.mapmory.backend.place.application.model.PlaceCandidate;
import com.mapmory.backend.place.application.model.PlaceDetails;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import tools.jackson.databind.JsonNode;

class GooglePlaceMapper {

    /*
     * Google은 괌·홍콩 등을 별도 국가 코드로 준다. 앱 지역 목록에는 아직 없어서, 영토 분류를
     * 한 번에 정리하기 전까지 기존 Geoapify와 같은 소속 국가로 맞춘다.
     */
    private static final Map<String, String> TERRITORY_TO_COUNTRY = Map.of(
            "GU", "US", "MP", "US", "AS", "US", "PR", "US", "VI", "US",
            "HK", "CN", "MO", "CN");

    List<PlaceCandidate> candidates(JsonNode response) {
        List<PlaceCandidate> candidates = new ArrayList<>();
        for (JsonNode suggestion : response.path("suggestions")) {
            JsonNode prediction = suggestion.path("placePrediction");
            String placeId = text(prediction.path("placeId"));
            String name = text(prediction.path("structuredFormat").path("mainText").path("text"));
            if (name == null) {
                name = text(prediction.path("text").path("text"));
            }
            if (placeId == null || name == null) {
                continue;
            }
            String address = text(prediction.path("structuredFormat").path("secondaryText").path("text"));
            candidates.add(new PlaceCandidate(placeId, name, address,
                    GooglePlacesClient.ATTRIBUTION, GooglePlacesClient.ATTRIBUTION_URL));
        }
        return List.copyOf(candidates);
    }

    Optional<PlaceDetails> details(JsonNode response, String placeId) {
        JsonNode location = response.path("location");
        if (!location.path("latitude").isNumber() || !location.path("longitude").isNumber()) {
            return Optional.empty();
        }
        return Optional.of(new PlaceDetails(
                placeId,
                text(response.path("displayName").path("text")),
                countryCode(response.path("addressComponents")),
                location.path("latitude").asDouble(),
                location.path("longitude").asDouble(),
                GooglePlacesClient.ATTRIBUTION,
                GooglePlacesClient.ATTRIBUTION_URL));
    }

    private static String countryCode(JsonNode components) {
        for (JsonNode component : components) {
            for (JsonNode type : component.path("types")) {
                if ("country".equals(type.asString())) {
                    String code = text(component.path("shortText"));
                    if (code == null) {
                        return null;
                    }
                    String upper = code.toUpperCase(Locale.ROOT);
                    return TERRITORY_TO_COUNTRY.getOrDefault(upper, upper);
                }
            }
        }
        return null;
    }

    private static String text(JsonNode node) {
        if (node == null || !node.isString()) {
            return null;
        }
        String value = node.asString().strip();
        return value.isEmpty() ? null : value;
    }
}
