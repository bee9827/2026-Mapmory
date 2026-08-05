# Mapmory 클라이언트 환경

## 디렉터리 책임

```text
client/androidApp  Android 진입점 및 Android 전용 코드
client/iosApp      Xcode 진입점 및 iOS 전용 코드
client/shared      Compose Multiplatform 공통 UI·상태·도메인 코드
```

## 기준 버전

| 항목 | 기준 |
| --- | --- |
| Kotlin | 2.4.10 |
| Compose Multiplatform | 1.11.1 |
| Android Gradle Plugin | 9.1.0 |
| Gradle | 9.5.0 (Wrapper로 고정 예정) |
| JDK | 21 |
| Android compileSdk | 36 |
| Android targetSdk | 36 |
| Android minSdk | 28 |
| iOS Deployment Target | 16.0 |
| Xcode | 26.4 계열 |

Kotlin 2.4.10의 공식 KMP 호환 범위에 맞추기 위해 AGP 9.1.0을 선택했습니다. AGP 9.3.0은 이 Kotlin 버전과 공식 호환 범위를 벗어나므로 사용하지 않습니다.

## 빌드 명령

Gradle Wrapper를 추가한 뒤 다음 명령으로 Android와 공통 모듈을 검증합니다.

```bash
./gradlew :client:shared:compileKotlinAndroid
./gradlew :client:androidApp:assembleDebug
```

iOS는 Xcode 프로젝트가 추가된 뒤 Simulator와 실제 기기에서 별도로 검증합니다.

## 현재 검증 상태

- JDK 21: 로컬 확인 완료
- Gradle Wrapper: 아직 추가하지 않음
- Android SDK 36: 로컬 확인 필요
- Xcode 전체 설치: 현재 Command Line Tools만 활성화되어 있음
- Android/iOS 실제 빌드: 환경 설치 후 수행
