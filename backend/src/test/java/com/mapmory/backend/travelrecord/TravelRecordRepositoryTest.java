package com.mapmory.backend.travelrecord;

import static org.assertj.core.api.Assertions.assertThat;

import com.mapmory.backend.IntegrationTest;
import com.mapmory.backend.member.Member;
import com.mapmory.backend.member.MemberRepository;
import com.mapmory.backend.region.Region;
import com.mapmory.backend.region.RegionRepository;
import com.mapmory.backend.region.RegionType;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("여행 기록 Repository")
class TravelRecordRepositoryTest extends IntegrationTest {

    @Autowired
    private TravelRecordRepository travelRecordRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    @DisplayName("여행 기록을 저장하고 ID로 다시 조회한다")
    void savesTravelRecord() {
        Member member = memberRepository.save(Member.of("테스터", UUID.randomUUID()));
        Region testCountry = regionRepository.save(
                Region.of(null, null, "ZZ", "테스트 국가", RegionType.COUNTRY)
        );

        TravelRecord travelRecord = travelRecordRepository.save(
                TravelRecord.of(
                        member,
                        testCountry,
                        "테스트 여행",
                        "",
                        LocalDate.of(2026, 8, 11),
                        null
                )
        );

        entityManager.flush();
        entityManager.clear();

        assertThat(travelRecord.getId()).isNotNull();
        assertThat(travelRecordRepository.findById(travelRecord.getId())).isPresent();
    }
}
