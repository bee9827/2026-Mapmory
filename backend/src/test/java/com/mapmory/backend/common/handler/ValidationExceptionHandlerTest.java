package com.mapmory.backend.common.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mapmory.backend.common.ProblemDetailFactory;
import com.mapmory.backend.common.exception.FieldErrorDetail;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

class ValidationExceptionHandlerTest {

    private MockMvc mockMvc;
    private ValidationExceptionHandler handler;

    @BeforeEach
    void setUp() {
        ProblemDetailFactory problemDetailFactory = new ProblemDetailFactory();
        handler = new ValidationExceptionHandler(problemDetailFactory);
        mockMvc = MockMvcBuilders.standaloneSetup(new ValidationController())
                .setControllerAdvice(handler)
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

    @Test
    void returnsInternalServerErrorForInvalidReturnValue() {
        HandlerMethodValidationException exception = mock(HandlerMethodValidationException.class);
        when(exception.isForReturnValue()).thenReturn(true);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test/return-value");

        ResponseEntity<ProblemDetail> response = handler.handleMethodValidation(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo("요청을 처리하는 중 오류가 발생했습니다.");
    }

    @Test
    @SuppressWarnings("unchecked")
    void includesCrossParameterErrors() {
        HandlerMethodValidationException exception = mock(HandlerMethodValidationException.class);
        MessageSourceResolvable crossParameterError = mock(MessageSourceResolvable.class);
        when(exception.isForReturnValue()).thenReturn(false);
        when(exception.getParameterValidationResults()).thenReturn(List.of());
        when(exception.getCrossParameterValidationResults()).thenReturn(List.of(crossParameterError));
        when(crossParameterError.getDefaultMessage()).thenReturn("요청 인자 조합이 올바르지 않습니다.");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/test/cross-parameter");

        ResponseEntity<ProblemDetail> response = handler.handleMethodValidation(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        List<FieldErrorDetail> errors = (List<FieldErrorDetail>) response.getBody()
                .getProperties()
                .get("errors");
        assertThat(errors).containsExactly(
                new FieldErrorDetail("arguments", "요청 인자 조합이 올바르지 않습니다.")
        );
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
