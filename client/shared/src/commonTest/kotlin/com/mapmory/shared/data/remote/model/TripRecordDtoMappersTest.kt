package com.mapmory.shared.data.remote.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ApiDtoMappersTest {
    @Test
    fun `여행_기록_상세는_미디어와_날짜를_매핑한다`() {
        val result = TripRecordDetailDto(
            id = 101,
            member = MemberSummaryDto(10, "맵모리"),
            location = LocationSummaryDto(1, "KR", "11110", "서울특별시 종로구"),
            title = "비 오는 날의 종로",
            content = "골목을 걸었다.",
            startDate = "2026-07-10",
            endDate = "2026-07-12",
            media = listOf(TripRecordMediaDto(1001, "travel-records/10/a.jpg", "https://example.com/a", 300, 0)),
            createdAt = "2026-07-13T09:30:00+09:00",
            updatedAt = "2026-07-13T10:15:00+09:00",
        ).toDomain()

        assertEquals(101, result.id)
        assertEquals(1, result.locationId)
        assertEquals("2026-07-12", result.endDate)
        assertEquals("https://example.com/a", result.media.single().url)
    }

    @Test
    fun `초안은_Object_Key를_포함한_요청으로_변환된다`() {
        val result = TripRecordRequestDto(
            locationId = 1,
            title = "제목",
            content = "본문",
            startDate = null,
            endDate = null,
            objectKeys = listOf("travel-records/10/a.jpg"),
        ).toDraft()

        assertEquals(listOf("travel-records/10/a.jpg"), result.mediaObjectKeys)
        assertEquals(null, result.startDate)
    }
}
