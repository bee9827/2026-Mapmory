package com.mapmory.backend.travelrecord;

import com.mapmory.backend.common.entity.BaseEntity;
import com.mapmory.backend.country.Country;
import com.mapmory.backend.location.Location;
import com.mapmory.backend.member.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "travel_record")
public class TravelRecord extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @ManyToOne(optional = true)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String content;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    protected TravelRecord() {
    }

    private TravelRecord(
            Member member,
            Country country,
            Location location,
            String title,
            String content,
            LocalDate startDate,
            LocalDate endDate
    ) {
        this.member = member;
        this.country = country;
        this.location = location;
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static TravelRecord of(
            Member member,
            Country country,
            Location location,
            String title,
            String content,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return new TravelRecord(member, country, location, title, content, startDate, endDate);
    }

}
