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

## 문서

- [ADR 0006: API 오류 응답에 Problem Details를 사용한다](docs/adr/0006-use-problem-details-for-api-errors.md)
- [여행 기록 저장 및 목록 조회 방식](docs/travel-record-storage-and-query.md)
- [Testcontainers 테스트 성능 개선](docs/testcontainers-performance.md)

## 환경변수

필요한 키 목록은 `.env.example` 참고.

**운영 서버**는 `/etc/mapmory.env` 에서 읽는다 (systemd EnvironmentFile).
새 환경변수가 추가되면 배포 전에 이 파일도 갱신해야 한다.

```bash
sudo nano /etc/mapmory.env
sudo systemctl restart mapmory
```

⚠️ `export` 없이 `키=값` 형식으로 작성한다. 따옴표도 붙이지 않는다.