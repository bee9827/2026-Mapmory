package com.mapmory.shared.domain.model

// 위치를 저장하기 위한 도메인
data class Location(
    val id: Long, // 지역 자체의 내부 ID
    val countryId: Long, // 이 지역이 속한 국가의 ID
    val parentId: Long?, // DISTRICT가 속한 PROVINCE
    val regionCode: String, // 지도 GeoJSON 데이터와 매칭할 지역 코드
    val name: String, // 지역명(ex) 서울특별시, 강남구)
    val type: LocationType, // 지역 단계
)

enum class LocationType {
    PROVINCE, // 시, 도 단위(서울특별시, 경기도)
    DISTRICT, // 시, 군, 구 단위(강남구, 수원시)
}
