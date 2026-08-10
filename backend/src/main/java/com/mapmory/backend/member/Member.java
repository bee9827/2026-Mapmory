package com.mapmory.backend.member;

import com.mapmory.backend.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "member")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true)
    private UUID uuid;

    protected Member() {
    }

    private Member(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    public static Member of(String name, UUID uuid) {
        return new Member(name, uuid);
    }

    public Long getId() {
        return id;
    }
}
