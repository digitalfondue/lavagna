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
package io.lavagna.web.security.login;

import io.lavagna.web.security.Redirector;
import io.lavagna.web.security.SecurityConfiguration.SessionHandler;
import io.lavagna.web.security.SecurityConfiguration.User;
import io.lavagna.web.security.SecurityConfiguration.Users;
import io.lavagna.web.security.login.LoginHandler.AbstractLoginHandler;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Implementation notes:
 * 
 * <pre>
 * - https://developer.mozilla.org/en-US/Persona/Quick_Setup
 * - https://github.com/mozilla/browserid-cookbook/blob/master/java/spring/src/pt/webdetails/browserid/BrowserIdVerifier.java
 * - https://developer.mozilla.org/en-US/Persona/Security_Considerations
 * </pre>
 * 
 * response object:
 * 
 * <pre>
 * {
 *   "status": "okay",
 *   "email": "bob@eyedee.me",
 *   "audience": "https://example.com:443",
 *   "expires": 1308859352261,
 *   "issuer": "eyedee.me"
 * }
 * </pre>
 */
public class PersonaLogin extends AbstractLoginHandler {

	static final String USER_PROVIDER = "persona";

	private final AudienceFetcher audienceFetcher;
	private final RestTemplate restTemplate;
	private final String logoutPage;

	public PersonaLogin(Users users, SessionHandler sessionHandler, AudienceFetcher audienceFetcher, RestTemplate restTemplate, String logoutPage) {

		super(users, sessionHandler);

		this.audienceFetcher = audienceFetcher;
		this.logoutPage = logoutPage;
		this.restTemplate = restTemplate;
	}

	@Override
	public boolean doAction(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		if (!("POST".equalsIgnoreCase(req.getMethod()) && req.getParameterMap().containsKey("assertion"))) {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return true;
		}

		String audience = audienceFetcher.fetch();

		MultiValueMap<String, String> toPost = new LinkedMultiValueMap<>();
		toPost.add("assertion", req.getParameter("assertion"));
		toPost.add("audience", audience);
		VerifierResponse verifier = restTemplate.postForObject("https://verifier.login.persona.org/verify", toPost, VerifierResponse.class);

		if ("okay".equals(verifier.status) && audience.equals(verifier.audience) && users.userExistsAndEnabled(USER_PROVIDER, verifier.email)) {
			String url = Redirector.cleanupRequestedUrl(req.getParameter("reqUrl"), req);
			
			User user = users.findUserByName(USER_PROVIDER, verifier.email);
			sessionHandler.setUser(user.getId(), user.isAnonymous(), req, resp);
			
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentType("application/json");
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("redirectTo", new JsonPrimitive(url));
			resp.getWriter().write(jsonObject.toString());
		} else {
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
		return true;
	}

	static class VerifierResponse {
		private String status;
		private String email;
		private String audience;

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getAudience() {
			return audience;
		}

		public void setAudience(String audience) {
			this.audience = audience;
		}
	}

	@Override
	public boolean handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		if ("POST".equalsIgnoreCase(req.getMethod())) {
		    sessionHandler.invalidate(req, resp);
			resp.setStatus(HttpServletResponse.SC_OK);
			resp.setContentType("application/json");
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("redirectToSelf", new JsonPrimitive(true));
			resp.getWriter().write(jsonObject.toString());
		} else {
			req.getRequestDispatcher(logoutPage).forward(req, resp);
		}
		return true;
	}

	@Override
	public Map<String, Object> modelForLoginPage(HttpServletRequest request) {
		Map<String, Object> r = super.modelForLoginPage(request);
		r.put("loginPersona", "block");
		return r;
	}
	
	public interface AudienceFetcher {
	    String fetch();
	}

}
