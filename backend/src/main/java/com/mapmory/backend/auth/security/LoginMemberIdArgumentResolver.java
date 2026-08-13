package com.mapmory.backend.auth.security;

import com.mapmory.backend.auth.exception.AuthErrorCode;
import com.mapmory.backend.common.exception.BusinessException;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * {@link LoginMemberId}가 붙은 Long 파라미터에 SecurityContext의 memberId를 주입한다.
 *
 * 인증 정보가 없거나 형식이 예상과 다르면 인증 오류로 처리한다.
 * (보호된 엔드포인트라면 시큐리티 단계에서 이미 걸러지지만, 방어적으로 확인한다.)
 */
@Component
public class LoginMemberIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMemberId.class)
                && parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Long memberId)) {
            throw new BusinessException(AuthErrorCode.INVALID_ACCESS_TOKEN);
        }
        return memberId;
    }
}
