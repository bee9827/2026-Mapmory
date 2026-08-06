package com.mapmory.shared.domain.model

// 여행 기록에 첨부된 사진 한 장을 나타내는 도메인
data class RecordMedia(
    val id: Long, // 첨부 사진의 내부 ID
    val objectKey: String, // 이미지 저장소에 영구적으로 저장된 경로
    val sortOrder: Int, // 기록 화면에서 사진을 보여줄 순서. 0이 첫 번째
    val url: String?, // 서버가 objectKey로 생성한 임시 조회 URL
)
