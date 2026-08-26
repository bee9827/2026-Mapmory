package com.mapmory.backend.auth.security;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginMemberArgumentResolver loginMemberArgumentResolver;
    private final String[] waitlistAllowedOrigins;

    public WebMvcConfig(
            LoginMemberArgumentResolver loginMemberArgumentResolver,
            @Value("${waitlist.cors.allowed-origins}") String[] waitlistAllowedOrigins
    ) {
        this.loginMemberArgumentResolver = loginMemberArgumentResolver;
        this.waitlistAllowedOrigins = waitlistAllowedOrigins;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/waitlist")
                .allowedOrigins(waitlistAllowedOrigins)
                .allowedMethods("POST")
                .allowedHeaders("Content-Type")
                .maxAge(3600);
    }
}
