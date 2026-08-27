package com.mapmory.backend.recordmedia;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordMediaRepository extends JpaRepository<RecordMedia, Long> {

    List<RecordMedia> findByTravelRecordIdOrderBySortOrderAsc(Long travelRecordId);

    List<RecordMedia> findByTravelRecordIdInAndSortOrder(
            Collection<Long> travelRecordIds,
            int sortOrder
    );

    List<RecordMedia> findByObjectKeyIn(Collection<String> objectKeys);
}
