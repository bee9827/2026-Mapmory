package com.mapmory.backend.travelrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mapmory.backend.auth.security.LoginMember;
import com.mapmory.backend.member.Member;
import com.mapmory.backend.travelrecord.dto.CreateTravelRecordResponse;
import com.mapmory.backend.travelrecord.dto.RegionDetailResponse;
import com.mapmory.backend.travelrecord.dto.RegionItemResponse;
import com.mapmory.backend.travelrecord.dto.TravelRecordDetailResponse;
import com.mapmory.backend.travelrecord.dto.TravelRecordRequest;
import com.mapmory.backend.travelrecord.dto.TravelRecordResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@ExtendWith(MockitoExtension.class)
class TravelRecordControllerTest {

    private static final long MEMBER_ID = 10L;
    private static final Member MEMBER = Member.of("테스터", UUID.randomUUID());

    static {
        ReflectionTestUtils.setField(MEMBER, "id", MEMBER_ID);
    }

    @Mock
    private TravelRecordService travelRecordService;

    @InjectMocks
    private TravelRecordController travelRecordController;

    // @LoginMember를 고정 Member로 해석하는 리졸버. standalone MockMvc에서 HTTP·JSON 레이어만
    // 검증하고, 실제 인증(401 등)은 SecurityIntegrationTest가 담당한다.
    private MockMvc mockMvcWithLoginMember() {
        return MockMvcBuilders.standaloneSetup(travelRecordController)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.hasParameterAnnotation(LoginMember.class);
                    }

                    @Override
                    public Object resolveArgument(
                            MethodParameter parameter,
                            ModelAndViewContainer mavContainer,
                            NativeWebRequest webRequest,
                            WebDataBinderFactory binderFactory
                    ) {
                        return MEMBER;
                    }
                })
                .build();
    }

    @Test
    void 여행_일지를_생성한다() {
        TravelRecordRequest request = new TravelRecordRequest(
                "JP",
                null,
                null,
                "일본 여행",
                "",
                LocalDate.of(2026, 8, 11),
                null,
                List.of()
        );
        TravelRecord travelRecord = TravelRecord.of(
                null,
                null,
                request.title(),
                request.content(),
                request.startDate(),
                request.endDate()
        );
        ReflectionTestUtils.setField(travelRecord, "id", 1L);

        when(travelRecordService.create(MEMBER, request)).thenReturn(travelRecord);

        ResponseEntity<TravelRecordResponse<CreateTravelRecordResponse>> response =
                travelRecordController.create(MEMBER, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(
                TravelRecordResponse.of(new CreateTravelRecordResponse(1L))
        );
        verify(travelRecordService).create(MEMBER, request);
    }

    @Test
    void 여행_일지_상세_조회를_서비스에_위임한다() {
        TravelRecordDetailResponse detail = detail("제주 여행", "제주시를 걸었다.",
                List.of("mapmory/travel-records/a.jpg"));
        when(travelRecordService.findById(MEMBER, 101L)).thenReturn(detail);

        ResponseEntity<TravelRecordResponse<TravelRecordDetailResponse>> response =
                travelRecordController.findById(MEMBER, 101L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(TravelRecordResponse.of(detail));
        verify(travelRecordService).findById(MEMBER, 101L);
    }

    @Test
    void 여행_일지_상세_HTTP_응답을_반환한다() throws Exception {
        TravelRecordDetailResponse detail = detail("제주 여행", "제주시를 걸었다.",
                List.of("mapmory/travel-records/a.jpg"));
        when(travelRecordService.findById(MEMBER, 101L)).thenReturn(detail);

        mockMvcWithLoginMember().perform(get("/api/v1/travel-records/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(101L))
                .andExpect(jsonPath("$.data.content").value("제주시를 걸었다."))
                .andExpect(jsonPath("$.data.region.country.code").value("KR"))
                .andExpect(jsonPath("$.data.region.district.code").value("50110"))
                .andExpect(jsonPath("$.data.objectKeys[0]")
                        .value("mapmory/travel-records/a.jpg"));
    }

    @Test
    void 여행_일지를_수정한다() throws Exception {
        TravelRecordDetailResponse detail = detail("수정된 제주 여행", "수정된 본문",
                List.of("travel-records/10/b.jpg"));
        when(travelRecordService.update(
                ArgumentMatchers.eq(MEMBER),
                ArgumentMatchers.eq(101L),
                ArgumentMatchers.any(TravelRecordRequest.class)
        )).thenReturn(detail);

        mockMvcWithLoginMember().perform(put("/api/v1/travel-records/101")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "countryCode": "KR",
                                  "provinceCode": "49",
                                  "districtCode": "50110",
                                  "title": "수정된 제주 여행",
                                  "content": "수정된 본문",
                                  "startDate": "2026-08-11",
                                  "endDate": "2026-08-13",
                                  "objectKeys": ["travel-records/10/b.jpg"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(101L))
                .andExpect(jsonPath("$.data.title").value("수정된 제주 여행"))
                .andExpect(jsonPath("$.data.region.district.code").value("50110"))
                .andExpect(jsonPath("$.data.objectKeys[0]")
                        .value("travel-records/10/b.jpg"));
    }

    @Test
    void 여행_일지를_삭제한다() throws Exception {
        mockMvcWithLoginMember().perform(delete("/api/v1/travel-records/101"))
                .andExpect(status().isNoContent());

        verify(travelRecordService).delete(MEMBER, 101L);
    }

    private TravelRecordDetailResponse detail(String title, String content, List<String> objectKeys) {
        return new TravelRecordDetailResponse(
                101L,
                title,
                content,
                new RegionDetailResponse(
                        new RegionItemResponse("KR", "대한민국"),
                        new RegionItemResponse("49", "제주특별자치도"),
                        new RegionItemResponse("50110", "제주시")
                ),
                LocalDate.of(2026, 8, 11),
                LocalDate.of(2026, 8, 13),
                objectKeys,
                null,
                null
        );
    }
}
