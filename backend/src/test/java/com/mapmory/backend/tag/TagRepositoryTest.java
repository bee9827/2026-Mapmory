package com.mapmory.backend.tag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mapmory.backend.member.Member;
import com.mapmory.backend.member.MemberRepository;
import com.mapmory.backend.support.MySqlTestContainerSupport;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class TagRepositoryTest extends MySqlTestContainerSupport {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void 대소문자만_다른_이름은_같은_태그로_판단한다() {
        Member member = memberRepository.save(Member.of("태그 회원", UUID.randomUUID()));
        tagRepository.saveAndFlush(Tag.of(member, "Cafe"));

        assertThatThrownBy(() -> tagRepository.saveAndFlush(Tag.of(member, "cafe")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 악센트가_다른_이름은_서로_다른_태그로_저장한다() {
        Member member = memberRepository.save(Member.of("악센트 태그 회원", UUID.randomUUID()));
        tagRepository.saveAndFlush(Tag.of(member, "cafe"));
        tagRepository.saveAndFlush(Tag.of(member, "café"));

        assertThat(tagRepository.findAllByMemberIdOrderByCreatedAtAscIdAsc(member.getId()))
                .extracting(Tag::getName)
                .containsExactly("cafe", "café");
    }

    @Test
    void 임베디드_이름의_비교_키로_중복을_조회한다() {
        Member member = memberRepository.save(Member.of("태그 중복 조회 회원", UUID.randomUUID()));
        Tag tag = tagRepository.saveAndFlush(Tag.of(member, "Cafe"));

        assertThat(tagRepository.existsByMemberIdAndNameKey(member.getId(), "cafe")).isTrue();
        assertThat(tagRepository.existsByMemberIdAndNameKeyAndIdNot(
                member.getId(),
                "cafe",
                tag.getId()
        )).isFalse();
    }

    @Test
    void 소문자_변환으로_늘어난_비교_키를_저장한다() {
        Member member = memberRepository.save(Member.of("확장 태그 키 회원", UUID.randomUUID()));
        String displayName = "İ".repeat(30);

        Tag tag = tagRepository.saveAndFlush(Tag.of(member, displayName));

        assertThat(tag.getName()).isEqualTo(displayName);
    }
}
