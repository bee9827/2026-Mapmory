package com.mapmory.backend.place.application.model;

import com.mapmory.backend.region.Region;

/**
 * 서버가 장소에 추천한 지역의 국가, 시·도, 시·군·구. 국가만 찾았으면 province와 district가 null이다.
 *
 * <p>장소 선택 API는 트랜잭션 밖에서 응답을 만들므로(open-in-view: false) 엔티티 대신 값을 넘긴다.
 */
public record SuggestedRegion(Area country, Area province, Area district) {

    public static SuggestedRegion from(Region region) {
        return switch (region.getRegionType()) {
            case COUNTRY -> new SuggestedRegion(Area.from(region), null, null);
            case PROVINCE -> new SuggestedRegion(Area.from(region.getRoot()), Area.from(region), null);
            case DISTRICT -> new SuggestedRegion(
                    Area.from(region.getRoot()), Area.from(region.getParent()), Area.from(region));
        };
    }

    public record Area(String code, String name) {

        static Area from(Region region) {
            return new Area(region.getRegionCode(), region.getName());
        }
    }
}
