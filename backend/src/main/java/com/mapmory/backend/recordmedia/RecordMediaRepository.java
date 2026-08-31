package com.mapmory.backend.recordmedia;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecordMediaRepository extends JpaRepository<RecordMedia, Long> {

    List<RecordMedia> findByTravelRecordIdOrderBySortOrderAsc(Long travelRecordId);

    List<RecordMedia> findByTravelRecordIdInOrderByTravelRecordIdAscSortOrderAscIdAsc(
            Collection<Long> travelRecordIds
    );

    @Query("""
            SELECT rm
            FROM RecordMedia rm
            WHERE rm.objectKey.value IN :objectKeys
            """)
    List<RecordMedia> findByObjectKeyIn(@Param("objectKeys") Collection<String> objectKeys);
}
