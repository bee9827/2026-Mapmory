package com.mapmory.backend.travelrecord;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.mapmory.backend.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * 상세 응답의 지역 계층이 트랜잭션 밖에서도 채워지는지 확인한다.
 *
 * <p>open-in-view가 꺼져 있어 컨트롤러 시점에는 영속성 컨텍스트가 닫혀 있다.
 * RegionDetailResponse는 시·군·구 기록에서 Region의 parent·root를 읽는데, 이 둘은 지연 로딩이라
 * 서비스가 미리 초기화해 두지 않으면 LazyInitializationException이 난다.
 * 기존 인수 테스트는 모두 국가 단위(JP) 기록만 다뤄 이 경로를 지나지 않았다.
 */
@AutoConfigureMockMvc
class TravelRecordDetailAcceptanceTest extends IntegrationTest {

    private static final String SEOUL_PROVINCE_CODE = "11";
    private static final String JONGNO_DISTRICT_CODE = "11110";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void 시군구_기록의_상세_조회는_국가_시도_시군구를_모두_응답한다() throws Exception {
        String accessToken = guestAccessToken();
        long recordId = createDistrictRecord(accessToken);

        mockMvc.perform(get("/api/v1/travel-records/" + recordId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.region.country.code").value("KR"))
                .andExpect(jsonPath("$.data.region.province.code").value(SEOUL_PROVINCE_CODE))
                .andExpect(jsonPath("$.data.region.district.code").value(JONGNO_DISTRICT_CODE));
    }

    @Test
    void 시군구_기록의_수정_응답도_국가_시도_시군구를_모두_담는다() throws Exception {
        String accessToken = guestAccessToken();
        long recordId = createDistrictRecord(accessToken);

        mockMvc.perform(put("/api/v1/travel-records/" + recordId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(districtRecordBody("수정한 종로 여행")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("수정한 종로 여행"))
                .andExpect(jsonPath("$.data.region.country.code").value("KR"))
                .andExpect(jsonPath("$.data.region.province.code").value(SEOUL_PROVINCE_CODE))
                .andExpect(jsonPath("$.data.region.district.code").value(JONGNO_DISTRICT_CODE));
    }

    private long createDistrictRecord(String accessToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/travel-records")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(districtRecordBody("종로 여행")))
                .andExpect(status().isCreated())
                .andReturn();
        Integer id = JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");
        return id.longValue();
    }

    private String districtRecordBody(String title) {
        return """
                {
                  "countryCode": "KR",
                  "provinceCode": "%s",
                  "districtCode": "%s",
                  "title": "%s",
                  "content": "기록 본문",
                  "startDate": "2026-08-01",
                  "objectKeys": []
                }
                """.formatted(SEOUL_PROVINCE_CODE, JONGNO_DISTRICT_CODE, title);
    }

    private String guestAccessToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login/guest"))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
    }
}
