package com.mapmory.backend.mapsummary;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mapmory.backend.common.ProblemDetailFactory;
import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.mapsummary.api.CountryMapSummaryResponse;
import com.mapmory.backend.mapsummary.api.MapSummaryController;
import com.mapmory.backend.mapsummary.api.RegionMapSummaryResponse;
import com.mapmory.backend.mapsummary.application.MapSummaryService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MapSummaryController.class)
@Import(ProblemDetailFactory.class)
@DisplayName("지도 요약 API 컨트롤러")
class MapSummaryControllerTest {

    @MockitoBean
    private MapSummaryService mapSummaryService;

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("6.1 세계 지도 국가별 집계")
    class CountrySummary {

        @Test
        @DisplayName("현재 회원의 국가별 지도 요약을 data 배열로 반환한다")
        void returnsCountrySummaries() throws Exception {
            when(mapSummaryService.summarizeCountries(10L)).thenReturn(List.of(
                    new CountryMapSummaryResponse("KR", "대한민국", 12, 3),
                    new CountryMapSummaryResponse("JP", "일본", 5, 2)
            ));

            mockMvc.perform(get("/api/v1/travel-records/map-summary/countries")
                            .header("X-Member-Id", 10))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].countryCode").value("KR"))
                    .andExpect(jsonPath("$.data[0].name").value("대한민국"))
                    .andExpect(jsonPath("$.data[0].count").value(12))
                    .andExpect(jsonPath("$.data[0].level").value(3))
                    .andExpect(jsonPath("$.data[1].countryCode").value("JP"));
        }

        @Test
        @DisplayName("존재하지 않는 회원 요청은 code가 없는 404 Problem Details를 반환한다")
        void returnsMemberNotFoundProblemDetailWithoutCode() throws Exception {
            when(mapSummaryService.summarizeCountries(999L))
                    .thenThrow(new BusinessException(MapSummaryErrorCode.MEMBER_NOT_FOUND));

            mockMvc.perform(get("/api/v1/travel-records/map-summary/countries")
                            .header("X-Member-Id", 999))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.code").doesNotExist())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("6.2 국가의 최상위 지역별 집계")
    class TopLevelRegionSummary {

        @Test
        @DisplayName("선택한 국가의 최상위 지역별 지도 요약을 반환한다")
        void returnsTopLevelRegionSummaries() throws Exception {
            when(mapSummaryService.summarizeTopLevelRegions(10L, "KR")).thenReturn(List.of(
                    new RegionMapSummaryResponse("11", "서울특별시", 5, 2)
            ));

            mockMvc.perform(get("/api/v1/travel-records/map-summary/countries/KR/regions")
                            .header("X-Member-Id", 10))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].locationCode").value("11"))
                    .andExpect(jsonPath("$.data[0].name").value("서울특별시"))
                    .andExpect(jsonPath("$.data[0].count").value(5))
                    .andExpect(jsonPath("$.data[0].level").value(2));
        }

        @Test
        @DisplayName("존재하지 않는 국가 요청은 COUNTRY_NOT_FOUND Problem Details를 반환한다")
        void returnsCountryNotFoundProblemDetail() throws Exception {
            when(mapSummaryService.summarizeTopLevelRegions(10L, "US"))
                    .thenThrow(new BusinessException(MapSummaryErrorCode.COUNTRY_NOT_FOUND));

            mockMvc.perform(get("/api/v1/travel-records/map-summary/countries/US/regions")
                            .header("X-Member-Id", 10))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("COUNTRY_NOT_FOUND"))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.instance").value(
                            "/api/v1/travel-records/map-summary/countries/US/regions"
                    ));
        }
    }

    @Nested
    @DisplayName("6.3 지역의 직속 하위 지역별 집계")
    class ChildRegionSummary {

        @Test
        @DisplayName("선택한 상위 지역의 직속 하위 지역별 지도 요약을 반환한다")
        void returnsChildRegionSummaries() throws Exception {
            when(mapSummaryService.summarizeChildren(10L, "KR", "11")).thenReturn(List.of(
                    new RegionMapSummaryResponse("11110", "종로구", 3, 2)
            ));

            mockMvc.perform(get(
                            "/api/v1/travel-records/map-summary/countries/KR/regions/11/children"
                    ).header("X-Member-Id", 10))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].locationCode").value("11110"))
                    .andExpect(jsonPath("$.data[0].name").value("종로구"))
                    .andExpect(jsonPath("$.data[0].count").value(3))
                    .andExpect(jsonPath("$.data[0].level").value(2));
        }

        @Test
        @DisplayName("존재하지 않는 상위 지역 요청은 LOCATION_NOT_FOUND Problem Details를 반환한다")
        void returnsLocationNotFoundProblemDetail() throws Exception {
            when(mapSummaryService.summarizeChildren(10L, "KR", "99"))
                    .thenThrow(new BusinessException(MapSummaryErrorCode.LOCATION_NOT_FOUND));

            mockMvc.perform(get(
                            "/api/v1/travel-records/map-summary/countries/KR/regions/99/children"
                    ).header("X-Member-Id", 10))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("LOCATION_NOT_FOUND"))
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("공통 요청 값 검증")
    class RequestValidation {

        @Nested
        @DisplayName("X-Member-Id 헤더")
        class MemberIdHeader {

            @Test
            @DisplayName("헤더가 없으면 400 VALIDATION_ERROR를 반환한다")
            void rejectsMissingMemberIdHeader() throws Exception {
                mockMvc.perform(get("/api/v1/travel-records/map-summary/countries"))
                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                        .andExpect(jsonPath("$.errors[0].field").value("X-Member-Id"));
            }

            @Test
            @DisplayName("숫자가 아니면 400 VALIDATION_ERROR를 반환한다")
            void rejectsNonNumericMemberIdHeader() throws Exception {
                mockMvc.perform(get("/api/v1/travel-records/map-summary/countries")
                                .header("X-Member-Id", "not-a-number"))
                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
            }

            @Test
            @DisplayName("양수가 아니면 400 VALIDATION_ERROR를 반환한다")
            void rejectsNonPositiveMemberIdHeader() throws Exception {
                mockMvc.perform(get("/api/v1/travel-records/map-summary/countries")
                                .header("X-Member-Id", 0))
                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
            }
        }

        @Nested
        @DisplayName("countryCode 경로 변수")
        class CountryCodePathVariable {

            @Test
            @DisplayName("대문자 ISO-2 형식이 아니면 400 VALIDATION_ERROR를 반환한다")
            void rejectsInvalidCountryCodeFormat() throws Exception {
                mockMvc.perform(get("/api/v1/travel-records/map-summary/countries/kr/regions")
                                .header("X-Member-Id", 10))
                        .andExpect(status().isBadRequest())
                        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
            }
        }
    }
}
