package com.mapmory.backend.region;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.mapmory.backend.common.exception.BusinessException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegionResolverTest {

    @Mock
    private RegionRepository regionRepository;

    @InjectMocks
    private RegionResolver regionResolver;

    @Test
    void findsCountry() {
        Region korea = mock(Region.class);
        when(regionRepository.findByParentIsNullAndRegionTypeAndRegionCode(RegionType.COUNTRY, "KR"))
                .thenReturn(Optional.of(korea));

        assertThat(regionResolver.findCountry("KR")).isEqualTo(korea);
    }

    @Test
    void rejectsNonexistentCountry() {
        when(regionRepository.findByParentIsNullAndRegionTypeAndRegionCode(RegionType.COUNTRY, "ZZ"))
                .thenReturn(Optional.empty());

        assertError(() -> regionResolver.findCountry("ZZ"), "COUNTRY_NOT_FOUND");
    }

    @Test
    void distinguishesMissingChildFromInvalidHierarchy() {
        Region korea = parent(1L);
        when(regionRepository.findByParentIdAndRegionTypeAndRegionCode(1L, RegionType.PROVINCE, "99"))
                .thenReturn(Optional.empty());
        when(regionRepository.existsByRegionTypeAndRegionCode(RegionType.PROVINCE, "99")).thenReturn(false);

        assertError(() -> regionResolver.findProvince(korea, "99"), "REGION_NOT_FOUND");

        when(regionRepository.findByParentIdAndRegionTypeAndRegionCode(1L, RegionType.PROVINCE, "49"))
                .thenReturn(Optional.empty());
        when(regionRepository.existsByRegionTypeAndRegionCode(RegionType.PROVINCE, "49")).thenReturn(true);

        assertError(() -> regionResolver.findProvince(korea, "49"), "INVALID_REGION_HIERARCHY");
    }

    @Test
    void resolvesDistrictByItsDirectParent() {
        Region jeju = parent(2L);
        Region jejuCity = mock(Region.class);
        when(regionRepository.findByParentIdAndRegionTypeAndRegionCode(2L, RegionType.DISTRICT, "50110"))
                .thenReturn(Optional.of(jejuCity));

        assertThat(regionResolver.findDistrict(jeju, "50110")).isEqualTo(jejuCity);
    }

    private Region parent(Long id) {
        Region region = mock(Region.class);
        when(region.getId()).thenReturn(id);
        return region;
    }

    private void assertError(Runnable action, String errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getErrorCode().code())
                .isEqualTo(errorCode);
    }
}
