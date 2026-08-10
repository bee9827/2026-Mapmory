package com.mapmory.backend.common;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(
        useDefaultFilters = false,
        properties = "spring.mvc.problemdetails.enabled=true"
)
@Import({ProblemDetailFactory.class, SpringMvcProblemDetailTest.MethodRestrictedController.class})
@DisplayName("Spring MVC 기본 Problem Details")
class SpringMvcProblemDetailTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("지원하지 않는 HTTP 메서드는 405 Problem Details를 반환한다")
    void returnsProblemDetailWhenHttpMethodIsNotSupported() throws Exception {
        mockMvc.perform(post("/test/method-restricted"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.instance").value("/test/method-restricted"));
    }

    @RestController
    static class MethodRestrictedController {

        @GetMapping("/test/method-restricted")
        void get() {
        }
    }
}
