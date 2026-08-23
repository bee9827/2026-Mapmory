package com.mapmory.backend.common.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestIdFilterTest {

    private final RequestIdFilter filter = new RequestIdFilter();

    @AfterEach
    void clearMdc() {
        MDC.remove(RequestIdFilter.REQUEST_ID_MDC_KEY);
    }

    @Test
    void 요청_ID가_없으면_생성하여_MDC와_응답에_추가한다() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            String requestId = MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY);
            assertThat(requestId).isNotNull();
            assertThat(UUID.fromString(requestId).toString()).isEqualTo(requestId);
        });

        assertThat(response.getHeader(RequestIdFilter.REQUEST_ID_HEADER)).isNotNull();
        assertThat(MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY)).isNull();
    }

    @Test
    void 유효한_요청_ID는_그대로_사용한다() throws Exception {
        String requestId = UUID.randomUUID().toString();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestIdFilter.REQUEST_ID_HEADER, requestId);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                assertThat(MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY)).isEqualTo(requestId));

        assertThat(response.getHeader(RequestIdFilter.REQUEST_ID_HEADER)).isEqualTo(requestId);
    }

    @Test
    void 유효하지_않은_요청_ID는_새로_생성한다() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestIdFilter.REQUEST_ID_HEADER, "invalid-request-id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
        });

        String responseRequestId = response.getHeader(RequestIdFilter.REQUEST_ID_HEADER);
        assertThat(responseRequestId).isNotEqualTo("invalid-request-id");
        assertThat(UUID.fromString(responseRequestId).toString()).isEqualTo(responseRequestId);
    }

    @Test
    void 예외가_발생해도_MDC를_정리한다() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            throw new IOException("test failure");
        })).isInstanceOf(IOException.class);

        assertThat(MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY)).isNull();
        assertThat(response.getHeader(RequestIdFilter.REQUEST_ID_HEADER)).isNotNull();
    }
}
