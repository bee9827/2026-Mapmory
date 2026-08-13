package com.mapmory.backend.auth.security;

import com.mapmory.backend.common.ProblemDetailFactory;
import com.mapmory.backend.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * 시큐리티 필터 단계의 오류를 기존 ProblemDetails 포맷으로 직접 응답한다.
 *
 * 필터에서 발생한 예외는 @RestControllerAdvice가 잡지 못하므로,
 * ProblemDetailFactory로 만든 본문을 HttpServletResponse에 직접 기록한다.
 */
@Component
public class ProblemResponseWriter {

    private final ProblemDetailFactory problemDetailFactory;
    private final JsonMapper jsonMapper;

    public ProblemResponseWriter(ProblemDetailFactory problemDetailFactory, JsonMapper jsonMapper) {
        this.problemDetailFactory = problemDetailFactory;
        this.jsonMapper = jsonMapper;
    }

    public void write(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            ErrorCode errorCode
    ) throws IOException {
        ProblemDetail body = problemDetailFactory
                .from(status, errorCode, errorCode.detail(), request)
                .getBody();

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(jsonMapper.writeValueAsString(body));
    }
}
