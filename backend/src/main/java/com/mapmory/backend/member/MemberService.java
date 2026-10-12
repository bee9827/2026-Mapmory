package com.mapmory.backend.member;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.member.exception.MemberErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    /** 컨트롤러가 받은 Member는 영속성 컨텍스트 밖(open-in-view: false)이므로 다시 읽어 바꾼다. */
    @Transactional
    public Member rename(Long memberId, String name) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
        member.rename(name);
        return member;
    }
}
