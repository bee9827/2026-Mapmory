# 이미지 로딩·미리보기 파이프라인

## 문서 목적

위치 기반 사진 추천과 기록 작성 화면에서 사진이 어떤 과정을 거쳐 화면에 표시되는지 정리한다.
현재 적용한 개선의 이유와 아직 남은 기술 부채를 분리해, 다음 성능 개선을 시작할 때 구현을
다시 추적할 수 있도록 한다.

## 현재 결론

- 화면 표시에는 Coil 3 `AsyncImage`를 사용한다.
- 위치 기반 추천에서는 원본 바이트를 보관하지 않고, 축소한 미리보기 바이트만 준비한다.
- 갤러리에서 사용자가 직접 선택한 사진은 기록 저장을 위해 원본 바이트를 유지한다.
- 사진 경로와 원본 파일 자체는 Room에 저장하지 않는다.
- S3 presigned URL 미리보기는 기존 `PhotoPreviewLoader`가 담당하므로 이번 변경에서 중복 구현하지 않았다.

## 전체 흐름

```text
위치 선택
  ↓
MediaStore에서 사진 ID·촬영일 등 메타데이터 조회
  ↓
Room 캐시와 비교해 EXIF GPS가 필요한 사진만 읽음
  ↓
선택한 지역의 Polygon 안에 있는지 판정
  ↓
추천 목록에는 축소된 previewBytes만 전달
  ↓
TripPhotoImage가 Coil AsyncImage로 비동기 표시
```

직접 사진을 추가하는 흐름은 원본이 필요하므로 별도 경로를 사용한다.

```text
갤러리에서 사진 선택
  ↓
ContentResolver로 원본 바이트 조회
  ↓
EXIF 방향 보정 및 1280px 미리보기 생성
  ↓
SelectedPhoto에 previewBytes와 originalBytes를 함께 보관
  ↓
기록 저장 시 originalBytes 업로드
```

## 개선 전과 현재

| 구분 | 기존 방식 | 현재 방식 | 기대 효과 |
| --- | --- | --- | --- |
| 화면 표시 | `ByteArray.decodeToImageBitmap()`를 컴포넌트에서 직접 실행 | Coil `AsyncImage`로 비동기 로딩 | UI 스레드의 직접 디코딩 부담과 화면 생명주기 관리 부담 감소 |
| 추천 사진 읽기 | 원본 입력 스트림을 먼저 전부 `readBytes()`한 뒤 원본을 버림 | Android `ImageDecoder`로 필요한 크기의 미리보기만 생성 | 추천 중 원본 바이트 임시 누적 방지 |
| 추천 미리보기 크기 | picker와 같은 큰 미리보기 기준 사용 | 최대 640px | 추천 목록의 메모리·디코딩 비용 감소 |
| 직접 선택 사진 | 미리보기와 원본을 모두 필요로 함 | 동일하게 원본 유지 | 업로드 동작과 호환 |
| 로딩 상태 | 컴포넌트가 디코딩 성공 여부를 즉시 처리 | Coil의 loading/success/error 콜백과 placeholder 사용 | 로딩 중 빈 칸과 실패 화면 완화 |

Coil의 `AsyncImage`는 Compose 제약 조건에 맞는 크기를 계산하고 비동기 요청의 생명주기를
관리한다. 자세한 동작은 [Coil Compose 문서](https://coil-kt.github.io/coil/compose/)와
[Coil 개요](https://coil-kt.github.io/coil/)를 참고한다.

## `SelectedPhoto`에서 보관하는 값

| 값 | 추천 사진 | 직접 선택 사진 | 용도 |
| --- | --- | --- | --- |
| `id` | MediaStore URI | 선택된 URI | 사진 식별 |
| `displayName` | 파일 표시 이름 | 파일 표시 이름 | 화면 표시 |
| `previewBytes` | 최대 640px JPEG | 최대 1280px JPEG | 목록·미리보기 표시 |
| `latitude`, `longitude` | EXIF GPS | EXIF GPS | 지역 Polygon 비교 |
| `capturedAt` | 촬영 시각 | 촬영 시각 | 기록 표시·정렬 |
| `originalBytes` | `null` | 원본 바이트 | 기록 저장 시 업로드 |

핵심은 Room이 사진 원본을 저장한다는 뜻이 아니다. Room에는 MediaStore 사진의 식별자, 수정
시각, GPS와 같은 재사용 가능한 메타데이터만 저장하고, 화면에 필요한 바이트는 현재 화면의
상태가 보관한다.

## 왜 원본을 추천 단계에서 읽지 않는가

추천 단계에서 필요한 것은 “이 사진이 선택한 지역에 속하는가”와 “목록에 어떤 이미지로
보여줄 것인가”이다. 이 단계에서는 업로드할 원본이 필요하지 않다.

원본을 여러 장 읽으면 사진 한 장의 해상도와 파일 크기만큼 임시 메모리가 증가한다. 반면
추천용 미리보기는 표시 목적에 맞는 최대 크기로 제한할 수 있다. 사용자가 실제로 사진을
기록에 추가한 시점에만 원본을 다시 읽어 업로드 데이터로 사용한다.

따라서 현재 구조는 다음 트레이드오프를 선택한다.

- 추천 화면: 원본 메모리와 불필요한 원본 읽기를 줄인다.
- 사진 추가 시점: 원본을 다시 읽는 짧은 비용을 지불한다.
- 기록 저장: 원본이 필요한 기존 업로드 계약은 유지한다.

## 현재 남은 기술 부채

### 1. 추천용 `ByteArray`를 화면 상태에 보관한다

원본보다는 작지만 추천 페이지마다 JPEG 바이트가 메모리에 남는다. 사진 수가 계속 늘어나면
`LazyVerticalGrid`를 사용해도 상태에 보관한 바이트 자체가 자동으로 사라지는 것은 아니다.

다음 단계에서는 MediaStore URI 또는 iOS `PHAsset` 식별자를 화면 모델에 전달하고, Coil이
필요한 시점에 디코딩하도록 바꾸는 방식을 검토한다. 이때 기록 저장을 위한 원본 재조회와
화면 표시용 미리보기를 분리해야 한다.

### 2. 로컬 미리보기 생성과 Coil 디코딩이 이어서 발생한다

현재 Android 추천 흐름은 `ImageDecoder`로 640px JPEG를 만든 뒤 Coil이 그 JPEG를 다시
디코딩한다. 원본 전체를 화면에 전달하지 않는 안전한 단계적 개선이지만, 최종적으로는
URI 기반 Coil 로더를 사용해 중간 JPEG 생성 비용을 줄일 여지가 있다.

### 3. 원격 S3 미리보기는 별도 로더를 사용한다

`PhotoPreviewLoader`는 presigned URL의 만료·재발급과 기존 캐시 동작을 알고 있다. 이를
당장 Coil 네트워크 로더로 교체하면 URL 만료, 캐시 키, 인증 실패 처리를 다시 설계해야 한다.
원격 이미지가 실제 병목이라는 측정 결과가 생길 때 교체 여부를 판단한다.

### 4. 640px와 1280px 기준은 현재 고정값이다

화면 크기나 실제 이미지 표시 영역에 따라 필요한 해상도는 달라질 수 있다. 이후에는 다음
조건을 비교해 기준을 조정한다.

- 작은 카드와 전체 화면 상세 이미지의 요구 해상도
- 기기별 메모리 사용량
- 첫 표시 시간과 스크롤 중 프레임 드롭
- 이미지 품질에 대한 사용자 피드백

### 5. 시간 측정과 메모리 측정을 분리해야 한다

`MapmoryPhotoPerf` 로그는 MediaStore·EXIF·추천 단계의 시간을 측정하지만, Coil의 메모리
캐시 적중률이나 화면 스크롤 중 메모리 증가까지 설명하지는 않는다. 성능 판단 시 시간,
메모리, 프레임 드롭, 실패율을 별도로 수집해야 한다.

## 다음에 학습할 개념

1. Coil `ImageLoader`, `MemoryCache`, `DiskCache`, `SizeResolver`의 역할
2. Compose에서 `AsyncImage`와 `rememberAsyncImagePainter`의 사용 차이
3. Android `ImageDecoder`의 샘플링·색 공간·EXIF 방향 처리
4. iOS `PHImageManager`의 `targetSize`, `deliveryMode`, 네트워크 접근 동작
5. URI·`PHAsset` 기반 지연 로딩과 원본 업로드 시점 분리
6. Android Studio Memory Profiler와 Macrobenchmark를 이용한 실제 측정

## 검증 방법

```bash
cd /Users/chohs4164/2026-Mapmory/client

# 공통 테스트와 Android 빌드
./gradlew :shared:jvmTest :shared:testAndroidHostTest :androidApp:assembleDebug

# 실제 Android 기기 또는 에뮬레이터의 MediaStore·Room·EXIF 흐름
ANDROID_SERIAL=<serial> ./gradlew :shared:androidConnectedCheck
```

개발 중 사진 추천 시간은 `MapmoryPhotoPerf` 로그로 확인한다.

```bash
adb -s <serial> shell setprop log.tag.MapmoryPhotoPerf DEBUG
adb -s <serial> logcat -v time -s MapmoryPhotoPerf:D
```

비교할 값은 `recommend_total_ms`, `metadata_sync_ms`, `exif_reads`,
`reused_coordinates`, `recommended_photos`이다. 구현을 바꾼 뒤에는 같은 기기·같은 사진
목록·같은 캐시 조건에서 비교해야 하며, 단 한 번의 측정으로 Coil 또는 Room의 효과를
단정하지 않는다.

## 관련 문서

- [사진 추천 로딩 측정 결과](./photo-loading-benchmark.md)
- [사진 메타데이터 자동화 테스트](../testing/photo-metadata-automation.md)
- [모바일 모니터링 기준](../android-monitoring.md)
