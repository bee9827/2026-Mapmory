package com.mapmory.backend.place.infrastructure;

import com.mapmory.backend.place.application.model.PlaceCandidate;
import com.mapmory.backend.place.application.model.PlaceDetails;
import com.mapmory.backend.place.application.port.PlaceLookupPort;
import com.mapmory.backend.place.infrastructure.geoapify.GeoapifyClient;
import com.mapmory.backend.place.infrastructure.google.GooglePlacesClient;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Google 키가 있으면 Google 자동완성으로 검색하고, 없으면 Geoapify를 쓴다.
 * Geoapify 장소 ID(소문자 16진수)는 기존 기록 수정을 위해 계속 Geoapify로 조회한다.
 */
@Primary
@Component
public class PlaceLookupRouter implements PlaceLookupPort {

    static final String GOOGLE_PROVIDER_CODE = "GOOGLE";

    private static final Pattern GEOAPIFY_PLACE_ID = Pattern.compile("[0-9a-f]+");

    private final GooglePlacesClient google;
    private final GeoapifyClient geoapify;

    public PlaceLookupRouter(GooglePlacesClient google, GeoapifyClient geoapify) {
        this.google = google;
        this.geoapify = geoapify;
    }

    @Override
    public String providerCode(String placeId) {
        return usesGoogle(placeId) ? GOOGLE_PROVIDER_CODE : geoapify.providerCode(placeId);
    }

    @Override
    public List<PlaceCandidate> search(String query, String sessionToken) {
        return google.isConfigured() ? google.search(query, sessionToken) : geoapify.search(query);
    }

    @Override
    public PlaceDetails findForSelection(String placeId, String sessionToken) {
        return usesGoogle(placeId)
                ? google.findForSelection(placeId, sessionToken)
                : geoapify.findById(placeId);
    }

    @Override
    public PlaceDetails findById(String placeId) {
        return usesGoogle(placeId) ? google.findById(placeId) : geoapify.findById(placeId);
    }

    private boolean usesGoogle(String placeId) {
        return google.isConfigured() && placeId != null && !GEOAPIFY_PLACE_ID.matcher(placeId).matches();
    }
}
