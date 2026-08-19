package com.mapmory.shared.domain.model

// API 응답과 화면 흐름에서 사용하는 여행 기록 데이터다.
// TripRecord와 달리 백엔드의 locationId, nullable 날짜, 여러 미디어를 보존한다.
data class TripRecordData(
    val id: Long,
    val memberId: Long,
    val locationId: Long,
    val title: String,
    val content: String,
    val startDate: String?,
    val endDate: String?,
    val media: List<TripRecordMedia>,
    val createdAt: String,
    val updatedAt: String,
    val thumbnailUrl: String? = null,
)

data class TripRecordDraft(
    val locationId: Long,
    val title: String,
    val content: String,
    val startDate: String?,
    val endDate: String?,
    val mediaObjectKeys: List<String>,
)

fun TripRecordDraft.dateValidationError(): String? = when {
    startDate != null && !startDate.isValidIsoDate() -> "올바른 시작일을 입력해 주세요."
    endDate != null && !endDate.isValidIsoDate() -> "올바른 종료일을 입력해 주세요."
    startDate != null && endDate != null && endDate < startDate -> "종료일은 시작일보다 빠를 수 없습니다."
    else -> null
}

private fun String.isValidIsoDate(): Boolean {
    if (!matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return false

    val year = substring(0, 4).toInt()
    val month = substring(5, 7).toInt()
    val day = substring(8, 10).toInt()
    val daysInMonth = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28
        else -> return false
    }
    return day in 1..daysInMonth
}

data class TripRecordQuery(
    val locationId: Long? = null,
    val keyword: String? = null,
    val page: Int = 0,
    val size: Int = 20,
)

data class TripRecordPage(
    val records: List<TripRecordData>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
)
