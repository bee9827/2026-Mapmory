package com.mapmory.backend.travelrecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mapmory.backend.common.ProblemDetailFactory;
import com.mapmory.backend.common.handler.ValidationExceptionHandler;
import com.mapmory.backend.travelrecord.dto.CreateTravelRecordResponse;
import com.mapmory.backend.travelrecord.dto.RegionDetailResponse;
import com.mapmory.backend.travelrecord.dto.RegionItemResponse;
import com.mapmory.backend.travelrecord.dto.TravelRecordDetailResponse;
import com.mapmory.backend.travelrecord.dto.TravelRecordRequest;
import com.mapmory.backend.travelrecord.dto.TravelRecordResponse;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;

@ExtendWith(MockitoExtension.class)
class TravelRecordControllerTest {

    @Mock
    private TravelRecordService travelRecordService;

    @InjectMocks
    private TravelRecordController travelRecordController;

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

        when(travelRecordService.create(10L, request)).thenReturn(travelRecord);

        ResponseEntity<TravelRecordResponse<CreateTravelRecordResponse>> response =
                travelRecordController.create(10L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(
                TravelRecordResponse.of(new CreateTravelRecordResponse(1L))
        );
        verify(travelRecordService).create(10L, request);
    }

    @Test
    void 여행_일지_상세_조회를_서비스에_위임한다() {
        TravelRecordDetailResponse detail = new TravelRecordDetailResponse(
                101L,
                "제주 여행",
                "제주시를 걸었다.",
                new RegionDetailResponse(
                        new RegionItemResponse("KR", "대한민국"),
                        new RegionItemResponse("49", "제주특별자치도"),
                        new RegionItemResponse("50110", "제주시")
                ),
                LocalDate.of(2026, 8, 11),
                LocalDate.of(2026, 8, 13),
                List.of("mapmory/travel-records/a.jpg"),
                null,
                null
        );
        when(travelRecordService.findById(10L, 101L)).thenReturn(detail);

        ResponseEntity<TravelRecordResponse<TravelRecordDetailResponse>> response =
                travelRecordController.findById(10L, 101L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(TravelRecordResponse.of(detail));
        verify(travelRecordService).findById(10L, 101L);
    }

    @Test
    void 여행_일지_상세_HTTP_응답을_반환한다() throws Exception {
        TravelRecordDetailResponse detail = new TravelRecordDetailResponse(
                101L,
                "제주 여행",
                "제주시를 걸었다.",
                new RegionDetailResponse(
                        new RegionItemResponse("KR", "대한민국"),
                        new RegionItemResponse("49", "제주특별자치도"),
                        new RegionItemResponse("50110", "제주시")
                ),
                LocalDate.of(2026, 8, 11),
                LocalDate.of(2026, 8, 13),
                List.of("mapmory/travel-records/a.jpg"),
                null,
                null
        );
        when(travelRecordService.findById(10L, 101L)).thenReturn(detail);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(travelRecordController).build();

        mockMvc.perform(get("/api/v1/travel-records/101")
                        .header("X-Member-Id", 10L))
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
        TravelRecordDetailResponse detail = new TravelRecordDetailResponse(
                101L,
                "수정된 제주 여행",
                "수정된 본문",
                new RegionDetailResponse(
                        new RegionItemResponse("KR", "대한민국"),
                        new RegionItemResponse("49", "제주특별자치도"),
                        new RegionItemResponse("50110", "제주시")
                ),
                LocalDate.of(2026, 8, 11),
                LocalDate.of(2026, 8, 13),
                List.of("travel-records/10/b.jpg"),
                null,
                null
        );
        when(travelRecordService.update(
                org.mockito.ArgumentMatchers.eq(10L),
                org.mockito.ArgumentMatchers.eq(101L),
                org.mockito.ArgumentMatchers.any(TravelRecordRequest.class)
        )).thenReturn(detail);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(travelRecordController).build();

        mockMvc.perform(put("/api/v1/travel-records/101")
                        .header("X-Member-Id", 10L)
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
    void 회원_ID_헤더가_없으면_요청을_거부한다() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(travelRecordController)
                .setControllerAdvice(new ValidationExceptionHandler(new ProblemDetailFactory()))
                .build();

        mockMvc.perform(get("/api/v1/travel-records"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[0].field").value("X-Member-Id"));
    }
}
