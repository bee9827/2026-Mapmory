package com.mapmory.backend.waitlist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mapmory.backend.waitlist.dto.LaunchWaitlistRequest;
import com.mapmory.backend.waitlist.dto.LaunchWaitlistResponse;
import com.mapmory.backend.waitlist.dto.LaunchWaitlistStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class LaunchWaitlistServiceTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-08-25T10:00:00Z"),
            ZoneOffset.UTC
    );

    @Mock
    private LaunchWaitlistRepository repository;

    private LaunchWaitlistService service;

    @BeforeEach
    void setUp() {
        service = new LaunchWaitlistService(repository, CLOCK);
    }

    @Test
    void 이메일을_정규화해_출시_알림을_신청한다() {
        LaunchWaitlistRequest request = request("  MapMory.User@Example.COM  ");

        LaunchWaitlistResponse response = service.subscribe(request);

        assertThat(response.status()).isEqualTo(LaunchWaitlistStatus.SUBSCRIBED);
        ArgumentCaptor<LaunchWaitlistEntry> captor = ArgumentCaptor.forClass(LaunchWaitlistEntry.class);
        verify(repository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("mapmory.user@example.com");
        assertThat(captor.getValue().getConsentedAt()).isEqualTo("2026-08-25T10:00:00");
    }

    @Test
    void 이미_등록된_이메일은_다시_저장하지_않는다() {
        when(repository.existsByEmail("user@example.com")).thenReturn(true);

        LaunchWaitlistResponse response = service.subscribe(request("USER@example.com"));

        assertThat(response.status()).isEqualTo(LaunchWaitlistStatus.ALREADY_SUBSCRIBED);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void 동시에_같은_이메일이_저장되어도_중복_신청으로_응답한다() {
        when(repository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        LaunchWaitlistResponse response = service.subscribe(request("user@example.com"));

        assertThat(response.status()).isEqualTo(LaunchWaitlistStatus.ALREADY_SUBSCRIBED);
    }

    private LaunchWaitlistRequest request(String email) {
        return new LaunchWaitlistRequest(email, true, true);
    }
}
