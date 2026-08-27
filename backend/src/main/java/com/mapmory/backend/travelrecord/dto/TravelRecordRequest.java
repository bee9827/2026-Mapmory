package com.mapmory.backend.travelrecord.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record TravelRecordRequest(
        @NotNull
        String countryCode,
        String provinceCode,
        String districtCode,
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        String title,
        String content,
        @NotNull
        LocalDate startDate,
        LocalDate endDate,
        List<String> objectKeys,
        List<Long> tagIds
) {
    public TravelRecordRequest(
            String countryCode,
            String provinceCode,
            String districtCode,
            String title,
            String content,
            LocalDate startDate,
            LocalDate endDate,
            List<String> objectKeys
    ) {
        this(countryCode, provinceCode, districtCode, title, content, startDate, endDate, objectKeys, List.of());
    }
}
