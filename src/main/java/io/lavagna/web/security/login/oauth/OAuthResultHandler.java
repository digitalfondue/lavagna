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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.lavagna.web.security.Redirector;
import io.lavagna.web.security.SecurityConfiguration.SessionHandler;
import io.lavagna.web.security.SecurityConfiguration.User;
import io.lavagna.web.security.SecurityConfiguration.Users;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;
import org.springframework.web.util.UriUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.removeStart;

public interface OAuthResultHandler {

	void handleAuthorizationUrl(HttpServletRequest req, HttpServletResponse resp) throws IOException;

	void handleCallback(HttpServletRequest req, HttpServletResponse resp) throws IOException;

	class OAuthResultHandlerAdapter implements OAuthResultHandler {

	    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

		private final String provider;
		private final String profileUrl;
		private final Class<? extends RemoteUserProfile> profileClass;
		private final String verifierParamName;

		private final Users users;
		private final String errorPage;
		private final SessionHandler sessionHandler;
		protected final OAuthService oauthService;
		private final OAuthRequestBuilder reqBuilder;

		OAuthResultHandlerAdapter(String provider, String profileUrl, Class<? extends RemoteUserProfile> profileClass,
				String verifierParamName, Users users, SessionHandler sessionHandler, String errorPage, OAuthService oauthService,
				OAuthRequestBuilder reqBuilder) {
			this.provider = provider;
			this.profileUrl = profileUrl;
			this.profileClass = profileClass;
			this.verifierParamName = verifierParamName;
			//
			this.users = users;
			this.sessionHandler = sessionHandler;
			this.errorPage = errorPage;
			this.oauthService = oauthService;
			this.reqBuilder = reqBuilder;
		}

		private String stateForAttribute() {
			return "EXPECTED_STATE_FOR_" + provider;
		}

		@Override
		public void handleAuthorizationUrl(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			// scribe does not support out of the box the state parameter, must
			// be overridden to be removed
			String state = UUID.randomUUID().toString();
			saveStateAndRequestUrlParameter(req, state);
			resp.sendRedirect(oauthService.getAuthorizationUrl(null) + "&state=" + state);
		}

		protected void saveStateAndRequestUrlParameter(HttpServletRequest req, String state)
				throws UnsupportedEncodingException {
			req.getSession().setAttribute(stateForAttribute(), state);
			req.getSession().setAttribute("rememberMe-" + state, req.getParameter("rememberMe"));

			String reqUrl = req.getParameter("reqUrl");
			if (reqUrl != null) {
				req.getSession().setAttribute("reqUrl-" + state, UriUtils.decode(reqUrl, "UTF-8"));
			}
		}

		// only for services that support the state parameter, must be
		// overridden to be ignored
		protected boolean validateStateParam(HttpServletRequest req) {
			String stateParam = req.getParameter("state");
			String expectedState = (String) req.getSession().getAttribute(stateForAttribute());
			req.getSession().removeAttribute(stateForAttribute());
			return expectedState != null && expectedState.equals(stateParam);
		}

		@Override
		public void handleCallback(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			String state = (String) req.getSession().getAttribute(stateForAttribute());
			String reqUrl = (String) req.getSession().getAttribute("reqUrl-" + state);
			req.setAttribute("rememberMe", req.getSession().getAttribute("rememberMe-" + state));
			req.getSession().removeAttribute("reqUrl-" + state);
			req.getSession().removeAttribute("rememberMe-" + state);

			if (!validateStateParam(req)) {
				Redirector.sendRedirect(req, resp, req.getContextPath() + "/" + removeStart(errorPage, "/"), Collections.<String, List<String>> emptyMap());
				return;
			}

			// verify token
			Verifier verifier = new Verifier(req.getParameter(verifierParamName));
			Token accessToken = oauthService.getAccessToken(reqToken(req), verifier);

			// fetch user profile
			OAuthRequest oauthRequest = reqBuilder.req(Verb.GET, profileUrl);
			oauthService.signRequest(accessToken, oauthRequest);
			Response oauthResponse = oauthRequest.send();
			RemoteUserProfile profile = GSON.fromJson(oauthResponse.getBody(), profileClass);

			if (profile.valid(users, provider)) {
				String url = Redirector.cleanupRequestedUrl(reqUrl, req);
				User user = users.findUserByName(provider, profile.username());
				sessionHandler.setUser(user.getId(), user.isAnonymous(), req, resp);
				Redirector.sendRedirect(req, resp, url, Collections.<String, List<String>> emptyMap());
			} else {
				Redirector.sendRedirect(req, resp, req.getContextPath() + "/" + removeStart(errorPage, "/"), Collections.<String, List<String>> emptyMap());
			}
		}

		protected Token reqToken(HttpServletRequest req) {
			return null;
		}
	}

	public static class OAuthRequestBuilder {

		public OAuthRequest req(Verb verb, String url) {
			return new OAuthRequest(verb, url);
		}
	}

	interface RemoteUserProfile {
		boolean valid(Users users, String provider);

		String username();
	}
}
