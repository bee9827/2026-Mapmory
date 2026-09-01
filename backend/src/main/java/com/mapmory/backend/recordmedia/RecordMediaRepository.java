package com.mapmory.backend.recordmedia;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * RecordMedia는 TravelRecord 애그리거트 내부 엔티티이므로 쓰기는 루트를 통한다.
 * 여기에는 목록 화면의 썸네일 일괄 조회만 남긴다. (ADR 0017)
 */
public interface RecordMediaRepository extends JpaRepository<RecordMedia, Long> {

    List<RecordMedia> findByTravelRecordIdInOrderByTravelRecordIdAscSortOrderAscIdAsc(
            Collection<Long> travelRecordIds
    );
}
