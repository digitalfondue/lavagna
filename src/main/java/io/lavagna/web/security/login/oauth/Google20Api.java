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

import org.apache.commons.lang3.Validate;
import org.scribe.builder.api.DefaultApi20;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.model.*;
import org.scribe.oauth.OAuth20ServiceImpl;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.Preconditions;

import static io.lavagna.web.security.login.oauth.Utils.encode;
import static java.lang.String.format;

/**
 * Google OAuth 2.0 implementation.
 *
 * Initial form taken from :
 *
 * <pre>
 * http://svn.codehaus.org/tynamo/tags/tynamo-federatedaccounts-parent-0.3.0/tynamo-federatedaccounts-scribebasedoauth/src/main/java/org/tynamo/security/federatedaccounts/scribe/google/Google20Api.java
 * </pre>
 *
 * Tynamo is under apache2.0 license.
 *
 * And then duly modified.
 */
class Google20Api extends DefaultApi20 {

	private static final String AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/auth?client_id=%s&redirect_uri=%s&response_type=code&scope=%s";

	@Override
	public String getAccessTokenEndpoint() {
		return "https://accounts.google.com/o/oauth2/token";
	}

	@Override
	public String getAuthorizationUrl(OAuthConfig config) {
		Preconditions.checkValidUrl(config.getCallback(), "Must provide a valid url as callback");
		Validate.isTrue(config.hasScope(), "must contain scope");
		return format(AUTHORIZE_URL, config.getApiKey(), encode(config.getCallback()), encode(config.getScope()));
	}

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
				request.addBodyParameter("grant_type", "authorization_code");
				if (config.hasScope()) {
					request.addBodyParameter(OAuthConstants.SCOPE, config.getScope());
				}
				Response response = request.send();
				return getAccessTokenExtractor().extract(response.getBody());
			}
		};
	}
}
