package com.mapmory.backend.travelrecord.mapsummary.controller;

import com.mapmory.backend.common.dto.ApiResponse;
import com.mapmory.backend.travelrecord.mapsummary.dto.RegionMapSummaryResponse;
import com.mapmory.backend.travelrecord.mapsummary.service.CountryMapSummaryService;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/travel-records/map-summary/regions")
public class RegionMapSummaryController {

    private final CountryMapSummaryService countryMapSummaryService;

    public RegionMapSummaryController(CountryMapSummaryService countryMapSummaryService) {
        this.countryMapSummaryService = countryMapSummaryService;
    }

    @GetMapping
    public ApiResponse<List<RegionMapSummaryResponse>> getCountrySummaries(
            @RequestHeader("X-Member-Id")
            @Positive(message = "회원 ID는 양수여야 합니다.")
            Long memberId
    ) {
        return ApiResponse.from(countryMapSummaryService.getCountrySummaries(memberId));
    }
}
