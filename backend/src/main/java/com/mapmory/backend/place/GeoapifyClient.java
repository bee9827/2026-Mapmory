package com.mapmory.backend.place;

import tools.jackson.databind.JsonNode;
import com.mapmory.backend.common.exception.BusinessException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class GeoapifyClient implements PlaceLookupPort {

    private static final String PROVIDER_CODE = "GEOAPIFY";

    private final RestClient restClient;
    private final String apiKey;

    public GeoapifyClient(
            RestClient.Builder builder,
            @Value("${geoapify.base-url:https://api.geoapify.com}") String baseUrl,
            @Value("${geoapify.api-key:}") String apiKey
    ) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    @Override
    public String providerCode() {
        return PROVIDER_CODE;
    }

    @Override
    public List<PlaceCandidate> search(String query) {
        requireConfigured();
        JsonNode response = get(uri -> uri.path("/v1/geocode/autocomplete")
                .queryParam("text", query)
                .queryParam("format", "json")
                .queryParam("lang", "ko")
                .queryParam("limit", 10)
                .queryParam("apiKey", apiKey)
                .build());

        List<PlaceCandidate> candidates = new ArrayList<>();
        for (JsonNode result : response.path("results")) {
            String placeId = text(result, "place_id");
            String name = text(result, "name");
            if (name == null) {
                name = text(result, "address_line1");
            }
            if (placeId != null && name != null) {
                candidates.add(new PlaceCandidate(placeId, name, text(result, "formatted"),
                        attribution(result), attributionUrl(result)));
            }
        }
        return List.copyOf(candidates);
    }

    @Override
    public PlaceDetails findById(String placeId) {
        if (placeId == null || !placeId.matches("[A-Za-z0-9_-]{1,255}")) {
            throw new BusinessException(PlaceErrorCode.INVALID_PLACE_ID);
        }
        requireConfigured();
        JsonNode response = get(uri -> uri.path("/v2/place-details")
                .queryParam("id", placeId)
                .queryParam("lang", "ko")
                .queryParam("apiKey", apiKey)
                .build());

        for (JsonNode feature : response.path("features")) {
            JsonNode properties = feature.path("properties");
            if (!"details".equals(text(properties, "feature_type"))) {
                continue;
            }
            String name = text(properties, "name");
            if (name == null) {
                name = text(properties, "address_line1");
            }
            String countryCode = text(properties, "country_code");
            JsonNode lat = properties.path("lat");
            JsonNode lon = properties.path("lon");
            if (name == null || !lat.isNumber() || !lon.isNumber()
                    || lat.asDouble() < -90 || lat.asDouble() > 90
                    || lon.asDouble() < -180 || lon.asDouble() > 180) {
                break;
            }
            return new PlaceDetails(placeId, name,
                    countryCode == null ? null : countryCode.toUpperCase(Locale.ROOT), lat.asDouble(), lon.asDouble(),
                    attribution(properties), attributionUrl(properties));
        }
        throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
    }

    private JsonNode get(java.util.function.Function<org.springframework.web.util.UriBuilder, java.net.URI> uri) {
        try {
            JsonNode body = restClient.get().uri(uri).retrieve().body(JsonNode.class);
            if (body == null || body.isNull()) {
                throw new BusinessException(PlaceErrorCode.PLACE_PROVIDER_UNAVAILABLE);
            }
            return body;
        } catch (RestClientResponseException exception) {
            HttpStatusCode status = exception.getStatusCode();
            if (status.value() == 400 || status.value() == 404) {
                throw new BusinessException(PlaceErrorCode.PLACE_NOT_FOUND);
            }
            throw unavailable();
        } catch (RestClientException exception) {
            throw unavailable();
        }
    }

    private void requireConfigured() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException(PlaceErrorCode.PLACE_PROVIDER_UNAVAILABLE,
                    "GEOAPIFY_API_KEY가 설정되지 않았습니다.");
        }
    }

    private static String text(JsonNode node, String key) {
        JsonNode value = node.path(key);
        return value.isTextual() && !value.asText().isBlank() ? value.asText() : null;
    }

    private static String attribution(JsonNode node) {
        String source = text(node.path("datasource"), "attribution");
        return source == null ? PlaceAttribution.OSM_TEXT : source;
    }

    private static String attributionUrl(JsonNode node) {
        String source = text(node.path("datasource"), "url");
        return source == null ? PlaceAttribution.OSM_URL : source;
    }

    private static BusinessException unavailable() {
        // RestClient 예외 메시지에는 query parameter의 API 키가 포함될 수 있다.
        return new BusinessException(PlaceErrorCode.PLACE_PROVIDER_UNAVAILABLE);
    }
}
