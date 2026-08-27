# Android 모니터링 기준

## 목적

Mapmory Android 앱의 기능 오류와 성능 저하를 같은 기준으로 확인한다.
현재는 Firebase 같은 원격 SDK를 추가하지 않고, CI·Android Vitals·Logcat·System Trace를 조합한다.

모니터링은 사용자 행동 분석과 구분한다.

- 모니터링: 앱이 안정적으로 실행되고 빠르게 반응하는지 확인한다.
- 분석: 사용자가 어떤 기능을 사용하는지 확인한다.

## 1차 완료 범위

- PR 단계의 CI 검증 방법을 정의했다.
- 사진 추천 흐름의 기존 `MapmoryPhotoPerf` 로그와 측정 문서를 연결했다.
- 지도 선택 정확성, 사진 추천, 기록 저장을 우선 모니터링 대상으로 정했다.
- 개인정보를 포함하지 않는 로그 규칙을 정했다.
- Firebase와 같은 원격 SDK는 현재 범위에서 제외했다.

이 문서는 현재 MVP의 개발·출시 전 검증 기준이다. Macrobenchmark와 원격 오류 수집은 실제 필요성이 확인된 뒤 별도 작업으로 추가한다.

## 모니터링 계층

| 시점 | 도구 | 확인 대상 |
| --- | --- | --- |
| PR 병합 전 | GitHub Actions CI | 테스트, Lint, Debug 빌드 |
| 개발 중 | `MapmoryPhotoPerf`, Logcat, `android.os.Trace` | 사진 조회·EXIF·추천 단계별 시간 |
| 성능 조사 | System Trace·Perfetto·Macrobenchmark | 앱 시작, 지도 전환, UI 응답성 |
| Play 배포 후 | Play Console Android Vitals | 크래시, ANR, 시작 시간, 렌더링, 메모리 |

CI 실행 방법은 [ci.md](./ci.md), 사진 측정 결과는 [photo-loading-benchmark.md](./performance/photo-loading-benchmark.md)에 기록한다.

## 우선 모니터링 항목

### 1. 안정성

| 흐름 | 기준 | 확인 방법 |
| --- | --- | --- |
| 앱 실행 | 크래시 없이 지도 화면 표시 | 수동 스모크 테스트, Android Vitals |
| 지도 선택 | 선택 후 앱이 종료되지 않고 다음 화면 이동 | Compose 계측 테스트, 수동 확인 |
| 기록 저장 | 성공 또는 사용자에게 이해 가능한 오류 표시 | 계측 테스트, 서버 응답 로그 |
| 사진 추천 | 권한·빈 결과·EXIF 누락 상황에서 크래시 없음 | 계측 테스트, 실기기 확인 |

크래시 또는 ANR이 발생하면 기능 성공으로 보지 않는다. Play에 배포된 버전은 Android Vitals에서 사용자 체감 크래시율과 ANR을 확인한다.

### 2. 사용자 흐름 성능

다음 구간을 별도의 측정 단위로 본다.

| 측정 이름 | 시작 | 종료 | 주요 값 |
| --- | --- | --- | --- |
| `app_startup` | 앱 실행 | 첫 화면 표시 | 전체 시간 |
| `map_scope_change` | 시·도 선택 | 상세 지도 표시 | 전체 시간, 선택 성공 여부 |
| `map_location_select` | 지도 Polygon 터치 | 기록 화면 표시 | 전체 시간, regionCode 일치 여부 |
| `photo_recommend` | 추천 버튼 터치 | 추천 결과 표시 | 전체 시간, EXIF 읽기 수, 캐시 재사용 수, 결과 수 |
| `photo_preview` | 사진 선택 | 미리보기 표시 | 전체 시간, 성공 여부 |
| `record_save` | 저장 버튼 터치 | 저장 성공 또는 오류 표시 | 전체 시간, 성공 여부 |

시간은 평균만 사용하지 않고 동일 조건에서 반복 측정한 중앙값과 최댓값을 함께 기록한다.
첫 기준선이 없는 항목은 임의의 절대 시간보다 동일 기기·동일 데이터의 이전 측정 대비 20% 이상 느려졌는지를 먼저 본다.

사진 추천은 시간만으로 캐시 효과를 판단하지 않는다. 다음 값도 함께 확인한다.

```text
previous_photos > 0
exif_reads 감소
reused_coordinates 증가
recommended_photos 결과 확인
```

### 3. 지도 선택 정확성

성능과 별개로 다음 조건을 항상 만족해야 한다.

- 화면에 표시된 지역과 기록 작성 화면의 지역명이 일치한다.
- 선택한 Polygon의 canonical `regionCode`가 앱 `Location`과 일치한다.
- 시·도에서 상세 지도로 이동한 뒤 뒤로가기를 하면 원래 시·도 지도로 돌아간다.
- 지역 경계선이나 작은 지역을 눌러도 앱이 종료되지 않는다.

이 항목은 시간보다 자동화 테스트의 성공 여부를 우선한다. 잘못된 지역으로 이동하면 성능이 빨라도 실패다.

## 로그 규칙

개발 중 성능 로그는 `MapmoryPhotoPerf` 태그를 사용한다.

```bash
adb -s <serial> shell setprop log.tag.MapmoryPhotoPerf DEBUG
adb -s <serial> logcat -v time -s MapmoryPhotoPerf:D
```

로그에 포함할 수 있는 값:

- 단계별 소요 시간
- 조회·처리한 사진 개수
- EXIF 읽기 개수
- 캐시 재사용 개수
- 성공·실패 여부
- 오류 종류

로그에 포함하지 않는 값:

- 사진 파일명과 전체 경로
- 사진 원본·미리보기 데이터
- 제목·본문 등 기록 내용
- 위도·경도 원본 좌표
- 회원 식별 정보

## 실행 주기

| 시점 | 실행 항목 |
| --- | --- |
| 일반 코드 수정 | 관련 JVM·호스트 테스트, 필요 시 계측 테스트 |
| 사진·Room 수정 | 사진 메타데이터 자동화 테스트와 실기기 로그 확인 |
| 지도 렌더링·탐색 수정 | 지도 선택 계측 테스트와 System Trace 확인 |
| PR 생성·수정 | CI의 테스트·Lint·Debug 빌드 |
| 릴리스 전 | 대표 기기 스모크 테스트와 Play Console 사전 출시 보고서 확인 |
| 릴리스 후 | Android Vitals에서 크래시·ANR·성능 이상 확인 |

## Firebase 도입 판단

Firebase는 백엔드 API나 데이터베이스를 대신하지 않는다.

- 백엔드: 여행 기록, 사진, Location, API 응답을 관리한다.
- Crashlytics: 사용자 기기에서 발생한 Android 크래시·ANR·비정상 오류를 원격으로 확인한다.
- Analytics: 기능 사용 흐름을 분석한다.
- Performance: 앱 시작과 네트워크 등 성능을 원격으로 수집한다.

현재 MVP에서는 Play Console Android Vitals와 기존 로컬 로그로 시작한다.
실제 사용자 환경의 오류 원인을 원격으로 추적해야 할 때 Crashlytics 도입을 별도 결정한다.
도입하면 개인정보처리방침과 Play Console 데이터 보안 응답을 함께 갱신한다.

## 후속 작업

1. 이 기준으로 현재 사진 추천 로그를 다시 측정해 기준선을 만든다.
2. 앱 시작과 지도 전환에 Macrobenchmark를 추가한다.
3. 비공개·프로덕션 테스트에서 Android Vitals를 주기적으로 확인한다.
4. 원격 오류 추적이 필요해지는 시점에만 Crashlytics를 검토한다.
