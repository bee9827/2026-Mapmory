package com.mapmory.backend.place.infrastructure.google;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.place.application.model.PlaceCandidate;
import com.mapmory.backend.place.application.model.PlaceDetails;
import com.mapmory.backend.place.infrastructure.geoapify.GeoapifyRequestLimiter;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class GooglePlacesClientTest {

    private static final String BASE_URL = "https://places.google.test";

    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final GooglePlacesClient client =
            new GooglePlacesClient(builder, BASE_URL, "test-key", mock(GeoapifyRequestLimiter.class));

    @Test
    void 자동완성_후보를_장소명과_보조_주소로_반환한다() {
        server.expect(requestTo(BASE_URL + "/v1/places:autocomplete"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Goog-Api-Key", "test-key"))
                .andExpect(content().json("""
                        {"input":"경복","languageCode":"ko","sessionToken":"session-1"}
                        """))
                .andRespond(withSuccess("""
                        {"suggestions":[
                          {"placePrediction":{"placeId":"ChIJ-gyeong","text":{"text":"경복궁, 서울특별시 종로구"},
                            "structuredFormat":{"mainText":{"text":"경복궁"},
                                                "secondaryText":{"text":"서울특별시 종로구"}}}},
                          {"queryPrediction":{"text":{"text":"경복궁 맛집"}}},
                          {"placePrediction":{"placeId":"ChIJ-station","text":{"text":"경복궁역"}}}
                        ]}
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.search("경복", "session-1")).containsExactly(
                new PlaceCandidate("ChIJ-gyeong", "경복궁", "서울특별시 종로구",
                        GooglePlacesClient.ATTRIBUTION, GooglePlacesClient.ATTRIBUTION_URL),
                new PlaceCandidate("ChIJ-station", "경복궁역", null,
                        GooglePlacesClient.ATTRIBUTION, GooglePlacesClient.ATTRIBUTION_URL));
        server.verify();
    }

    @Test
    void 선택_조회는_화면에_보여_줄_이름까지_받는다() {
        server.expect(requestToUriTemplate(BASE_URL + "/v1/places/{id}?languageCode=ko&sessionToken=session-1",
                        "ChIJ-gyeong"))
                .andExpect(header("X-Goog-FieldMask", "id,location,addressComponents,displayName"))
                .andRespond(withSuccess("""
                        {"id":"ChIJ-gyeong","displayName":{"text":"경복궁","languageCode":"ko"},
                         "location":{"latitude":37.5796,"longitude":126.977},
                         "addressComponents":[
                           {"longText":"종로구","shortText":"종로구","types":["sublocality_level_1"]},
                           {"longText":"대한민국","shortText":"KR","types":["country","political"]}]}
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.findForSelection("ChIJ-gyeong", "session-1"))
                .isEqualTo(new PlaceDetails("ChIJ-gyeong", "경복궁", "KR", 37.5796, 126.977,
                        GooglePlacesClient.ATTRIBUTION, GooglePlacesClient.ATTRIBUTION_URL));
        server.verify();
    }

    @Test
    void 저장용_조회는_Essentials_필드만_요청해_이름을_받지_않는다() {
        server.expect(requestToUriTemplate(BASE_URL + "/v1/places/{id}?languageCode=ko", "ChIJ-gyeong"))
                .andExpect(header("X-Goog-FieldMask", "id,location,addressComponents"))
                .andRespond(withSuccess("""
                        {"id":"ChIJ-gyeong","location":{"latitude":37.5796,"longitude":126.977},
                         "addressComponents":[{"shortText":"KR","types":["country","political"]}]}
                        """, MediaType.APPLICATION_JSON));

        PlaceDetails place = client.findById("ChIJ-gyeong");

        assertThat(place.name()).isNull();
        assertThat(place.countryCode()).isEqualTo("KR");
        server.verify();
    }

    @Test
    void 괌은_영토_분류_전까지_미국으로_돌려준다() {
        server.expect(queryParam("languageCode", "ko"))
                .andRespond(withSuccess("""
                        {"id":"ChIJ-tumon","location":{"latitude":13.51,"longitude":144.80},
                         "addressComponents":[{"shortText":"GU","types":["country","political"]}]}
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.findById("ChIJ-tumon").countryCode()).isEqualTo("US");
    }

    @Test
    void 없는_장소_ID는_장소_없음으로_처리한다() {
        server.expect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> client.findById("ChIJ-missing"))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode().code())
                        .isEqualTo("PLACE_NOT_FOUND"));
    }

    @Test
    void API_키가_없으면_외부_요청_없이_오류를_반환한다() {
        GooglePlacesClient unconfigured =
                new GooglePlacesClient(RestClient.builder(), BASE_URL, "", mock(GeoapifyRequestLimiter.class));

        assertThat(unconfigured.isConfigured()).isFalse();
        assertThatThrownBy(() -> unconfigured.search("경복", null))
                .isInstanceOf(BusinessException.class)
                .satisfies(error -> assertThat(((BusinessException) error).getErrorCode().code())
                        .isEqualTo("PLACE_PROVIDER_UNAVAILABLE"));
    }
}
