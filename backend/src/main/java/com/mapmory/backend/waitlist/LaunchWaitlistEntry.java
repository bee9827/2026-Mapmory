package com.mapmory.backend.waitlist;

import com.mapmory.backend.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "launch_waitlist",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_launch_waitlist_email",
                columnNames = "email"
        )
)
public class LaunchWaitlistEntry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "consented_at", nullable = false, updatable = false)
    private LocalDateTime consentedAt;

    protected LaunchWaitlistEntry() {
    }

    private LaunchWaitlistEntry(String email, LocalDateTime consentedAt) {
        this.email = email;
        this.consentedAt = consentedAt;
    }

    public static LaunchWaitlistEntry of(String normalizedEmail, LocalDateTime consentedAt) {
        return new LaunchWaitlistEntry(normalizedEmail, consentedAt);
    }

    public Long getId() {
        return id;
    }

    String getEmail() {
        return email;
    }

    LocalDateTime getConsentedAt() {
        return consentedAt;
    }
}
