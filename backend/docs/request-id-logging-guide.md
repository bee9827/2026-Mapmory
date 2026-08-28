# Request ID로 하나의 요청 로그 찾기

## 한 줄 설명

Request ID는 서버에 들어온 요청 하나에 붙이는 **번호표**다.

실제 서버에서는 여러 사용자의 요청을 동시에 처리하기 때문에 로그가 서로 섞인다. 각 로그에
같은 Request ID를 붙이면, 나중에 특정 요청에서 발생한 로그만 골라서 볼 수 있다.

이 문서에서는 이해하기 쉽도록 짧은 값인 `REQ-123`을 사용한다. 현재 Mapmory의 실제 구현은
충돌 가능성을 줄이기 위해 UUID를 사용한다.

## 요청 처리 예시

사용자가 여행 기록 생성을 요청한다고 가정한다.

```http
POST /api/v1/travel-records
X-Request-Id: REQ-123
```

서버는 이 요청을 처리하면서 만들어지는 모든 로그에 같은 번호표를 붙인다.

```text
14:00:01 [requestId:REQ-123] 여행 기록 생성 요청
14:00:01 [requestId:REQ-123] 회원 조회 완료
14:00:02 [requestId:REQ-123] 이미지 URL 저장 시작
14:00:02 [requestId:REQ-123] ERROR 데이터베이스 저장 실패
```

응답에도 같은 Request ID를 넣는다.

```http
HTTP/1.1 500 Internal Server Error
X-Request-Id: REQ-123
```

사용자가 오류를 제보할 때 다음과 같이 Request ID를 전달할 수 있다.

```text
여행 기록 저장에 실패했습니다. 응답의 Request ID는 REQ-123입니다.
```

개발자는 `REQ-123`을 검색해서 해당 요청의 처리 과정과 실패 원인을 찾는다.

## Request ID가 필요한 이유

두 사용자의 요청이 동시에 들어오면 실제 로그는 다음처럼 섞인다.

```text
14:00:01 [requestId:REQ-123] 여행 기록 생성 요청
14:00:01 [requestId:REQ-456] 지도 조회 요청
14:00:01 [requestId:REQ-123] 회원 조회 완료
14:00:02 [requestId:REQ-456] 지도 조회 완료
14:00:02 [requestId:REQ-123] ERROR DB 저장 실패
```

여기서 `REQ-123`만 검색하면 여행 기록 생성 요청의 로그만 남는다.

```text
[requestId:REQ-123] 여행 기록 생성 요청
[requestId:REQ-123] 회원 조회 완료
[requestId:REQ-123] ERROR DB 저장 실패
```

이를 통해 다음 흐름을 확인할 수 있다.

```text
요청 도착 → 회원 조회 → 이미지 저장 시도 → DB 저장 실패
```

## Mapmory 코드에서의 동작

Mapmory의 `RequestIdFilter`가 요청 시작 시 Request ID를 MDC에 넣는다.

```java
try (MDC.MDCCloseable ignored = MDC.putCloseable("requestId", requestId)) {
    filterChain.doFilter(request, response);
}
```

요청 처리 중에는 평소처럼 로그만 작성하면 된다.

```java
log.info("여행 기록 생성 요청");
log.error("데이터베이스 저장 실패", exception);
```

개발자가 로그를 작성할 때마다 Request ID를 직접 넘길 필요는 없다. MDC에 저장된 값이 로그
설정에 의해 자동으로 추가된다.

```yaml
logging:
  pattern:
    level: "%5p [requestId:%X{requestId:-}]"
```

요청 처리가 끝나면 `MDC.putCloseable()`이 값을 제거한다. 이를 제거하지 않으면 서버가 같은
스레드를 다음 요청에 재사용할 때 이전 Request ID가 다음 사용자의 로그에 잘못 붙을 수 있다.

## CloudWatch에서 검색하기

사용자가 알려준 Request ID가 `REQ-123`이라면 CloudWatch Logs Insights에서 다음과 같이
검색한다.

```sql
fields @timestamp, @message
| filter @message like /REQ-123/
| sort @timestamp asc
```

검색 결과를 시간순으로 읽으면 요청이 어느 단계까지 성공했고 어디에서 실패했는지 확인할 수
있다.

운영 로그가 JSON 형식으로 수집되어 `requestId`가 별도 필드로 인식되는 경우에는 다음처럼 더
정확하게 검색할 수 있다.

```sql
fields @timestamp, requestId, @message
| filter requestId = "REQ-123"
| sort @timestamp asc
```

## 로그와 메트릭에서의 차이

Request ID는 개별 요청을 찾기 위한 **로그 검색용 값**이다.

```text
로그:    requestId 사용 가능
메트릭:  requestId 사용 금지
```

Request ID는 요청마다 값이 달라진다. 이를 Micrometer나 Prometheus 메트릭 태그에 넣으면 요청
수만큼 시계열이 만들어져 메모리 사용량과 모니터링 비용이 크게 늘어난다.

```java
// 잘못된 예: 요청마다 새로운 메트릭 시계열이 만들어진다.
counter.tag("requestId", requestId);
```

메트릭에서는 HTTP 메서드, 상태 코드, 정해진 작업 이름처럼 값의 종류가 제한된 태그만 사용한다.

## 현재 구현에서 확인할 점

이 문서의 `REQ-123`은 이해를 돕기 위한 예시다. 현재 Mapmory는 `X-Request-Id`로 UUID만
허용한다.

```text
예시용 값: REQ-123
실제 값:   550e8400-e29b-41d4-a716-446655440000
```

실제 서버에 `REQ-123`을 전달하면 유효하지 않은 값으로 판단하고 새로운 UUID를 만들어
응답한다. 운영에서는 중복 가능성이 매우 낮은 현재 UUID 방식을 유지한다.
