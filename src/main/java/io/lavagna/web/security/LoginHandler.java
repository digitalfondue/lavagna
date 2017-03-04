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
package io.lavagna.web.security;

import io.lavagna.web.security.SecurityConfiguration.SessionHandler;
import io.lavagna.web.security.SecurityConfiguration.Users;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface LoginHandler {

	boolean doAction(HttpServletRequest req, HttpServletResponse resp) throws IOException;

	boolean handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException;

	List<String> getAllHandlerNames();
	String getBaseProviderName();

	Map<String, Object> modelForLoginPage(HttpServletRequest request);

	abstract class AbstractLoginHandler implements LoginHandler {

		protected final Users users;
		protected final SessionHandler sessionHandler;

		public AbstractLoginHandler(Users users, SessionHandler sessionHandler) {
			this.users = users;
			this.sessionHandler = sessionHandler;
		}

		@Override
		public boolean handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		    sessionHandler.invalidate(req, resp);
		    resp.setStatus(HttpServletResponse.SC_OK);
			return true;
		}

		public Map<String, Object> modelForLoginPage(HttpServletRequest request) {
			String tokenValue =  CSRFToken.getToken(request);
			Map<String, Object> r = new HashMap<>();
			r.put("csrfToken", tokenValue);
			r.put("reqUrl", UriComponentsBuilder.fromPath(request.getParameter("reqUrl")).build().encode().toUriString());
			return r;
		}
	}
}
