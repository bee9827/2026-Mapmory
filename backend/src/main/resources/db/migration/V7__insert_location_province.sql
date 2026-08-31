-- 대한민국 시·도 (ISO 3166-2)
--
-- location.id 를 명시적으로 1~17로 고정한다.
-- V8(시·군·구)에서 parent_id 로 참조하기 위함이다.
--
-- [결정 사항] 2026-07-01 전남광주통합특별시 출범으로 광주광역시와 전라남도가 통합되었으나,
--   지도 화면에서 두 지역이 별개 도형으로 렌더링되므로 데이터도 분리해 유지한다.
--   통합 후 ISO 3166-2가 아직 갱신되지 않아 기존 코드(29 / 46)를 그대로 사용한다.
--
-- TODO 표시 명칭은 기획/디자인과 최종 확정 필요 (광주광역시 / 전라남도 vs 통합 명칭)
SET @kr := (SELECT id FROM country WHERE code = 'KR');

INSERT INTO location (id, country_id, parent_id, region_code, name, location_type) VALUES
                                                                                       ( 1, @kr, NULL, '11', '서울특별시',     'PROVINCE'),
                                                                                       ( 2, @kr, NULL, '26', '부산광역시',     'PROVINCE'),
                                                                                       ( 3, @kr, NULL, '27', '대구광역시',     'PROVINCE'),
                                                                                       ( 4, @kr, NULL, '28', '인천광역시',     'PROVINCE'),
                                                                                       ( 5, @kr, NULL, '29', '광주광역시',     'PROVINCE'),
                                                                                       ( 6, @kr, NULL, '30', '대전광역시',     'PROVINCE'),
                                                                                       ( 7, @kr, NULL, '31', '울산광역시',     'PROVINCE'),
                                                                                       ( 8, @kr, NULL, '41', '경기도',         'PROVINCE'),
                                                                                       ( 9, @kr, NULL, '42', '강원특별자치도', 'PROVINCE'),
                                                                                       (10, @kr, NULL, '43', '충청북도',       'PROVINCE'),
                                                                                       (11, @kr, NULL, '44', '충청남도',       'PROVINCE'),
                                                                                       (12, @kr, NULL, '45', '전북특별자치도', 'PROVINCE'),
                                                                                       (13, @kr, NULL, '46', '전라남도',       'PROVINCE'),
                                                                                       (14, @kr, NULL, '47', '경상북도',       'PROVINCE'),
                                                                                       (15, @kr, NULL, '48', '경상남도',       'PROVINCE'),
                                                                                       (16, @kr, NULL, '49', '제주특별자치도', 'PROVINCE'),
                                                                                       (17, @kr, NULL, '50', '세종특별자치시', 'PROVINCE');