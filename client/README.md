# Mapmory Client

Android와 iOS 클라이언트 및 Compose Multiplatform 공통 코드를 관리하는 독립 빌드 프로젝트입니다.

```text
client/
├── androidApp/  # Android 앱과 Android 전용 코드
├── iosApp/      # Xcode 앱과 iOS 전용 코드
├── shared/      # 공통 UI·상태·도메인 코드
├── gradle/      # 클라이언트 의존성 버전
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

클라이언트 빌드는 반드시 이 디렉터리를 기준으로 실행합니다.

```bash
cd client
./gradlew :shared:compileKotlinAndroid
./gradlew :androidApp:assembleDebug
```

세부 환경 기준은 [docs/environment.md](docs/environment.md)를 확인합니다.

CI 실행 범위와 실패 대응은 [docs/ci.md](docs/ci.md)를 확인합니다.

Firebase Analytics 이벤트와 개인정보 기준은 [docs/android-monitoring.md](docs/android-monitoring.md),
App Store 제출은 [docs/app-store-connect-submission-checklist.md](docs/app-store-connect-submission-checklist.md),
App Privacy 응답은 [docs/app-store-privacy-responses.md](docs/app-store-privacy-responses.md)를 확인합니다.

진행 중인 작업은 [docs/development-tasks.md](docs/development-tasks.md)에서 관리합니다.
