package com.mapmory.shared.domain.model

// 저장된 여행 기록을 나타내는 도메인
data class TravelRecord(
    val id: Long, // 여행 기록의 내부 ID
    val memberId: Long, // 기록을 작성한 회원의 ID
    val locationId: Long, // 기록한 최종 행정구역의 ID
    val title: String, // 기록 제목
    val content: String, // 기록 본문
    val startDate: String?, // 여행 시작일. 날짜를 입력하지 않으면 null
    val endDate: String?, // 여행 종료일. 날짜를 입력하지 않으면 null
    val media: List<RecordMedia>, // 기록에 첨부된 사진 목록
    val createdAt: String, // 기록 생성 시각
    val updatedAt: String, // 기록 최종 수정 시각
)

// 여행 기록을 새로 만들거나 수정할 때 입력하는 값
data class TravelRecordDraft(
    val locationId: Long, // 기록할 최종 행정구역의 ID
    val title: String, // 기록 제목
    val content: String, // 기록 본문
    val startDate: String?, // 여행 시작일
    val endDate: String?, // 여행 종료일
    val mediaObjectKeys: List<String>, // 첨부할 이미지 저장소 경로 목록
)

// 여행 기록 목록을 조회할 때 사용하는 조건
data class TravelRecordQuery(
    val locationId: Long? = null, // 특정 지역으로 필터링할 때 사용하는 지역 ID
    val keyword: String? = null, // 제목과 본문 검색어
    val page: Int = 0, // 0부터 시작하는 페이지 번호
    val size: Int = 20, // 한 페이지에 가져올 기록 수
)

// 여행 기록 목록의 페이지 응답
data class TravelRecordPage(
    val records: List<TravelRecord>, // 현재 페이지의 여행 기록
    val page: Int, // 현재 페이지 번호
    val size: Int, // 페이지 크기
    val totalElements: Long, // 전체 여행 기록 수
    val totalPages: Int, // 전체 페이지 수
)
