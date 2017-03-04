/**
 * This file is part of lavagna.
 *
 * lavagna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * lavagna is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with lavagna.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.lavagna.web.security.login.oauth;

import org.scribe.builder.api.DefaultApi20;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.model.*;
import org.scribe.oauth.OAuth20ServiceImpl;
import org.scribe.oauth.OAuthService;
import org.scribe.services.Base64Encoder;

import java.nio.charset.StandardCharsets;

import static io.lavagna.web.security.login.oauth.Utils.encode;

/**
 * <pre>
 * https://confluence.atlassian.com/display/BITBUCKET/OAuth+on+Bitbucket
 * </pre>
 */
class Bitbucket20Api extends DefaultApi20 {

    @Override
    public String getAccessTokenEndpoint() {
        return "https://bitbucket.org/site/oauth2/access_token";
    }

    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        return "https://bitbucket.org/site/oauth2/authorize?client_id=" + encode(config.getApiKey()) + "&redirect_uri="
                + encode(config.getCallback()) + "&response_type=code";
    }

    @Override
    public Verb getAccessTokenVerb() {
        return Verb.POST;
    }

    @Override
    public AccessTokenExtractor getAccessTokenExtractor() {
        return new JsonTokenExtractor();
    }

    @Override
    public OAuthService createService(final OAuthConfig config) {
        return new OAuth20ServiceImpl(this, config) {
            @Override
            public Token getAccessToken(Token requestToken, Verifier verifier) {
                //basic auth, as described at https://developer.atlassian.com/static/bitbucket/concepts/oauth2.html
                OAuthRequest request = new OAuthRequest(getAccessTokenVerb(), getAccessTokenEndpoint());

                //basic auth
                request.addHeader("Authorization", "Basic "+Base64Encoder.getInstance().encode((config.getApiKey()+":"+config.getApiSecret()).getBytes(StandardCharsets.UTF_8)));

                request.addBodyParameter(OAuthConstants.CODE, verifier.getValue());
                request.addBodyParameter(OAuthConstants.REDIRECT_URI, config.getCallback());

                request.addBodyParameter("grant_type", "authorization_code");
                Response response = request.send();
                return getAccessTokenExtractor().extract(response.getBody());
            }
        };
    }
}
