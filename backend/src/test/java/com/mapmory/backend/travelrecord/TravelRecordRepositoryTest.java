package com.mapmory.backend.travelrecord;

import static org.assertj.core.api.Assertions.assertThat;

import com.mapmory.backend.member.Member;
import com.mapmory.backend.member.MemberRepository;
import com.mapmory.backend.region.Region;
import com.mapmory.backend.region.RegionRepository;
import com.mapmory.backend.region.RegionType;
import com.mapmory.backend.support.MySqlTestContainerConfig;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@DataJpaTest
@Import(MySqlTestContainerConfig.class)
class TravelRecordRepositoryTest {

    @Autowired
    private TravelRecordRepository travelRecordRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
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

    @Test
    void findsTravelRecordsByMemberWithPagination() {
        Member member = memberRepository.save(
                Member.of("테스터", UUID.randomUUID())
        );

        Region country = regionRepository.save(
                Region.of(null, null, "XX", "테스트 국가", RegionType.COUNTRY)
        );

        travelRecordRepository.save(
                TravelRecord.of(
                        member,
                        country,
                        "첫 번째 기록",
                        "",
                        LocalDate.of(2026, 8, 10),
                        null
                )
        );

        Page<TravelRecord> result = travelRecordRepository.findByMemberId(
                member.getId(),
                PageRequest.of(
                        0, // 첫 페이지
                        20, // 최대 20개
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.hasNext()).isFalse();
    }
}
