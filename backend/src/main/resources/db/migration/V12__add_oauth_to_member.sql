-- 회원 소셜 로그인 정보 추가
--
-- provider     : 소셜 제공자 (KAKAO 등). AuthProvider enum 을 문자열로 저장.
-- provider_id  : 소셜 제공자가 부여한 회원 식별자 (예: 카카오 회원번호).
--
-- [기존 행 처리]
-- V11 로 적재한 임시 회원은 소셜 정보가 없다. 이들을 지우거나 더미값으로 채우는 대신
-- 두 컬럼을 NULL 허용으로 추가한다. 신규 소셜 회원은 애플리케이션(ofOAuth)에서
-- provider/provider_id 를 항상 채우므로, 실제 로그인 경로에서는 값이 비지 않는다.
--
-- [중복 가입 방지]
-- UNIQUE (provider, provider_id) 로 같은 소셜 계정의 중복 회원 생성을 막는다.
-- MySQL 은 UNIQUE 인덱스에서 NULL 을 서로 다른 값으로 취급하므로,
-- provider 가 NULL 인 기존 임시 회원 여러 건이 제약에 걸리지 않는다.
ALTER TABLE member
    ADD COLUMN provider    VARCHAR(20)  NULL COMMENT 'KAKAO 등 소셜 제공자',
    ADD COLUMN provider_id VARCHAR(255) NULL COMMENT '소셜 제공자의 회원 식별자',
    ADD CONSTRAINT uk_member_provider UNIQUE (provider, provider_id);
