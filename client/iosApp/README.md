# Mapmory iOS 앱

이 디렉터리는 Xcode 앱 진입점입니다. 공통 Compose Multiplatform 코드는 `../shared`에서 framework로 빌드하고, iOS 앱은 이 framework를 연결합니다.

## 초기 설정

1. Xcode 26.4 이상 설치
2. iOS Deployment Target 16.0 설정
3. Xcode에서 `Mapmory.xcodeproj`를 열기
4. 실제 기기 빌드 시 Signing Team과 Bundle Identifier 설정
5. `Mapmory` scheme으로 Simulator 또는 실제 기기 빌드

## 미디어 권한

- 카메라: `NSCameraUsageDescription`
- 전체 사진 보관함 읽기/쓰기: `NSPhotoLibraryUsageDescription`
- 사진 보관함에 추가만 하기: `NSPhotoLibraryAddUsageDescription`

권한은 앱 실행 시 자동으로 요청하지 않고, 사용자가 카메라나 갤러리 기능을 선택한 시점에
`MediaPermissionManager`를 통해 요청한다. `.limited`는 사용자가 일부 사진만 허용한 상태이므로 전체 허용과
구분해야 한다.

Xcode의 Build Phase가 Gradle `:shared:embedAndSignAppleFrameworkForXcode` 작업을 실행해 공통 Compose UI를
`Shared.framework`로 연결한다.
