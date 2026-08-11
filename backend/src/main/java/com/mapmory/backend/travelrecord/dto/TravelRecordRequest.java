package com.mapmory.backend.travelrecord.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record TravelRecordRequest(
        @NotNull
        String countryCode,
        String provinceCode,
        String districtCode,
        @NotNull
        String title,
        String content,
        @NotNull
        LocalDate startDate,
        LocalDate endDate,
        List<String> objectKeys
) {
}
