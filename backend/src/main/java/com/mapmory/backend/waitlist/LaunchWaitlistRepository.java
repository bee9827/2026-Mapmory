package com.mapmory.backend.waitlist;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LaunchWaitlistRepository extends JpaRepository<LaunchWaitlistEntry, Long> {

    boolean existsByEmail(String email);
}
