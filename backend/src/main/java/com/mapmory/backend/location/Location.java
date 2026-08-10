package com.mapmory.backend.location;

import com.mapmory.backend.common.entity.BaseEntity;
import com.mapmory.backend.country.Country;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "location",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_location_country_region_code",
                columnNames = {"country_id", "region_code"}
        )
)
public class Location extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Location parent;

    @Column(name = "region_code", nullable = false, length = 20)
    private String regionCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false, length = 20)
    private LocationType locationType;

    protected Location() {
    }

    private Location(
            Country country,
            Location parent,
            String regionCode,
            String name,
            LocationType locationType
    ) {
        this.country = country;
        this.parent = parent;
        this.regionCode = regionCode;
        this.name = name;
        this.locationType = locationType;
    }

    public static Location of(
            Country country,
            Location parent,
            String regionCode,
            String name,
            LocationType locationType
    ) {
        return new Location(country, parent, regionCode, name, locationType);
    }

    public Long getId() {
        return id;
    }
}
