# Mapmory iOS 앱

이 디렉터리는 Xcode 앱 진입점입니다. 공통 Compose Multiplatform 코드는 `../shared`에서 framework로 빌드하고, iOS 앱은 이 framework를 연결합니다.

## 초기 설정

1. Xcode 26.4 계열 설치
2. iOS Deployment Target 16.0 설정
3. Gradle로 `shared`의 iOS framework 생성
4. Xcode에서 Simulator와 실제 기기 빌드

Xcode 프로젝트 파일은 팀의 Apple Developer Bundle ID와 서명 팀이 정해진 뒤 추가합니다.
