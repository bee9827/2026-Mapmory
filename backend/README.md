# Mapmory Backend

Mapmory API 서버, 인증, 데이터베이스, 파일 저장소 연동을 관리할 독립 빌드 프로젝트입니다.

Backend Gradle Wrapper와 빌드 설정은 이 디렉터리 안에서 독립적으로 관리합니다.

```text
backend/
├── src/
├── gradle/                 # Backend 전용 Gradle Wrapper
├── build.gradle            # Backend 전용 빌드 설정
└── settings.gradle
```

Backend 빌드는 반드시 이 디렉터리를 기준으로 실행합니다.

```bash
cd backend
./gradlew build
```

Windows에서는 `./gradlew` 대신 `./gradlew.bat`을 사용합니다.

빌드가 성공하면 실행 가능한 `build/libs/mapmory-backend.jar`가 생성됩니다.

클라이언트와는 소스 코드를 직접 참조하지 않고 HTTP API 계약으로만 통신합니다.
