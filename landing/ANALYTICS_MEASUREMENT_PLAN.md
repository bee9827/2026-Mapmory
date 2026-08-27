# Mapmory 랜딩페이지 측정 계획

기준일: 2026-08-25  
대상 페이지: <https://map-mory.com/>  
GA4 측정 ID: `G-MC93CZWLZF`  
현재 랜딩 버전: `v1`

## 1. 측정 목적

랜딩페이지의 목표는 공개 출시 전에는 **신규 출시 알림 이메일 확보**, 공개 출시 후에는 **Google Play 다운로드 이동**이다. 측정은 아래 세 가지 의사결정에 답해야 한다.

1. 방문자가 제품의 가치를 이해하고 출시 알림을 신청하는가?
2. 지구본과 대한민국 상세지도 체험이 신청 전환에 실제로 도움이 되는가?
3. 전환이 낮을 때 CTA, 폼, 체험 중 어디가 병목인가?

GA4는 행동 분석 도구이며, 실제 고유 이메일 신청자 수의 원천은 백엔드 `launch_waitlist` 테이블이다. 광고 차단, 네트워크 차단, 중복 신청 때문에 두 수치는 완전히 일치하지 않을 수 있다.

## 2. 경쟁 서비스 공개 조사

2026-08-25에 공개 랜딩페이지의 코드·링크와 공식 개인정보/쿠키 문서를 확인했다. 경쟁사의 내부 GA4·Amplitude 대시보드에는 접근할 수 없으므로 실제 CTA 전환율이나 체험률은 공개 여부를 그대로 표시한다.

| 서비스 | 랜딩페이지에서 직접 확인된 측정 구조 | 앱/정책 문서에서 추가로 확인된 분석 | 공개 규모 | 랜딩 전환율 |
| --- | --- | --- | --- | --- |
| [Wanderlog](https://wanderlog.com/) | Amplitude 스크립트 사용. `Start planning` 링크에 이벤트명과 `position: top/bottom`, `source: LandingPageButtons`를 전달한다. 로그인·가입, 가이드, 도시, Pro 이동도 서로 다른 이벤트로 구분한다. | [개인정보처리방침](https://wanderlog.com/privacy)은 IP, 브라우저, 유입/이탈 페이지, 클릭 수, 방문 페이지와 사용자 환경 정보를 설명한다. | 공식 랜딩에서 100만 명 이상 사용 경험 표시 | 비공개 |
| [Mapstr](https://en.mapstr.com/) | GA4 스크립트와 `Create my Map`, `Download` CTA를 확인했다. 구체적인 커스텀 이벤트 이름은 공개되지 않는다. | [개인정보처리방침](https://en.mapstr.com/politique-de-confidentialite)은 앱 분석에 Amplitude·Firebase, 유입 측정에 Branch를 사용하며 기기 위치/IP, 접속 방식, 내부 사용자 ID, 장소·태그 통계를 처리한다고 밝힌다. | 공식 랜딩에서 400만 명 이상 사용자, 1억 500만 개 이상 장소 표시 | 비공개 |
| [Polarsteps](https://www.polarsteps.com/) | `Get the app` CTA와 상·하단 다운로드 흐름을 확인했다. 당시 공개 페이지에서 구체적인 클라이언트 이벤트 이름은 확인되지 않았다. | [공식 앱 쿠키 문서](https://www.polarsteps.com/de/legal/cookies-in-the-app)는 Firebase Analytics·Performance·Crashlytics·Remote Config, AppsFlyer, Mapbox 사용을 밝힌다. 이는 앱 측정 정보이며 랜딩 측정의 직접 증거는 아니다. | 공식 랜딩에서 2,200만 명 이상 사용자 표시 | 비공개 |

### 공개 랜딩 벤치마크

- [Unbounce 전체 랜딩 벤치마크](https://unbounce.com/average-conversion-rates-landing-pages/): 41,000개 랜딩, 4억 6,400만 방문, 5,700만 전환을 분석한 중앙 전환율은 **6.6%**다.
- [Unbounce 여행·숙박 벤치마크](https://unbounce.com/conversion-benchmark-report/travel-hospitality-conversion-rate/): 여행·숙박 랜딩의 중앙 전환율은 **4.8%**다.

전환에는 예약, 문의, 가입, 다운로드 등 서로 다른 행동이 섞여 있다. 따라서 4.8%와 6.6%는 Mapmory의 절대 합격선이 아니라 첫 목표 범위를 잡는 외부 기준으로만 사용한다.

## 3. KPI와 기대치·실측치 비교

기대치는 공개 벤치마크와 현재 한 필드 이메일 폼의 마찰도를 바탕으로 잡은 **출시 전 가설**이다. 최초 100~200개의 정상 세션 또는 배포 후 7일 중 늦게 도달하는 시점에 첫 실측치를 기록하고 목표를 다시 조정한다.

| 구분 | 지표와 계산식 | 기대치 | 주의 기준 | 현재 실측 | 차이/판정 |
| --- | --- | ---: | ---: | ---: | --- |
| 핵심 결과 | 신규 대기 명단 전환율 = `waitlist_submit(result=subscribed)` 세션 ÷ 전체 세션 | **5% 이상** | 3% 미만 | 배포 전 | 수집 후 계산 |
| 핵심 결과 | 체험 전환 효과 = 체험 시작 세션의 신규 신청률 ÷ 비체험 세션의 신규 신청률 | **1.0 초과** | 0.8 미만 | 배포 전 | 최소 200세션 후 판단 |
| 선행 지표 | 체험 도달률 = `experience_view(globe)` 세션 ÷ 전체 세션 | **50% 이상** | 35% 미만 | 배포 전 | 내부 가설 |
| 선행 지표 | 체험 활성화율 = `experience_start` 세션 ÷ `experience_view` 세션 | **30% 이상** | 20% 미만 | 배포 전 | 내부 가설 |
| 선행 지표 | 장소 탐색률 = `place_select` 세션 ÷ `experience_start` 세션 | **60% 이상** | 40% 미만 | 배포 전 | 내부 가설 |
| 선행 지표 | 폼 시작률 = `waitlist_form_start` 세션 ÷ `waitlist_form_view` 세션 | **25% 이상** | 15% 미만 | 배포 전 | 내부 가설 |
| 선행 지표 | 폼 완료율 = `waitlist_submit` 세션 ÷ `waitlist_form_start` 세션 | **70% 이상** | 50% 미만 | 배포 전 | 한 필드 폼 가설 |
| 가드레일 | 기술 제출 오류율 = `waitlist_submit_error(error_type!=validation)` ÷ `waitlist_form_start` | **2% 미만** | 5% 초과 | 배포 전 | 백엔드 로그와 함께 확인 |

운영 해석 기준:

- 신규 대기 명단 전환율 **8% 이상**은 초기 강한 신호로 본다.
- 체험 활성화율이 높아도 신청률이 오르지 않으면 체험은 재미는 있지만 제품 가치나 CTA로 연결되지 않는 것이다.
- 폼 시작률이 낮으면 제안 문구·신뢰·개인정보 설명을, 폼 완료율이 낮으면 검증 오류와 체크박스 마찰을 먼저 본다.
- `experience_engagement`의 10·30·60초 값과 CTA 위치별 클릭률은 첫 기간에는 목표를 두지 않고 기준선을 만든다.

## 4. 이벤트 사전

`page_view`는 GA4가 자동 수집한다. 아래 이벤트는 코드에서 명시적으로 전송한다.

| 이벤트 | 발생 조건 | 주요 파라미터 | 중복 방지 |
| --- | --- | --- | --- |
| `experience_view` | 체험 영역이 50% 이상 1초간 노출 | `experience_type` | 체험 종류별 페이지당 1회 |
| `experience_start` | 첫 장소 선택·지구본 드래그·확대 | `experience_type`, `interaction_type` | 체험 종류별 페이지당 1회 |
| `place_select` | 선택이 바뀌어 기억 카드가 실제로 변경 | `experience_type`, `place_id`, `selection_source` | 같은 장소 재선택 제외 |
| `experience_engagement` | 체험 시작 후 화면이 보이는 활성 시간이 10·30·60초에 도달 | `experience_type`, `milestone_seconds` | 각 마일스톤 1회 |
| `waitlist_cta_click` | 헤더·히어로·대한민국 기억 카드 CTA 클릭 | `cta_placement` | 클릭마다 기록 |
| `waitlist_form_view` | 폼이 50% 이상 1초간 노출 | 없음 | 페이지당 1회 |
| `waitlist_form_start` | 폼 내부 최초 포커스 또는 값 변경 | 없음 | 페이지당 1회 |
| `waitlist_submit` | 백엔드가 신규 또는 기존 신청으로 성공 응답 | `result` | 제출 성공마다 기록 |
| `waitlist_submit_error` | 클라이언트 검증, 네트워크, 서버 또는 응답 오류 | `error_type`, `validation_field` | 오류 발생마다 기록 |
| `download_click` | 공개 출시 후 Google Play CTA 클릭 | `cta_placement` | 클릭마다 기록 |

모든 커스텀 이벤트에는 `landing_version`이 자동으로 포함된다. 이벤트 이름은 허용 목록으로 제한하고, `email`, `phone`, `name`, `address`, 자유 입력문 등 직접 식별 가능 정보로 보이는 파라미터는 분석 모듈에서 제거한다.

### 파라미터 허용값

| 파라미터 | 허용값 |
| --- | --- |
| `landing_version` | `v1`부터 시작하는 안정적인 배포 버전 |
| `experience_type` | `globe`, `korea_detail` |
| `interaction_type` | `place_select`, `globe_drag`, `globe_zoom` |
| `place_id` | `hapjeong`, `shanghai`, `tokyo`, `usa-west`, `yeosu`, `jeju` |
| `selection_source` | `shortcut`, `globe`, `map` |
| `cta_placement` | `header`, `hero`, `korea_memory`, `final` |
| `milestone_seconds` | `10`, `30`, `60` |
| `result` | `subscribed`, `already_subscribed` |
| `error_type` | `validation`, `network`, `server`, `request`, `response`, `unknown` |

## 5. GA4 보고서 구성

GA4에서 다음 이벤트 파라미터를 이벤트 범위 맞춤 측정기준으로 등록한다.

`landing_version`, `experience_type`, `interaction_type`, `place_id`, `selection_source`, `cta_placement`, `result`, `error_type`, `validation_field`, `milestone_seconds`

권장 탐색 보고서:

1. **기본 전환 퍼널**: `page_view → waitlist_form_view → waitlist_form_start → waitlist_submit`
2. **체험 효과 퍼널**: `experience_view → experience_start → place_select → waitlist_submit`
3. **CTA 비교**: `cta_placement`별 `waitlist_cta_click`, 이후 `waitlist_submit` 비율
4. **체험 비교**: `experience_type`별 시작률, 장소 선택률, 10·30·60초 도달률
5. **품질 보고서**: `error_type`별 `waitlist_submit_error`와 백엔드 오류 로그 비교

`waitlist_submit`은 GA4 핵심 이벤트로 지정한다. 신규 획득 수치를 볼 때는 `result=subscribed`로 필터링하고, 최종 고유 신청자 수는 백엔드 DB를 사용한다.

## 6. 실측 업데이트 절차

매주 같은 기간과 유입 조건을 사용해 아래 순서로 이 문서의 `현재 실측`과 `차이/판정`을 갱신한다.

1. 내부 개발·QA 트래픽을 제외한다.
2. `landing_version`으로 페이지 변경 전후를 분리한다.
3. 전체와 모바일을 함께 보고, 모바일 표본도 별도로 확인한다.
4. GA4 신규 신청 이벤트 수와 DB 신규 행 수의 차이를 확인한다.
5. 기준 미달 지표는 바로 UI를 바꾸기 전에 해당 단계의 유입 품질과 오류율을 함께 확인한다.

첫 100세션 이전의 비율은 방향만 참고하고, 단일 날짜 변화로 결론 내리지 않는다.
