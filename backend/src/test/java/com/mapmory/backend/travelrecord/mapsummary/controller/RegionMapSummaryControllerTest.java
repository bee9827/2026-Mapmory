package com.mapmory.backend.travelrecord.mapsummary.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mapmory.backend.common.ProblemDetailFactory;
import com.mapmory.backend.region.RegionType;
import com.mapmory.backend.travelrecord.mapsummary.dto.RegionMapSummaryResponse;
import com.mapmory.backend.travelrecord.mapsummary.policy.MapColorLevel;
import com.mapmory.backend.travelrecord.mapsummary.service.CountryMapSummaryService;
import com.mapmory.backend.travelrecord.mapsummary.service.ProvinceMapSummaryService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RegionMapSummaryController.class)
@Import(ProblemDetailFactory.class)
@DisplayName("Region 지도 요약 API")
class RegionMapSummaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CountryMapSummaryService countryMapSummaryService;

    @MockitoBean
    private ProvinceMapSummaryService provinceMapSummaryService;

    @Nested
    @DisplayName("GET /api/v1/travel-records/map-summary/regions")
    class GetCountrySummaries {

        @Test
        @DisplayName("회원의 국가별 지도 요약을 data로 감싸 반환한다")
        void returnsCountrySummaries() throws Exception {
            when(countryMapSummaryService.getCountrySummaries(10L)).thenReturn(List.of(
                    new RegionMapSummaryResponse(
                            1L,
                            "KR",
                            RegionType.COUNTRY,
                            "대한민국",
                            3L,
                            MapColorLevel.MEDIUM
                    )
            ));

            mockMvc.perform(get("/api/v1/travel-records/map-summary/regions")
                            .header("X-Member-Id", 10L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].regionId").value(1))
                    .andExpect(jsonPath("$.data[0].regionCode").value("KR"))
                    .andExpect(jsonPath("$.data[0].regionType").value("COUNTRY"))
                    .andExpect(jsonPath("$.data[0].name").value("대한민국"))
                    .andExpect(jsonPath("$.data[0].count").value(3))
                    .andExpect(jsonPath("$.data[0].level").value("MEDIUM"));

            verify(countryMapSummaryService).getCountrySummaries(10L);
        }

        @Test
        @DisplayName("회원 ID가 양수가 아니면 400을 반환한다")
        void rejectsNonPositiveMemberId() throws Exception {
            mockMvc.perform(get("/api/v1/travel-records/map-summary/regions")
                            .header("X-Member-Id", 0L))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("회원 ID 헤더가 없으면 400을 반환한다")
        void rejectsMissingMemberId() throws Exception {
            mockMvc.perform(get("/api/v1/travel-records/map-summary/regions"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors[0].field").value("X-Member-Id"));
        }

        @Test
        @DisplayName("회원 ID가 숫자 형식이 아니면 400을 반환한다")
        void rejectsMalformedMemberId() throws Exception {
            mockMvc.perform(get("/api/v1/travel-records/map-summary/regions")
                            .header("X-Member-Id", "member"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors[0].field").value("X-Member-Id"));
        }

        @Test
        @DisplayName("상위 국가 ID가 있으면 시도별 지도 요약을 반환한다")
        void returnsProvinceSummariesForParentCountry() throws Exception {
            when(provinceMapSummaryService.getProvinceSummaries(10L, 1L)).thenReturn(List.of(
                    new RegionMapSummaryResponse(
                            15L,
                            "49",
                            RegionType.PROVINCE,
                            "제주특별자치도",
                            3L,
                            MapColorLevel.MEDIUM
                    )
            ));

            mockMvc.perform(get("/api/v1/travel-records/map-summary/regions")
                            .header("X-Member-Id", 10L)
                            .queryParam("parentRegionId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].regionId").value(15))
                    .andExpect(jsonPath("$.data[0].regionCode").value("49"))
                    .andExpect(jsonPath("$.data[0].regionType").value("PROVINCE"))
                    .andExpect(jsonPath("$.data[0].count").value(3))
                    .andExpect(jsonPath("$.data[0].level").value("MEDIUM"));

            verify(provinceMapSummaryService).getProvinceSummaries(10L, 1L);
        }

        @Test
        @DisplayName("상위 국가 ID가 양수가 아니면 400을 반환한다")
        void rejectsNonPositiveParentRegionId() throws Exception {
            mockMvc.perform(get("/api/v1/travel-records/map-summary/regions")
                            .header("X-Member-Id", 10L)
                            .queryParam("parentRegionId", "0"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }
    }
}
