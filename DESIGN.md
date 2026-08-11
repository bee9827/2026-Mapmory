# Design

## Source of truth

- Status: Draft
- Last refreshed: 2026-08-11
- Primary product surfaces: Mapmory Android/iOS 온보딩
- Evidence reviewed: `client/docs/onboarding/onboarding.html`, Stitch `DESIGN.md`, 온보딩 화면 3종

## Brand

- Personality: 차분하고 따뜻한 여행 기록 서비스
- Trust signals: 명확한 문구, 충분한 대비, 과하지 않은 장식
- Avoid: 외부 디바이스 목업, 불필요한 그림자, 색상 토큰의 임의 확장

## Product goals

- Goals: 사용자가 Mapmory의 핵심 가치와 기록 흐름을 빠르게 이해한다.
- Non-goals: 온보딩에서 모든 기능을 설명하거나 권한을 요청하지 않는다.
- Success signals: 3개 화면을 짧게 읽고 기록 시작 화면으로 이동한다.

## Information architecture

- Primary navigation: 건너뛰기, 다음, 시작하기
- Core screens: 지도 소개 → 지역·여행 기록 → 사진은 나중에 추가 가능
- Content hierarchy: 시각적 메시지 → 제목 → 상세 설명 → 진행 상태 → 주요 행동

## Design principles

- 여행 기록의 주체는 사진이 아니라 장소와 이야기다.
- 한 화면에는 하나의 메시지만 남기고, 행동 버튼은 하단에 고정한다.
- 온보딩은 전체 화면으로 보여주며 기기 목업을 사용하지 않는다.

## Visual language

- Color: canvas `#0B141A`, surface `#121D24`, raised surface `#1A262F`, accent `#21E69A`, primary text `#F0FFF8`, muted text `#B9CBBC`. Accent is reserved for actions, selected states, map markers, and small visual cues.
- Typography: Be Vietnam Pro 우선, Pretendard 및 시스템 폰트 대체.
- Spacing/layout rhythm: 4/8/12/16/20/24/32 px.
- Shape/radius/elevation: 카드 20 px, 버튼 14 px, 필/인디케이터 9999 px, 그림자 최소화.
- Motion: 현재 정적 디자인만 정의한다.
- Imagery/iconography: 첫 화면은 대륙 윤곽이 보이는 상세 지도 이미지를 사용하고, 나머지 화면은 지도·경로·앨범 벡터 일러스트를 사용한다.

## Components

- Existing components to reuse: Mapmory 공통 색상·간격·타이포그래피 토큰
- New/changed components: 온보딩 화면, Skip link, Indicator, Primary button, Memory illustration
- Variants and states: indicator active/inactive, 3개 화면별 primary action
- Token/component ownership: 공통 토큰은 디자인 시스템에서 관리하고 화면은 토큰만 참조한다.

## Accessibility

- Target standard: Android 기본 접근성 기준
- Keyboard/focus behavior: 웹 변환본에서는 링크와 버튼을 순서대로 포커스할 수 있어야 한다.
- Contrast/readability: 본문과 배경의 대비를 확보하고 색상만으로 상태를 구분하지 않는다.
- Screen-reader semantics: 화면 제목, 진행 상태, 주요 행동에 의미 있는 라벨을 제공한다.
- Reduced motion and sensory considerations: 모션은 별도 정의 전까지 사용하지 않는다.

## Responsive behavior

- Supported breakpoints/devices: 390×844 기준 모바일 프레임, 480 px 이하에서는 화면을 세로로 표시한다.
- Layout adaptations: 화면 프레임은 너비에 맞추고 내부 여백은 24 px을 유지한다.
- Touch/hover differences: 버튼과 건너뛰기는 터치 가능한 영역을 확보한다.

## Interaction states

- Loading: 해당 없음
- Empty: 사진이 없어도 기록을 시작할 수 있다.
- Error: 해당 없음
- Success: 시작하기 이후 기록 화면으로 이동한다.
- Disabled: 해당 없음
- Offline/slow network, if applicable: 온보딩 자체는 네트워크 없이도 표시 가능해야 한다.

## Content voice

- Tone: 친근하고 안심시키는 존댓말
- Terminology: 여행 기록, 지역, 사진, 지도
- Microcopy rules: 한 화면에 하나의 핵심 메시지, 짧은 문장, 사진 첨부는 선택 사항임을 명시한다.

## Implementation constraints

- Framework/styling system: HTML/CSS/SVG 시안, 이후 CMP 화면에 이식
- Design-token constraints: 강조색은 `#21E69A`로 통일한다.
- Performance constraints: 온보딩은 정적 벡터 중심으로 구성하고 큰 외부 이미지 의존성을 피한다.
- Compatibility constraints: Android 9 이상에서 표현 가능한 정적 UI를 기준으로 한다.
- Test/screenshot expectations: 3개 화면이 전체 화면 프레임으로 렌더링되고 인디케이터가 정확히 3개인지 확인한다.

## Open questions

- [ ] 최종 폰트 파일과 실제 CMP 폰트 적용 방식 결정
- [ ] 온보딩 완료 여부 저장 위치와 재노출 정책 결정
