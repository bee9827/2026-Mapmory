package com.mapmory.backend.common.handler;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mapmory.backend.common.ProblemDetailFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

class UnexpectedExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProblemDetailFactory problemDetailFactory = new ProblemDetailFactory();
        mockMvc = MockMvcBuilders.standaloneSetup(new UnexpectedExceptionController())
                .setControllerAdvice(new UnexpectedExceptionHandler(problemDetailFactory))
                .build();
    }

    @Test
    void hidesInternalExceptionMessage() throws Exception {
        mockMvc.perform(get("/test/unexpected-exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("요청을 처리하는 중 오류가 발생했습니다."))
                .andExpect(jsonPath("$.detail").value(not("sensitive message")));
    }

    @RestController
    private static class UnexpectedExceptionController {

        @GetMapping("/test/unexpected-exception")
        void throwUnexpectedException() {
            throw new IllegalStateException("sensitive message");
        }
    }
}
