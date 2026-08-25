package com.mapmory.backend.waitlist;

import com.mapmory.backend.waitlist.dto.LaunchWaitlistRequest;
import com.mapmory.backend.waitlist.dto.LaunchWaitlistResponse;
import java.text.Normalizer;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LaunchWaitlistService {

    private final LaunchWaitlistRepository repository;
    private final Clock clock;

    public LaunchWaitlistService(LaunchWaitlistRepository repository) {
        this(repository, Clock.systemUTC());
    }

    LaunchWaitlistService(LaunchWaitlistRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public LaunchWaitlistResponse subscribe(LaunchWaitlistRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (repository.existsByEmail(normalizedEmail)) {
            return LaunchWaitlistResponse.alreadySubscribed();
        }

        LaunchWaitlistEntry entry = LaunchWaitlistEntry.of(
                normalizedEmail,
                LocalDateTime.now(clock)
        );
        try {
            repository.saveAndFlush(entry);
            return LaunchWaitlistResponse.subscribed();
        } catch (DataIntegrityViolationException exception) {
            // 동시에 같은 주소가 들어온 경우에도 한 번만 저장하고 성공으로 취급한다.
            return LaunchWaitlistResponse.alreadySubscribed();
        }
    }

    static String normalizeEmail(String email) {
        return Normalizer.normalize(email.trim(), Normalizer.Form.NFKC)
                .toLowerCase(Locale.ROOT);
    }
}
