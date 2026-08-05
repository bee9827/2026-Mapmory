# Mapmory

여행을 지도에 남기는 서비스 Mapmory의 저장소 안내입니다.

## 저장소

| 영역 | 저장소 |
| --- | --- |
| Android | [2026-Mapmory-android](https://github.com/woowacourse-teams/2026-Mapmory-android) |
| iOS | [2026-Mapmory-ios](https://github.com/woowacourse-teams/2026-Mapmory-ios) |
| Backend | [2026-Mapmory-backend](https://github.com/woowacourse-teams/2026-Mapmory-backend) |

## 저장소 경계

각 플랫폼 저장소는 독립적으로 빌드·배포합니다. Compose Multiplatform 공통 코드를 도입할 경우 Android/iOS 저장소 중 한 곳에 복사하지 않고, 별도의 shared 모듈 저장소 또는 패키지 배포 전략을 먼저 결정합니다.

## 현재 클라이언트 구조

```text
client/
├── androidApp/  # Android 진입점
├── iosApp/      # Xcode 앱 진입점
└── shared/      # Compose Multiplatform 공통 코드
```

클라이언트 환경 구성은 [`docs/client-environment.md`](docs/client-environment.md)에 기록합니다.

## 팀 운영 원칙

- 버전과 실행 명령은 각 저장소 README에 기록합니다.
- 비밀정보·서명키·개인 환경 경로는 커밋하지 않습니다.
- 릴리스 서명과 배포 권한은 한 명의 개인 계정에만 의존하지 않습니다.
- 플랫폼 저장소의 변경은 해당 저장소의 PR에서 리뷰합니다.
