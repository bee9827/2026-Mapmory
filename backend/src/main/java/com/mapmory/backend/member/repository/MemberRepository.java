package com.mapmory.backend.member.repository;

import com.mapmory.backend.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
