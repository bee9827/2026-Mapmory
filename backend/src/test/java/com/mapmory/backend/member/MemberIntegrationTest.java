package com.mapmory.backend.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.willReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.mapmory.backend.IntegrationTest;
import com.mapmory.backend.auth.application.model.SocialIdentity;
import com.mapmory.backend.auth.infrastructure.google.GoogleIdentityAdapter;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@AutoConfigureMockMvc
class MemberIntegrationTest extends IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @MockitoSpyBean
    private GoogleIdentityAdapter googleIdentityAdapter;

    @Test
    void 내_이름을_조회한다() throws Exception {
        String accessToken = guestAccessToken();

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(Matchers.matchesPattern("회원\\d{5}")));
    }

    @Test
    void 내_이름을_바꾸면_앞뒤_공백을_지우고_저장한다() throws Exception {
        String accessToken = guestAccessToken();

        rename(accessToken, "  티온 ")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("티온"));

        mockMvc.perform(get("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(jsonPath("$.data.name").value("티온"));
    }

    @Test
    void 이름이_비었거나_20자를_넘으면_400이다() throws Exception {
        String accessToken = guestAccessToken();

        rename(accessToken, " ")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        rename(accessToken, "가".repeat(21))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/api/v1/members/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 로그인하지_않으면_401이다() throws Exception {
        mockMvc.perform(get("/api/v1/members/me"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/api/v1/members/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"티온\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 게스트가_바꾼_이름은_이름을_주지_않는_제공자로_승격해도_유지된다() throws Exception {
        String accessToken = guestAccessToken();
        rename(accessToken, "티온");
        willReturn(new SocialIdentity("google-sub-rename", null)).given(googleIdentityAdapter).verify(anyString());

        mockMvc.perform(post("/api/v1/auth/login/google")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"google-id-token\"}"))
                .andExpect(status().isOk());

        assertThat(memberRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "google-sub-rename")
                .orElseThrow().getName()).isEqualTo("티온");
    }

    private String guestAccessToken() throws Exception {
        return JsonPath.read(
                mockMvc.perform(post("/api/v1/auth/login/guest"))
                        .andReturn().getResponse().getContentAsString(),
                "$.data.accessToken");
    }

    private ResultActions rename(String accessToken, String name)
            throws Exception {
        return mockMvc.perform(patch("/api/v1/members/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"" + name + "\"}"));
    }
}
