# 클라이언트 개발 환경

## 기준 버전

| 항목 | 기준 |
| --- | --- |
| Kotlin | 2.4.10 |
| Compose Multiplatform | 1.11.1 |
| Android Gradle Plugin | 9.1.0 |
| Gradle | 9.5.0 (Wrapper로 고정) |
| JDK | 21 |
| Android compileSdk | 36 |
| Android targetSdk | 36 |
| Android minSdk | 28 |
| iOS Deployment Target | 16.0 |
| Xcode | 26.4 계열 |

Kotlin 2.4.10의 공식 KMP 호환 범위에 맞추기 위해 AGP 9.1.0을 사용합니다.

## 프로젝트 경계

- `androidApp`은 Android 진입점과 Android 전용 코드만 관리합니다.
- `iosApp`은 Xcode 진입점과 iOS 전용 코드만 관리합니다.
- `shared`는 Android와 iOS에서 함께 사용하는 코드만 관리합니다.
- Backend 소스와 의존성은 클라이언트 Gradle 프로젝트에 포함하지 않습니다.

## Mapbox 공개 액세스 토큰

지도 SDK를 연결하기 전에 `client/local.properties`에 아래 값을 추가한다. 이 파일은 Git에서 제외된다.

```properties
MAPBOX_ACCESS_TOKEN=발급받은_공개_액세스_토큰
```

Android 앱에서는 `BuildConfig.MAPBOX_ACCESS_TOKEN`으로 읽는다. 실제 Mapbox SDK 의존성과 지도 화면 연결은 다음 단계에서 추가한다.

## 현재 검증 상태

- JDK 21: 로컬 확인 완료
- Gradle Wrapper: `./gradlew --version`으로 9.5.0 확인
- Android SDK 36: `/Users/chohs4164/Library/Android/sdk/platforms/android-36` 확인
- Xcode 전체 설치: 현재 Command Line Tools만 활성화되어 있음
- Android Debug 빌드: `./gradlew :androidApp:assembleDebug` 성공
- iOS 실제 빌드: Xcode 전체 설치 후 수행
