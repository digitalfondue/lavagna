package io.lavagna.web.security.login.oauth;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.model.OAuthConfig;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Token;
import org.scribe.oauth.OAuth20ServiceImpl;

class OAuth2ServiceImplCustomSignRequest extends OAuth20ServiceImpl {

    public OAuth2ServiceImplCustomSignRequest(DefaultApi20 api, OAuthConfig config) {
        super(api, config);
    }

    @Override
    public void signRequest(Token accessToken, OAuthRequest request) {
        request.addHeader("Authorization", "token " + accessToken.getToken());
    }
}
