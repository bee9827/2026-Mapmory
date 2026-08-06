package com.mapmory.shared.domain.model

// 도시 구분을 위한 도메인
data class Country(
    val id: Long, // 국가 구분을 위한 내부 ID
    val code: String, // 국가 ISO 코드
    val name: String, // 화면에 보여줄 국가명
)
