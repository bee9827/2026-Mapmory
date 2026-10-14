package com.mapmory.backend.place.ratelimit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.mapmory.backend.IntegrationTest;
import com.mapmory.backend.place.application.model.PlaceDetails;
import com.mapmory.backend.place.infrastructure.google.GooglePlacesClient;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "place.rate-limit.searches-per-member-per-minute=2",
        "place.rate-limit.selections-per-member-per-day=1"
})
class PlaceRateLimitInterceptorIntegrationTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GooglePlacesClient googlePlacesClient;

    @Test
    void 회원별_검색_한도를_넘으면_Google을_부르지_않고_거절한다() throws Exception {
        given(googlePlacesClient.search(anyString(), anyString())).willReturn(List.of());
        String accessToken = guestAccessToken();

        search(accessToken).andExpect(status().isOk());
        search(accessToken).andExpect(status().isOk());
        search(accessToken)
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("PLACE_RATE_LIMITED"));

        verify(googlePlacesClient, times(2)).search(anyString(), anyString());
        // 다른 회원은 따로 센다.
        search(guestAccessToken()).andExpect(status().isOk());
    }

    @Test
    void 선택_한도는_검색_한도와_따로_센다() throws Exception {
        given(googlePlacesClient.search(anyString(), anyString())).willReturn(List.of());
        given(googlePlacesClient.findForSelection(anyString(), any())).willReturn(
                new PlaceDetails("ChIJ-tokyo", null, null, "Google Maps", "https://www.google.com/maps", List.of()));
        String accessToken = guestAccessToken();

        select(accessToken).andExpect(status().isOk());
        select(accessToken)
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("PLACE_RATE_LIMITED"));

        verify(googlePlacesClient, times(1)).findForSelection(anyString(), any());
        // 선택 한도를 다 써도 검색은 된다. 선택 요청이 검색 한도를 쓰지 않았다.
        search(accessToken).andExpect(status().isOk());
        search(accessToken).andExpect(status().isOk());
    }

    private ResultActions search(String accessToken) throws Exception {
        return mockMvc.perform(get("/api/v1/places/search")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .param("query", "도쿄")
                .param("sessionToken", "session-1"));
    }

    private ResultActions select(String accessToken) throws Exception {
        return mockMvc.perform(get("/api/v1/places/ChIJ-tokyo")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));
    }

    private String guestAccessToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login/guest"))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
    }
}
