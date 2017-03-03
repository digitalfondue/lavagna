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

import org.apache.commons.lang3.StringUtils;
import org.scribe.builder.api.DefaultApi20;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.model.*;
import org.scribe.oauth.OAuth20ServiceImpl;
import org.scribe.oauth.OAuthService;

import static io.lavagna.web.security.login.oauth.Utils.encode;

// based from doc: http://doc.gitlab.com/ce/api/oauth2.html
public class Gitlab20Api extends DefaultApi20 {

    private final String baseUrl;

    public Gitlab20Api(String baseUrl) {
        this.baseUrl = StringUtils.removeEnd(baseUrl, "/");
    }

    @Override
    public String getAccessTokenEndpoint() {
        // https://gitlab.com/oauth/token
        return baseUrl+"/oauth/token";
    }

    @Override
    public String getAuthorizationUrl(OAuthConfig config) {
        // https://gitlab.com/oauth/authorize
        return baseUrl+"/oauth/authorize?client_id="+encode(config.getApiKey())+"&redirect_uri="+encode(config.getCallback())+"&response_type=code";
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
                OAuthRequest request = new OAuthRequest(getAccessTokenVerb(), getAccessTokenEndpoint());
                request.addBodyParameter(OAuthConstants.CLIENT_ID, config.getApiKey());
                request.addBodyParameter(OAuthConstants.CLIENT_SECRET, config.getApiSecret());
                request.addBodyParameter(OAuthConstants.CODE, verifier.getValue());
                request.addBodyParameter(OAuthConstants.REDIRECT_URI, config.getCallback());
                // well, the doc is not correct, see https://github.com/gitlabhq/gitlabhq/issues/9141
                request.addBodyParameter("grant_type", "authorization_code");
                Response response = request.send();
                return getAccessTokenExtractor().extract(response.getBody());
            }
        };
    }

}
