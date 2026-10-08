package com.mapmory.backend.place.infrastructure.kakao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class KakaoLocalClientTest {

    @Test
    void 키워드_검색_결과를_장소로_변환한다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        KakaoLocalClient client = new KakaoLocalClient(builder, "https://dapi.kakao.test", "rest-key");
        server.expect(queryParam("query", URLEncoder.encode("경복", StandardCharsets.UTF_8)))
                .andExpect(header("Authorization", "KakaoAK rest-key"))
                .andRespond(withSuccess("""
                        {"documents":[
                          {"id":"18619000","place_name":"경복궁","road_address_name":"서울 종로구 사직로 161",
                           "address_name":"서울 종로구 세종로 1-91","x":"126.977","y":"37.5796"},
                          {"id":"1","place_name":"좌표 없음"}
                        ]}
                        """, MediaType.APPLICATION_JSON));

        assertThat(client.search("경복"))
                .containsExactly(new KakaoPlace("18619000", "경복궁", "서울 종로구 사직로 161", 37.5796, 126.977));
        server.verify();
    }

    @Test
    void 키가_없으면_설정되지_않은_것으로_본다() {
        assertThat(new KakaoLocalClient(RestClient.builder(), "https://dapi.kakao.test", "").isConfigured())
                .isFalse();
    }
}
