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

import io.lavagna.web.security.SecurityConfiguration.SessionHandler;
import io.lavagna.web.security.SecurityConfiguration.Users;
import io.lavagna.web.security.login.oauth.OAuthResultHandler.OAuthResultHandlerAdapter;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

public abstract class AbstractOAuth1Handler extends OAuthResultHandlerAdapter {

	AbstractOAuth1Handler(String provider, String profileUrl, Class<? extends RemoteUserProfile> profileClass,
			String verifierParamName, Users users, SessionHandler sessionHandler, String errorPage, OAuthService oauthService,
			OAuthRequestBuilder reqBuilder) {
		super(provider, profileUrl, profileClass, verifierParamName, users, sessionHandler, errorPage, oauthService,
				reqBuilder);
	}

	// ignore state parameter as it's not present
	@Override
	protected boolean validateStateParam(HttpServletRequest req) {
		return true;
	}

	@Override
	public void handleAuthorizationUrl(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		String state = UUID.randomUUID().toString();
		saveStateAndRequestUrlParameter(req, state);

		Token reqToken = oauthService.getRequestToken();
		req.getSession().setAttribute(getClass().getName(), reqToken);
		resp.sendRedirect(oauthService.getAuthorizationUrl(reqToken));
	}

	@Override
	protected Token reqToken(HttpServletRequest req) {
		Token reqToken = (Token) req.getSession().getAttribute(getClass().getName());
		req.getSession().removeAttribute(getClass().getName());
		return reqToken;
	}
}
