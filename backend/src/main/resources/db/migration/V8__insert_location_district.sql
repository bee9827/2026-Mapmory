-- 대한민국 시·군·구 (행정표준코드)
--
-- [주의] region_code 접두사와 상위 시·도의 ISO 코드는 일치하지 않는다.
--   세종  : 시·도 ISO 50  /  시·군·구 36110
--   제주  : 시·도 ISO 49  /  시·군·구 50110, 50130
--   강원  : 시·도 ISO 42  /  시·군·구 51xxx
--   전북  : 시·도 ISO 45  /  시·군·구 52xxx
--   광주·전남 : 시·도 ISO 29 / 46  /  시·군·구 12xxx (통합 코드)
-- 계층은 반드시 parent_id 로 판단한다.
SET @kr := (SELECT id FROM country WHERE code = 'KR');

-- 서울특별시 (parent 1)
INSERT INTO location (country_id, parent_id, region_code, name, location_type) VALUES
                                                                                   (@kr,  1, '11110', '종로구',     'DISTRICT'),
                                                                                   (@kr,  1, '11140', '중구',       'DISTRICT'),
                                                                                   (@kr,  1, '11170', '용산구',     'DISTRICT'),
                                                                                   (@kr,  1, '11200', '성동구',     'DISTRICT'),
                                                                                   (@kr,  1, '11215', '광진구',     'DISTRICT'),
                                                                                   (@kr,  1, '11230', '동대문구',   'DISTRICT'),
                                                                                   (@kr,  1, '11260', '중랑구',     'DISTRICT'),
                                                                                   (@kr,  1, '11290', '성북구',     'DISTRICT'),
                                                                                   (@kr,  1, '11305', '강북구',     'DISTRICT'),
                                                                                   (@kr,  1, '11320', '도봉구',     'DISTRICT'),
                                                                                   (@kr,  1, '11350', '노원구',     'DISTRICT'),
                                                                                   (@kr,  1, '11380', '은평구',     'DISTRICT'),
                                                                                   (@kr,  1, '11410', '서대문구',   'DISTRICT'),
                                                                                   (@kr,  1, '11440', '마포구',     'DISTRICT'),
                                                                                   (@kr,  1, '11470', '양천구',     'DISTRICT'),
                                                                                   (@kr,  1, '11500', '강서구',     'DISTRICT'),
                                                                                   (@kr,  1, '11530', '구로구',     'DISTRICT'),
                                                                                   (@kr,  1, '11545', '금천구',     'DISTRICT'),
                                                                                   (@kr,  1, '11560', '영등포구',   'DISTRICT'),
                                                                                   (@kr,  1, '11590', '동작구',     'DISTRICT'),
                                                                                   (@kr,  1, '11620', '관악구',     'DISTRICT'),
                                                                                   (@kr,  1, '11650', '서초구',     'DISTRICT'),
                                                                                   (@kr,  1, '11680', '강남구',     'DISTRICT'),
                                                                                   (@kr,  1, '11710', '송파구',     'DISTRICT'),
                                                                                   (@kr,  1, '11740', '강동구',     'DISTRICT');

-- 부산광역시 (parent 2)
INSERT INTO location (country_id, parent_id, region_code, name, location_type) VALUES
                                                                                   (@kr,  2, '26110', '중구',       'DISTRICT'),
                                                                                   (@kr,  2, '26140', '서구',       'DISTRICT'),
                                                                                   (@kr,  2, '26170', '동구',       'DISTRICT'),
                                                                                   (@kr,  2, '26200', '영도구',     'DISTRICT'),
                                                                                   (@kr,  2, '26230', '부산진구',   'DISTRICT'),
                                                                                   (@kr,  2, '26260', '동래구',     'DISTRICT'),
                                                                                   (@kr,  2, '26290', '남구',       'DISTRICT'),
                                                                                   (@kr,  2, '26320', '북구',       'DISTRICT'),
                                                                                   (@kr,  2, '26350', '해운대구',   'DISTRICT'),
                                                                                   (@kr,  2, '26380', '사하구',     'DISTRICT'),
                                                                                   (@kr,  2, '26410', '금정구',     'DISTRICT'),
                                                                                   (@kr,  2, '26440', '강서구',     'DISTRICT'),
                                                                                   (@kr,  2, '26470', '연제구',     'DISTRICT'),
                                                                                   (@kr,  2, '26500', '수영구',     'DISTRICT'),
                                                                                   (@kr,  2, '26530', '사상구',     'DISTRICT'),
                                                                                   (@kr,  2, '26710', '기장군',     'DISTRICT');

-- 대구광역시 (parent 3)
INSERT INTO location (country_id, parent_id, region_code, name, location_type) VALUES
                                                                                   (@kr,  3, '27110', '중구',       'DISTRICT'),
                                                                                   (@kr,  3, '27140', '동구',       'DISTRICT'),
                                                                                   (@kr,  3, '27170', '서구',       'DISTRICT'),
                                                                                   (@kr,  3, '27200', '남구',       'DISTRICT'),
                                                                                   (@kr,  3, '27230', '북구',       'DISTRICT'),
                                                                                   (@kr,  3, '27260', '수성구',     'DISTRICT'),
                                                                                   (@kr,  3, '27290', '달서구',     'DISTRICT'),
                                                                                   (@kr,  3, '27710', '달성군',     'DISTRICT'),
                                                                                   (@kr,  3, '27720', '군위군',     'DISTRICT');

-- 인천광역시 (parent 4)
INSERT INTO location (country_id, parent_id, region_code, name, location_type) VALUES
                                                                                   (@kr,  4, '28125', '제물포구',   'DISTRICT'),
                                                                                   (@kr,  4, '28155', '영종구',     'DISTRICT'),
                                                                                   (@kr,  4, '28177', '미추홀구',   'DISTRICT'),
                                                                                   (@kr,  4, '28185', '연수구',     'DISTRICT'),
                                                                                   (@kr,  4, '28200', '남동구',     'DISTRICT'),
                                                                                   (@kr,  4, '28237', '부평구',     'DISTRICT'),
                                                                                   (@kr,  4, '28245', '계양구',     'DISTRICT'),
                                                                                   (@kr,  4, '28275', '서해구',     'DISTRICT'),
                                                                                   (@kr,  4, '28290', '검단구',     'DISTRICT'),
                                                                                   (@kr,  4, '28710', '강화군',     'DISTRICT'),
                                                                                   (@kr,  4, '28720', '옹진군',     'DISTRICT');

-- 광주광역시 (parent 5) — 통합 코드 12xxx 사용
INSERT INTO location (country_id, parent_id, region_code, name, location_type) VALUES
                                                                                   (@kr,  5, '12210', '동구',       'DISTRICT'),
                                                                                   (@kr,  5, '12240', '서구',       'DISTRICT'),
                                                                                   (@kr,  5, '12270', '남구',       'DISTRICT'),
                                                                                   (@kr,  5, '12300', '북구',       'DISTRICT'),
                                                                                   (@kr,  5, '12330', '광산구',     'DISTRICT');

-- 대전광역시 (parent 6)
INSERT INTO location (country_id, parent_id, region_code, name, location_type) VALUES
                                                                                   (@kr,  6, '30110', '동구',       'DISTRICT'),
                                                                                   (@kr,  6, '30140', '중구',       'DISTRICT'),
                                                                                   (@kr,  6, '30170', '서구',       'DISTRICT'),
                                                                                   (@kr,  6, '30200', '유성구',     'DISTRICT'),
                                                                                   (@kr,  6, '30230', '대덕구',     'DISTRICT');

-- 울산광역시 (parent 7)
INSERT INTO location (country_id, parent_id, region_code, name, location_type) VALUES
                                                                                   (@kr,  7, '31110', '중구',       'DISTRICT'),
                                                                                   (@kr,  7, '31140', '남구',       'DISTRICT'),
                                                                                   (@kr,  7, '31170', '동구',       'DISTRICT'),
                                                                                   (@kr,  7, '31200', '북구',       'DISTRICT'),
                                                                                   (@kr,  7, '31710', '울주군',     'DISTRICT');

-- 경기도 (parent 8)
INSERT INTO location (country_id, parent_id, region_code, name, location_type) VALUES
                                                                                   (@kr,  8, '41111', '수원시 장안구',   'DISTRICT'),
                                                                                   (@kr,  8, '41113', '수원시 권선구',   'DISTRICT'),
                                                                                   (@kr,  8, '41115', '수원시 팔달구',   'DISTRICT'),
                                                                                   (@kr,  8, '41117', '수원시 영통구',   'DISTRICT'),
                                                                                   (@kr,  8, '41131', '성남시 수정구',   'DISTRICT'),
                                                                                   (@kr,  8, '41133', '성남시 중원구',   'DISTRICT'),
                                                                                   (@kr,  8, '41135', '성남시 분당구',   'DISTRICT'),
                                                                                   (@kr,  8, '41150', '의정부시',        'DISTRICT'),
                                                                                   (@kr,  8, '41171', '안양시 만안구',   'DISTRICT'),
                                                                                   (@kr,  8, '41173', '안양시 동안구',   'DISTRICT'),
                                                                                   (@kr,  8, '41192', '부천시 원미구',   'DISTRICT'),
                                                                                   (@kr,  8, '41194', '부천시 소사구',   'DISTRICT'),
                                                                                   (@kr,  8, '41196', '부천시 오정구',   'DISTRICT'),
                                                                                   (@kr,  8, '41210', '광명시',          'DISTRICT'),
                                                                                   (@kr,  8, '41220', '평택시',          'DISTRICT'),
                                                                                   (@kr,  8, '41250', '동두천시',        'DISTRICT'),
                                                                                   (@kr,  8, '41271', '안산시 상록구',   'DISTRICT'),
                                                                                   (@kr,  8, '41273', '안산시 단원구',   'DISTRICT'),
                                                                                   (@kr,  8, '41281', '고양시 덕양구',   'DISTRICT'),
                                                                                   (@kr,  8, '41285', '고양시 일산동구', 'DISTRICT'),
                                                                                   (@kr,  8, '41287', '고양시 일산서구', 'DISTRICT'),
                                                                                   (@kr,  8, '41290', '과천시',          'DISTRICT'),
                                                                                   (@kr,  8, '41310', '구리시',          'DISTRICT'),
                                                                                   (@kr,  8, '41360', '남양주시',        'DISTRICT'),
                                                                                   (@kr,  8, '41370', '오산시',          'DISTRICT'),
                                                                                   (@kr,  8, '41390', '시흥시',          'DISTRICT'),
                                                                                   (@kr,  8, '41410', '군포시',          'DISTRICT'),
                                                                                   (@kr,  8, '41430', '의왕시',          'DISTRICT'),
                                                                                   (@kr,  8, '41450', '하남시',          'DISTRICT'),
                                                                                   (@kr,  8, '41461', '용인시 처인구',   'DISTRICT'),
                                                                                   (@kr,  8, '41463', '용인시 기흥구',   'DISTRICT'),
                                                                                   (@kr,  8, '41465', '용인시 수지구',   'DISTRICT'),
                                                                                   (@kr,  8, '41480', '파주시',          'DISTRICT'),
                                                                                   (@kr,  8, '41500', '이천시',          'DISTRICT'),
                                                                                   (@kr,  8, '41550', '안성시',          'DISTRICT'),
                                                                                   (@kr,  8, '41570', '김포시',          'DISTRICT'),
                                                                                   (@kr,  8, '41591', '화성시 만세구',   'DISTRICT'),
                                                                                   (@kr,  8, '41593', '화성시 효행구',   'DISTRICT'),
                                                                                   (@kr,  8, '41595', '화성시 병점구',   'DISTRICT'),
                                                                                   (@kr,  8, '41597', '화성시 동탄구',   'DISTRICT'),
                                                                                   (@kr,  8, '41610', '광주시',          'DISTRICT'),
                                                                                   (@kr,  8, '41630', '양주시',          'DISTRICT'),
                                                                                   (@kr,  8, '41650', '포천시',          'DISTRICT'),
                                                                                   (@kr,  8, '41670', '여주시',          'DISTRICT'),
                                                                                   (@kr,  8, '41800', '연천군',          'DISTRICT'),
                                                                                   (@kr,  8, '41820', '가평군',          'DISTRICT'),
                                                                                   (@kr,  8, '41830', '양평군',          'DISTRICT');

-- 강원특별자치도 (parent 9)
INSERT INTO location (country_id, parent_id, region_code, name, location_type) VALUES
                                                                                   (@kr,  9, '51110', '춘천시',     'DISTRICT'),
                                                                                   (@kr,  9, '51130', '원주시',     'DISTRICT'),
                                                                                   (@kr,  9, '51150', '강릉시',     'DISTRICT'),
                                                                                   (@kr,  9, '51170', '동해시',     'DISTRICT'),
                                                                                   (@kr,  9, '51190', '태백시',     'DISTRICT'),
                                                                                   (@kr,  9, '51210', '속초시',     'DISTRICT'),
                                                                                   (@kr,  9, '51230', '삼척시',     'DISTRICT'),
                                                                                   (@kr,  9, '51720', '홍천군',     'DISTRICT'),
                                                                                   (@kr,  9, '51730', '횡성군',     'DISTRICT'),
                                                                                   (@kr,  9, '51750', '영월군',     'DISTRICT'),
                                                                                   (@kr,  9, '51760', '평창군',     'DISTRICT'),
                                                                                   (@kr,  9, '51770', '정선군',     'DISTRICT'),
                                                                                   (@kr,  9, '51780', '철원군',     'DISTRICT'),
                                                                                   (@kr,  9, '51790', '화천군',     'DISTRICT'),
                                                                                   (@kr,  9, '51800', '양구군',     'DISTRICT'),
                                                                                   (@kr,  9, '51810', '인제군',     'DISTRICT'),
                                                                                   (@kr,  9, '51820', '고성군',     'DISTRICT'),
                                                                                   (@kr,  9, '51830', '양양군',     'DISTRICT');

-- 충청북도 (parent 10)
INSERT INTO location (country_id, parent_id, region_code, name, location_type) VALUES
                                                                                   (@kr, 10, '43111', '청주시 상당구', 'DISTRICT'),
                                                                                   (@kr, 10, '43112', '청주시 서원구', 'DISTRICT'),
                                                                                   (@kr, 10, '43113', '청주시 흥덕구', 'DISTRICT'),
                                                                                   (@kr, 10, '43114', '청주시 청원구', 'DISTRICT'),
                                                                                   (@kr, 10, '43130', '충주시',        'DISTRICT'),
                                                                                   (@kr, 10, '43150', '제천시',        'DISTRICT'),
                                                                                   (@kr, 10, '43720', '보은군',        'DISTRICT'),
                                                                                   (@kr, 10, '43730', '옥천군',        'DISTRICT'),
                                                                                   (@kr, 10, '43740', '영동군',        'DISTRICT'),
                                                                                   (@kr, 10, '43745', '증평군',        'DISTRICT'),
                                                                                   (@kr, 10, '43750', '진천군',        'DISTRICT'),
                                                                                   (@kr, 10, '43760', '괴산군',        'DISTRICT'),
                                                                                   (@kr, 10, '43770', '음성군',        'DISTRICT'),
                                                                                   (@kr, 10, '43800', '단양군',        'DISTRICT');

-- 충청남도 (parent 11)
INSERT INTO location (country_id, parent_id, region_code, name, location_type) VALUES
                                                                                   (@kr, 11, '44131', '천안시 동남구', 'DISTRICT'),
                                                                                   (@kr, 11, '44133', '천안시 서북구', 'DISTRICT'),
                                                                                   (@kr, 11, '44150', '공주시',        'DISTRICT'),
                                                                                   (@kr, 11, '44180', '보령시',        'DISTRICT'),
                                                                                   (@kr, 11, '44200', '아산시',        'DISTRICT'),
                                                                                   (@kr, 11, '44210', '서산시',        'DISTRICT'),
                                                                                   (@kr, 11, '44230', '논산시',        'DISTRICT'),
                                                                                   (@kr, 11, '44250', '계룡시',        'DISTRICT'),
                                                                                   (@kr, 11, '44270', '당진시',        'DISTRICT'),
                                                                                   (@kr, 11, '44710', '금산군',        'DISTRICT'),
                                                                                   (@kr, 11, '44760', '부여군',        'DISTRICT'),
                                                                                   (@kr, 11, '44770', '서천군',        'DISTRICT'),
                                                                                   (@kr, 11, '44790', '청양군',        'DISTRICT'),
                                                                                   (@kr, 11, '44800', '홍성군',        'DISTRICT'),
                                                                                   (@kr, 11, '44810', '예산군',        'DISTRICT'),
                                                                                   (@kr, 11, '44825', '태안군',        'DISTRICT');

-- 전북특별자치도 (parent 12)
INSERT INTO location (country_id, parent_id, region_code, name, location_type) VALUES
                                                                                   (@kr, 12, '52111', '전주시 완산구', 'DISTRICT'),
                                                                                   (@kr, 12, '52113', '전주시 덕진구', 'DISTRICT'),
                                                                                   (@kr, 12, '52130', '군산시',        'DISTRICT'),
                                                                                   (@kr, 12, '52140', '익산시',        'DISTRICT'),
                                                                                   (@kr, 12, '52180', '정읍시',        'DISTRICT'),
                                                                                   (@kr, 12, '52190', '남원시',        'DISTRICT'),
                                                                                   (@kr, 12, '52210', '김제시',        'DISTRICT'),
                                                                                   (@kr, 12, '52710', '완주군',        'DISTRICT'),
                                                                                   (@kr, 12, '52720', '진안군',        'DISTRICT'),
                                                                                   (@kr, 12, '52730', '무주군',        'DISTRICT'),
                                                                                   (@kr, 12, '52740', '장수군',        'DISTRICT'),
                                                                                   (@kr, 12, '52750', '임실군',        'DISTRICT'),
                                                                                   (@kr, 12, '52770', '순창군',        'DISTRICT'),
                                                                                   (@kr, 12, '52790', '고창군',        'DISTRICT'),
                                                                                   (@kr, 12, '52800', '부안군',        'DISTRICT');

-- 전라남도 (parent 13) — 통합 코드 12xxx 사용
INSERT INTO location (country_id, parent_id, region_code, name, location_type) VALUES
                                                                                   (@kr, 13, '12110', '목포시',     'DISTRICT'),
                                                                                   (@kr, 13, '12130', '여수시',     'DISTRICT'),
                                                                                   (@kr, 13, '12150', '순천시',     'DISTRICT'),
                                                                                   (@kr, 13, '12170', '나주시',     'DISTRICT'),
                                                                                   (@kr, 13, '12190', '광양시',     'DISTRICT'),
                                                                                   (@kr, 13, '12710', '담양군',     'DISTRICT'),
                                                                                   (@kr, 13, '12720', '곡성군',     'DISTRICT'),
                                                                                   (@kr, 13, '12730', '구례군',     'DISTRICT'),
                                                                                   (@kr, 13, '12740', '고흥군',     'DISTRICT'),
                                                                                   (@kr, 13, '12750', '보성군',     'DISTRICT'),
                                                                                   (@kr, 13, '12760', '화순군',     'DISTRICT'),
                                                                                   (@kr, 13, '12770', '장흥군',     'DISTRICT'),
                                                                                   (@kr, 13, '12780', '강진군',     'DISTRICT'),
                                                                                   (@kr, 13, '12790', '해남군',     'DISTRICT'),
                                                                                   (@kr, 13, '12800', '영암군',     'DISTRICT'),
                                                                                   (@kr, 13, '12810', '무안군',     'DISTRICT'),
                                                                                   (@kr, 13, '12820', '함평군',     'DISTRICT'),
                                                                                   (@kr, 13, '12830', '영광군',     'DISTRICT'),
                                                                                   (@kr, 13, '12840', '장성군',     'DISTRICT'),
                                                                                   (@kr, 13, '12850', '완도군',     'DISTRICT'),
                                                                                   (@kr, 13, '12860', '진도군',     'DISTRICT'),
                                                                                   (@kr, 13, '12870', '신안군',     'DISTRICT');

-- 경상북도 (parent 14)
INSERT INTO location (country_id, parent_id, region_code, name, location_type) VALUES
                                                                                   (@kr, 14, '47111', '포항시 남구', 'DISTRICT'),
                                                                                   (@kr, 14, '47113', '포항시 북구', 'DISTRICT'),
                                                                                   (@kr, 14, '47130', '경주시',      'DISTRICT'),
                                                                                   (@kr, 14, '47150', '김천시',      'DISTRICT'),
                                                                                   (@kr, 14, '47170', '안동시',      'DISTRICT'),
                                                                                   (@kr, 14, '47190', '구미시',      'DISTRICT'),
                                                                                   (@kr, 14, '47210', '영주시',      'DISTRICT'),
                                                                                   (@kr, 14, '47230', '영천시',      'DISTRICT'),
                                                                                   (@kr, 14, '47250', '상주시',      'DISTRICT'),
                                                                                   (@kr, 14, '47280', '문경시',      'DISTRICT'),
                                                                                   (@kr, 14, '47290', '경산시',      'DISTRICT'),
                                                                                   (@kr, 14, '47730', '의성군',      'DISTRICT'),
                                                                                   (@kr, 14, '47750', '청송군',      'DISTRICT'),
                                                                                   (@kr, 14, '47760', '영양군',      'DISTRICT'),
                                                                                   (@kr, 14, '47770', '영덕군',      'DISTRICT'),
                                                                                   (@kr, 14, '47820', '청도군',      'DISTRICT'),
                                                                                   (@kr, 14, '47830', '고령군',      'DISTRICT'),
                                                                                   (@kr, 14, '47840', '성주군',      'DISTRICT'),
                                                                                   (@kr, 14, '47850', '칠곡군',      'DISTRICT'),
                                                                                   (@kr, 14, '47900', '예천군',      'DISTRICT'),
                                                                                   (@kr, 14, '47920', '봉화군',      'DISTRICT'),
                                                                                   (@kr, 14, '47930', '울진군',      'DISTRICT'),
                                                                                   (@kr, 14, '47940', '울릉군',      'DISTRICT');

-- 경상남도 (parent 15)
INSERT INTO location (country_id, parent_id, region_code, name, location_type) VALUES
                                                                                   (@kr, 15, '48121', '창원시 의창구',     'DISTRICT'),
                                                                                   (@kr, 15, '48123', '창원시 성산구',     'DISTRICT'),
                                                                                   (@kr, 15, '48125', '창원시 마산합포구', 'DISTRICT'),
                                                                                   (@kr, 15, '48127', '창원시 마산회원구', 'DISTRICT'),
                                                                                   (@kr, 15, '48129', '창원시 진해구',     'DISTRICT'),
                                                                                   (@kr, 15, '48170', '진주시',            'DISTRICT'),
                                                                                   (@kr, 15, '48220', '통영시',            'DISTRICT'),
                                                                                   (@kr, 15, '48240', '사천시',            'DISTRICT'),
                                                                                   (@kr, 15, '48250', '김해시',            'DISTRICT'),
                                                                                   (@kr, 15, '48270', '밀양시',            'DISTRICT'),
                                                                                   (@kr, 15, '48310', '거제시',            'DISTRICT'),
                                                                                   (@kr, 15, '48330', '양산시',            'DISTRICT'),
                                                                                   (@kr, 15, '48720', '의령군',            'DISTRICT'),
                                                                                   (@kr, 15, '48730', '함안군',            'DISTRICT'),
                                                                                   (@kr, 15, '48740', '창녕군',            'DISTRICT'),
                                                                                   (@kr, 15, '48820', '고성군',            'DISTRICT'),
                                                                                   (@kr, 15, '48840', '남해군',            'DISTRICT'),
                                                                                   (@kr, 15, '48850', '하동군',            'DISTRICT'),
                                                                                   (@kr, 15, '48860', '산청군',            'DISTRICT'),
                                                                                   (@kr, 15, '48870', '함양군',            'DISTRICT'),
                                                                                   (@kr, 15, '48880', '거창군',            'DISTRICT'),
                                                                                   (@kr, 15, '48890', '합천군',            'DISTRICT');

-- 제주특별자치도 (parent 16)
INSERT INTO location (country_id, parent_id, region_code, name, location_type) VALUES
                                                                                   (@kr, 16, '50110', '제주시',     'DISTRICT'),
                                                                                   (@kr, 16, '50130', '서귀포시',   'DISTRICT');

-- 세종특별자치시 (parent 17)
INSERT INTO location (country_id, parent_id, region_code, name, location_type) VALUES
    (@kr, 17, '36110', '세종시',     'DISTRICT');