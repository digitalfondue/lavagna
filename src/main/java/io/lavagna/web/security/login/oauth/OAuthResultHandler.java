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

import io.lavagna.common.Json;
import io.lavagna.service.UserRepository;
import io.lavagna.web.helper.Redirector;
import io.lavagna.web.helper.UserSession;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;
import org.springframework.web.util.UriUtils;

public interface OAuthResultHandler {

	void handleAuthorizationUrl(HttpServletRequest req, HttpServletResponse resp) throws IOException;

	void handleCallback(HttpServletRequest req, HttpServletResponse resp) throws IOException;

	class OAuthResultHandlerAdapter implements OAuthResultHandler {

		private final String provider;
		private final String profileUrl;
		private final Class<? extends RemoteUserProfile> profileClass;
		private final String verifierParamName;

		private final UserRepository userRepository;
		private final String errorPage;
		protected final OAuthService oauthService;
		private final OAuthRequestBuilder reqBuilder;

		OAuthResultHandlerAdapter(String provider, String profileUrl, Class<? extends RemoteUserProfile> profileClass,
				String verifierParamName, UserRepository userRepository, String errorPage, OAuthService oauthService,
				OAuthRequestBuilder reqBuilder) {
			this.provider = provider;
			this.profileUrl = profileUrl;
			this.profileClass = profileClass;
			this.verifierParamName = verifierParamName;
			//
			this.userRepository = userRepository;
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
				Redirector.sendRedirect(req, resp, errorPage);
				return;
			}

			// verify token
			Verifier verifier = new Verifier(req.getParameter(verifierParamName));
			Token accessToken = oauthService.getAccessToken(reqToken(req), verifier);

			// fetch user profile
			OAuthRequest oauthRequest = reqBuilder.req(Verb.GET, profileUrl);
			oauthService.signRequest(accessToken, oauthRequest);
			Response oauthResponse = oauthRequest.send();
			RemoteUserProfile profile = Json.GSON.fromJson(oauthResponse.getBody(), profileClass);

			if (profile.valid(userRepository, provider)) {
				String url = Redirector.cleanupRequestedUrl(reqUrl);
				UserSession.setUser(userRepository.findUserByName(provider, profile.username()), req, resp,
						userRepository);
				Redirector.sendRedirect(req, resp, url);
			} else {
				Redirector.sendRedirect(req, resp, errorPage);
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
		boolean valid(UserRepository userRepository, String provider);

		String username();
	}
}
