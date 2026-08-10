package com.mapmory.backend.common.handler;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mapmory.backend.common.ProblemDetailFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

class ValidationExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProblemDetailFactory problemDetailFactory = new ProblemDetailFactory();
        mockMvc = MockMvcBuilders.standaloneSetup(new ValidationController())
                .setControllerAdvice(new ValidationExceptionHandler(problemDetailFactory))
                .build();
    }

    @Test
    void returnsFieldErrorsForInvalidRequestBody() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[0].field").value("title"))
                .andExpect(jsonPath("$.errors[0].detail").value("제목은 필수입니다."));
    }

    @RestController
    private static class ValidationController {

        @PostMapping("/test/validation")
        void validate(@Valid @RequestBody TestRequest request) {
        }
    }

    private record TestRequest(@NotBlank(message = "제목은 필수입니다.") String title) {
    }
}
