package com.mapmory.backend.auth.oauth.google;

import com.mapmory.backend.auth.oauth.SocialUser;
import com.mapmory.backend.auth.oauth.SocialUserFetcher;
import com.mapmory.backend.member.AuthProvider;
import org.springframework.stereotype.Component;

@Component
public class GoogleUserFetcher implements SocialUserFetcher {

    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    public GoogleUserFetcher(GoogleIdTokenVerifier googleIdTokenVerifier) {
        this.googleIdTokenVerifier = googleIdTokenVerifier;
    }

    @Override
    public AuthProvider provider() {
        return AuthProvider.GOOGLE;
    }

    @Override
    public SocialUser fetch(String idToken) {
        GoogleUser googleUser = googleIdTokenVerifier.verify(idToken);
        return new SocialUser(googleUser.subject(), googleUser.name());
    }
}
