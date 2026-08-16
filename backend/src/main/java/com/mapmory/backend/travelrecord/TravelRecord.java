package com.mapmory.backend.travelrecord;

import com.mapmory.backend.common.entity.BaseEntity;
import com.mapmory.backend.member.Member;
import com.mapmory.backend.region.Region;
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
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    protected TravelRecord() {
    }

    private TravelRecord(
            Member member,
            Region region,
            String title,
            String content,
            LocalDate startDate,
            LocalDate endDate
    ) {
        this.member = member;
        this.region = region;
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public static TravelRecord of(
            Member member,
            Region region,
            String title,
            String content,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return new TravelRecord(member, region, title, content, startDate, endDate);
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Region getRegion() {
        return region;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
