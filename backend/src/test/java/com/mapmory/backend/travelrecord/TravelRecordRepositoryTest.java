package com.mapmory.backend.travelrecord;

import static org.assertj.core.api.Assertions.assertThat;

import com.mapmory.backend.member.Member;
import com.mapmory.backend.member.MemberRepository;
import com.mapmory.backend.recordmedia.RecordMedia;
import com.mapmory.backend.recordmedia.RecordMediaRepository;
import com.mapmory.backend.region.Region;
import com.mapmory.backend.region.RegionRepository;
import com.mapmory.backend.region.RegionType;
import com.mapmory.backend.support.MySqlTestContainerConfig;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.UUID;
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
    private RecordMediaRepository recordMediaRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 여행_일지를_저장한다() {
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
    void 회원의_여행_일지를_페이지로_조회한다() {
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

    @Test
    void 소유한_일지와_정렬된_미디어를_조회한다() {
        Member owner = memberRepository.save(Member.of("작성자", UUID.randomUUID()));
        Member otherMember = memberRepository.save(Member.of("다른 회원", UUID.randomUUID()));
        Region country = regionRepository.save(
                Region.of(null, null, "YY", "상세 조회 국가", RegionType.COUNTRY)
        );
        TravelRecord travelRecord = travelRecordRepository.save(
                TravelRecord.of(
                        owner,
                        country,
                        "상세 조회 기록",
                        "본문",
                        LocalDate.of(2026, 8, 11),
                        null
                )
        );
        recordMediaRepository.save(RecordMedia.of(travelRecord, "mapmory/detail/b.jpg", null, 1));
        recordMediaRepository.save(RecordMedia.of(travelRecord, "mapmory/detail/a.jpg", null, 0));

        entityManager.flush();
        entityManager.clear();

        assertThat(travelRecordRepository.findByIdAndMemberId(travelRecord.getId(), owner.getId()))
                .isPresent();
        assertThat(travelRecordRepository.findByIdAndMemberId(travelRecord.getId(), otherMember.getId()))
                .isEmpty();
        assertThat(recordMediaRepository.findByTravelRecordIdOrderBySortOrderAsc(travelRecord.getId()))
                .extracting(RecordMedia::getObjectKey)
                .containsExactly("mapmory/detail/a.jpg", "mapmory/detail/b.jpg");
        assertThat(recordMediaRepository.findByObjectKeyIn(
                java.util.List.of("mapmory/detail/a.jpg")
        ))
                .extracting(RecordMedia::getObjectKey)
                .containsExactly("mapmory/detail/a.jpg");
    }
}
