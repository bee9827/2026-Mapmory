package com.mapmory.backend.travelrecord.mapsummary.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mapmory.backend.common.ProblemDetailFactory;
import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.member.exception.MemberErrorCode;
import com.mapmory.backend.region.RegionType;
import com.mapmory.backend.region.exception.RegionErrorCode;
import com.mapmory.backend.travelrecord.mapsummary.dto.RegionMapSummaryResponse;
import com.mapmory.backend.travelrecord.mapsummary.policy.MapColorLevel;
import com.mapmory.backend.travelrecord.mapsummary.service.RegionMapSummaryService;
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
    private RegionMapSummaryService regionMapSummaryService;

    @Nested
    @DisplayName("GET /api/v1/travel-records/map-summary/regions/roots")
    class GetRootSummaries {

        @Test
        @DisplayName("회원의 루트 지역별 지도 요약을 의미 기반 단계와 함께 반환한다")
        void returnsRootSummaries() throws Exception {
            when(regionMapSummaryService.getSummaries(10L, null)).thenReturn(List.of(
                    new RegionMapSummaryResponse(
                            1L,
                            "KR",
                            RegionType.COUNTRY,
                            "대한민국",
                            3L,
                            MapColorLevel.MEDIUM
                    )
            ));

            mockMvc.perform(get("/api/v1/travel-records/map-summary/regions/roots")
                            .header("X-Member-Id", 10L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].regionId").value(1))
                    .andExpect(jsonPath("$.data[0].code").value("KR"))
                    .andExpect(jsonPath("$.data[0].regionType").value("COUNTRY"))
                    .andExpect(jsonPath("$.data[0].name").value("대한민국"))
                    .andExpect(jsonPath("$.data[0].count").value(3))
                    .andExpect(jsonPath("$.data[0].level").value("MEDIUM"));

            verify(regionMapSummaryService).getSummaries(10L, null);
        }

        @Test
        @DisplayName("회원 ID가 양수가 아니면 400을 반환한다")
        void rejectsNonPositiveMemberId() throws Exception {
            mockMvc.perform(get("/api/v1/travel-records/map-summary/regions/roots")
                            .header("X-Member-Id", 0L))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("회원 ID 헤더가 없으면 400을 반환한다")
        void rejectsMissingMemberId() throws Exception {
            mockMvc.perform(get("/api/v1/travel-records/map-summary/regions/roots"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors[0].field").value("X-Member-Id"));
        }

        @Test
        @DisplayName("회원 ID가 숫자 형식이 아니면 400을 반환한다")
        void rejectsMalformedMemberId() throws Exception {
            mockMvc.perform(get("/api/v1/travel-records/map-summary/regions/roots")
                            .header("X-Member-Id", "member"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                    .andExpect(jsonPath("$.errors[0].field").value("X-Member-Id"));
        }

        @Test
        @DisplayName("회원을 찾을 수 없으면 MEMBER_NOT_FOUND를 반환한다")
        void returnsMemberNotFound() throws Exception {
            when(regionMapSummaryService.getSummaries(999L, null))
                    .thenThrow(new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));

            mockMvc.perform(get("/api/v1/travel-records/map-summary/regions/roots")
                            .header("X-Member-Id", 999L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("MEMBER_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/travel-records/map-summary/regions/{regionId}/children")
    class GetChildSummaries {

        @Test
        @DisplayName("선택 지역의 직속 하위 지역별 지도 요약을 반환한다")
        void returnsChildSummaries() throws Exception {
            when(regionMapSummaryService.getSummaries(10L, 1L)).thenReturn(List.of(
                    new RegionMapSummaryResponse(
                            15L,
                            "49",
                            RegionType.PROVINCE,
                            "제주특별자치도",
                            3L,
                            MapColorLevel.MEDIUM
                    )
            ));

            mockMvc.perform(get("/api/v1/travel-records/map-summary/regions/1/children")
                            .header("X-Member-Id", 10L))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].regionId").value(15))
                    .andExpect(jsonPath("$.data[0].code").value("49"))
                    .andExpect(jsonPath("$.data[0].regionType").value("PROVINCE"))
                    .andExpect(jsonPath("$.data[0].count").value(3))
                    .andExpect(jsonPath("$.data[0].level").value("MEDIUM"));

            verify(regionMapSummaryService).getSummaries(10L, 1L);
        }

        @Test
        @DisplayName("지역 ID가 양수가 아니면 400을 반환한다")
        void rejectsNonPositiveRegionId() throws Exception {
            mockMvc.perform(get("/api/v1/travel-records/map-summary/regions/0/children")
                            .header("X-Member-Id", 10L))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("상위 지역을 찾을 수 없으면 REGION_NOT_FOUND를 반환한다")
        void returnsRegionNotFound() throws Exception {
            when(regionMapSummaryService.getSummaries(10L, 999L))
                    .thenThrow(new BusinessException(RegionErrorCode.REGION_NOT_FOUND));

            mockMvc.perform(get("/api/v1/travel-records/map-summary/regions/999/children")
                            .header("X-Member-Id", 10L))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("REGION_NOT_FOUND"));
        }
    }
}
