package com.mapmory.backend.common.handler;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mapmory.backend.common.ProblemDetailFactory;
import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.common.exception.ErrorCode;
import com.mapmory.backend.common.exception.ErrorKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@DisplayName("비즈니스 예외 처리기")
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
    @DisplayName("오류 코드 설정에 맞는 Problem Details를 반환한다")
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

    @RestController
    private static class BusinessExceptionController {

        @GetMapping("/test/business-exception")
        void throwBusinessException() {
            throw new BusinessException(TestErrorCode.TRAVEL_RECORD_NOT_FOUND);
        }
    }

    private enum TestErrorCode implements ErrorCode {
        TRAVEL_RECORD_NOT_FOUND;

        @Override
        public ErrorKind kind() {
            return ErrorKind.NOT_FOUND;
        }

        @Override
        public String code() {
            return name();
        }

        @Override
        public String title() {
            return "여행 기록을 찾을 수 없습니다.";
        }

        @Override
        public String detail() {
            return "요청한 여행 기록이 존재하지 않습니다.";
        }
    }
}
