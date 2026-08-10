package com.mapmory.backend.mapsummary.api;

import com.mapmory.backend.common.ApiResponse;
import com.mapmory.backend.mapsummary.application.MapSummaryService;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/travel-records/map-summary")
public class MapSummaryController {

    private final MapSummaryService mapSummaryService;

    public MapSummaryController(MapSummaryService mapSummaryService) {
        this.mapSummaryService = mapSummaryService;
    }

    @GetMapping("/countries")
    public ApiResponse<List<CountryMapSummaryResponse>> summarizeCountries(
            @RequestHeader("X-Member-Id") @Positive Long memberId
    ) {
        return new ApiResponse<>(mapSummaryService.summarizeCountries(memberId));
    }

    @GetMapping("/countries/{countryCode}/regions")
    public ApiResponse<List<RegionMapSummaryResponse>> summarizeTopLevelRegions(
            @RequestHeader("X-Member-Id") @Positive Long memberId,
            @PathVariable @Pattern(regexp = "[A-Z]{2}") String countryCode
    ) {
        return new ApiResponse<>(
                mapSummaryService.summarizeTopLevelRegions(memberId, countryCode)
        );
    }

    @GetMapping("/countries/{countryCode}/regions/{parentLocationCode}/children")
    public ApiResponse<List<RegionMapSummaryResponse>> summarizeChildren(
            @RequestHeader("X-Member-Id") @Positive Long memberId,
            @PathVariable @Pattern(regexp = "[A-Z]{2}") String countryCode,
            @PathVariable String parentLocationCode
    ) {
        return new ApiResponse<>(mapSummaryService.summarizeChildren(
                memberId,
                countryCode,
                parentLocationCode
        ));
    }
}
