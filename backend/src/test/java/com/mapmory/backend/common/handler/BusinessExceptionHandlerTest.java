package com.mapmory.backend.common.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mapmory.backend.common.ProblemDetailFactory;
import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.common.exception.ErrorCode;
import com.mapmory.backend.common.exception.ErrorKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@ExtendWith(OutputCaptureExtension.class)
class BusinessExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProblemDetailFactory problemDetailFactory = new ProblemDetailFactory();
        mockMvc = MockMvcBuilders.standaloneSetup(new BusinessExceptionController())
                .setControllerAdvice(new BusinessExceptionHandler(problemDetailFactory))
                .build();
    }

    @Test
    void returnsConfiguredProblemDetail() throws Exception {
        mockMvc.perform(get("/test/business-exception"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").doesNotExist())
                .andExpect(jsonPath("$.title").value("여행 기록을 찾을 수 없습니다."))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("요청한 여행 기록이 존재하지 않습니다."))
                .andExpect(jsonPath("$.instance").value("/test/business-exception"))
                .andExpect(jsonPath("$.code").value("TRAVEL_RECORD_NOT_FOUND"));
    }

    @Test
    void 서비스_불가_예외는_ERROR와_원인을_기록한다(CapturedOutput output) throws Exception {
        mockMvc.perform(get("/test/service-unavailable"))
                .andExpect(status().isServiceUnavailable());

        assertThat(output)
                .contains("ERROR")
                .contains("code=SERVICE_UNAVAILABLE")
                .contains("upstream failure");
    }

    @RestController
    private static class BusinessExceptionController {

        @GetMapping("/test/business-exception")
        void throwBusinessException() {
            throw new BusinessException(TestErrorCode.TRAVEL_RECORD_NOT_FOUND);
        }

        @GetMapping("/test/service-unavailable")
        void throwServiceUnavailable() {
            throw new BusinessException(
                    TestErrorCode.SERVICE_UNAVAILABLE,
                    TestErrorCode.SERVICE_UNAVAILABLE.detail(),
                    new IllegalStateException("upstream failure")
            );
        }
    }

    private enum TestErrorCode implements ErrorCode {
        TRAVEL_RECORD_NOT_FOUND(ErrorKind.NOT_FOUND, "여행 기록을 찾을 수 없습니다.",
                "요청한 여행 기록이 존재하지 않습니다."),
        SERVICE_UNAVAILABLE(ErrorKind.SERVICE_UNAVAILABLE, "서비스를 사용할 수 없습니다.",
                "외부 서비스 장애로 요청을 처리할 수 없습니다.");

        private final ErrorKind kind;
        private final String title;
        private final String detail;

        TestErrorCode(ErrorKind kind, String title, String detail) {
            this.kind = kind;
            this.title = title;
            this.detail = detail;
        }

        @Override
        public ErrorKind kind() {
            return kind;
        }

        @Override
        public String code() {
            return name();
        }

        @Override
        public String title() {
            return title;
        }

        @Override
        public String detail() {
            return detail;
        }
    }
}
