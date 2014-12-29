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

import io.lavagna.service.Ldap;
import io.lavagna.service.UserRepository;
import io.lavagna.web.helper.Redirector;
import io.lavagna.web.helper.UserSession;
import io.lavagna.web.security.login.LoginHandler.AbstractLoginHandler;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

public class LdapLogin extends AbstractLoginHandler {

	static final String USER_PROVIDER = "ldap";

	private final String errorPage;
	private final Ldap ldap;

	public LdapLogin(UserRepository userRepository, Ldap ldap, String errorPage) {
		super(userRepository);
		this.ldap = ldap;
		this.errorPage = errorPage;
	}

	@Override
	public boolean doAction(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		if (!"POST".equalsIgnoreCase(req.getMethod())) {
			return false;
		}

		String username = req.getParameter("username");
		String password = req.getParameter("password");

		if (authenticate(username, password)) {
			// FIXME refactor out
			String url = Redirector.fetchRequestedUrl(req);
			UserSession.setUser(userRepository.findUserByName(USER_PROVIDER, username), req, resp, userRepository);
			Redirector.sendRedirect(req, resp, url);
		} else {
			Redirector.sendRedirect(req, resp, errorPage);
		}
		return true;

	}

	private boolean authenticate(String username, String password) {
		if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)
				|| !userRepository.userExistsAndEnabled(USER_PROVIDER, username)) {
			return false;
		}

		return ldap.authenticate(username, password);
	}

	@Override
	public Map<String, Object> modelForLoginPage(HttpServletRequest request) {
		Map<String, Object> r = super.modelForLoginPage(request);
		r.put("loginLdap", "block");
		return r;
	}

}
