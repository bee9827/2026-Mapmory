<!-- ccr-projects-attribution: {"github_login":"bee9827"} -->
_Requested by **티온** · [project thread](https://claude.ai/code/project/chan_0197zjXiErXRd6aWvrPQJAGq?thread=cmsg_0197zjXiErXRd6aWvrPQJAGqJ8zmQ3EyhJSYya3BGyu76x)_

## 요약

Before: dev에서 Google 장소 후보를 고르면(`GET /api/v1/places/{placeId}`) 국내 장소는 500이 났습니다.

After: 추천 지역(국가·시·도·시·군·구)이 담긴 선택 응답이 정상으로 옵니다.

주소로 시·군·구를 찾을 때 쓰는 목록 조회 `RegionRepository.findByParentIdAndRegionType`에 `@EntityGraph(attributePaths = {"parent", "root"})`를 붙였습니다.

## 배경

- #310부터 Google 장소는 좌표 대신 주소 이름으로 시·군·구를 찾습니다(`KoreanAddressRegionMatcher`). 이 경로는 `RegionResolver.children()`로 시·도와 시·군·구 목록을 가져옵니다.
- 이 목록 조회는 `parent`(시·도)와 `root`(국가)를 지연 로딩합니다. 장소 선택 서비스에는 트랜잭션이 없고 `open-in-view: false`라서, 컨트롤러가 `RegionDetailResponse`를 만들며 국가 코드를 읽을 때 `LazyInitializationException`이 납니다.
- dev 로그(2026-10-08 11:05~11:07 UTC): `GET /api/v1/places/ChIJ…` → `Could not initialize proxy [Region#122] - no session` 500, 4건.
- 기존 좌표 경로는 `@EntityGraph`가 붙은 단건 조회(`findByParentIdAndRegionTypeAndRegionCode`)를 써서 문제가 없었습니다.

## 주요 결정

- 서비스에 `@Transactional`을 붙이는 대신 조회에서 함께 가져왔습니다. 선택 API는 외부 Google 호출을 포함하므로, 트랜잭션을 열면 그동안 DB 연결을 잡고 있게 됩니다.
- 이 목록 조회는 주소 매칭에서만 씁니다. 시·도 17개, 시·군·구 수십 개라 조인 비용은 작습니다.

## 확인

- [x] 로컬에서 실행 확인: 통합 테스트 `KoreanAddressRegionMatcherIntegrationTest`를 추가했습니다. "서울특별시 종로구"로 찾은 지역을 트랜잭션 밖에서 응답으로 바꿉니다. 수정 전에는 dev와 같은 `Region#122` 오류로 실패했고, 수정 후 통과합니다. 전체 테스트 392개 통과.
- [x] 비밀 정보(비밀번호·키·토큰)를 커밋하지 않았음
- [ ] 새 환경변수를 추가했다면 `.env.example` 갱신 (해당 없음)
- [ ] DB 스키마를 변경했다면 **새 마이그레이션 파일**로 추가 (해당 없음)
- [ ] 실행 방법이나 환경 설정이 바뀌었다면 README 갱신 (해당 없음)

🤖 Generated with [Claude Code](https://claude.com/claude-code)

https://claude.ai/code/session_01DwZY5ZwSQzDxuf5XfaTB5W
