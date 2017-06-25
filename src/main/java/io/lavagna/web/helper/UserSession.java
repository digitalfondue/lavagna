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
package io.lavagna.web.helper;

import io.lavagna.common.CookieNames;
import io.lavagna.model.User;
import io.lavagna.model.UserWithPermission;
import io.lavagna.service.UserRepository;
import io.lavagna.service.UserService;
import io.lavagna.web.security.CSRFToken;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Objects;

public final class UserSession {

	private static final Logger LOG = LogManager.getLogger();

	private UserSession() {
	}

	private static final String AUTH_KEY = UserSession.class.getName() + ".AUTH_KEY";
	private static final String AUTH_USER_ID = UserSession.class.getName() + ".AUTH_USER_ID";
	private static final String AUTH_USER_IS_ANONYMOUS = UserSession.class.getName() + ".AUTH_USER_IS_ANONYMOUS";


	public static boolean isUserAuthenticated(HttpServletRequest req) {
		return Boolean.TRUE.equals(req.getSession().getAttribute(AUTH_KEY));
	}

	public static boolean isUserAnonymous(HttpServletRequest req) {
		return Boolean.TRUE.equals(req.getSession().getAttribute(AUTH_USER_IS_ANONYMOUS));
	}

	public static void authenticateUserIfRemembered(HttpServletRequest req, HttpServletResponse resp, UserRepository userRepository) {
		Cookie c;
		if (isUserAuthenticated(req) || (c = getCookie(req, CookieNames.getRememberMeCookieName())) == null) {
			return;
		}

		ImmutablePair<Integer, String> uIdToken = extractUserIdAndToken(c.getValue());

		if (uIdToken != null && userRepository.rememberMeTokenExists(uIdToken.getLeft(), uIdToken.getRight())) {
			userRepository.deleteRememberMeToken(uIdToken.getLeft(), uIdToken.getRight());
			User user = userRepository.findById(uIdToken.getLeft());
			setUser(user.getId(), user.getAnonymous(), req, resp, userRepository, true);
		} else {
			// delete cookie because it's invalid
			c.setMaxAge(0);
			c.setValue(null);
			resp.addCookie(c);
		}
	}

	public static int getUserId(HttpServletRequest req) {
		Object o = req.getSession().getAttribute(AUTH_USER_ID);
		Objects.requireNonNull(o);
		return (int) o;
	}

	public static void invalidate(HttpServletRequest req, HttpServletResponse resp, UserRepository userRepository) {
		req.getSession().invalidate();
		Cookie c = getCookie(req, CookieNames.getRememberMeCookieName());
		if (c != null) {
			deleteTokenIfExist(c.getValue(), userRepository);
			c.setMaxAge(0);
			c.setValue(null);
			resp.addCookie(c);
		}
	}

	private static ImmutablePair<Integer, String> extractUserIdAndToken(String cookieVal) {
        if (cookieVal == null) {
            return null;
        }
		try {
			String[] splitted = cookieVal.split("/");
			if (splitted.length == 2) {
				int userId = Integer.valueOf(splitted[0], 10);
				String token = splitted[1];
				if (token != null) {
					return ImmutablePair.of(userId, token);
				}
			}
		} catch (NullPointerException | NumberFormatException e) {
			LOG.debug("error while extracting userid and token", e);
		}
		return null;
	}

	private static void deleteTokenIfExist(String cookieVal, UserRepository userRepository) {
		ImmutablePair<Integer, String> uIdToken = extractUserIdAndToken(cookieVal);
		if (uIdToken != null) {
			userRepository.deleteRememberMeToken(uIdToken.getLeft(), uIdToken.getRight());
		}
	}

	private static Cookie getCookie(HttpServletRequest request, String name) {
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (cookie.getName().equals(name)) {
					return cookie;
				}
			}
		}
		return null;
	}

	public static void setUser(int userId, boolean isUserAnonymous, HttpServletRequest req, HttpServletResponse resp,
			UserRepository userRepository, boolean addRememberMeCookie) {

	    String currentToken = CSRFToken.getToken(req);
        req.getSession().invalidate();
		if (addRememberMeCookie) {
			addRememberMeCookie(userId, req, resp, userRepository);
		}
		HttpSession session = req.getSession(true);
		session.setAttribute(AUTH_KEY, true);
		session.setAttribute(AUTH_USER_ID, userId);
		session.setAttribute(AUTH_USER_IS_ANONYMOUS, isUserAnonymous);
        CSRFToken.setToken(session, currentToken);
	}

	public static void setUser(int userId, boolean isUserAnonymous, HttpServletRequest req, HttpServletResponse resp, UserRepository userRepository) {
	    boolean rememberMe = "true".equals(req.getParameter("rememberMe")) || "true".equals(req.getAttribute("rememberMe"));
	    setUser(userId, isUserAnonymous, req, resp, userRepository, rememberMe);
	}

	private static void addRememberMeCookie(int userId, HttpServletRequest req, HttpServletResponse resp, UserRepository userRepository) {

		String token = userRepository.createRememberMeToken(userId);
		//
		Cookie c = new Cookie(CookieNames.getRememberMeCookieName(), userId + "/" + token);
		c.setPath(req.getContextPath() + "/");
		c.setHttpOnly(true);
		c.setMaxAge(60 * 60 * 24 * 365); // 1 year
		if (req.getServletContext().getSessionCookieConfig().isSecure()) {
			c.setSecure(true);
		}
		resp.addCookie(c);
	}

	static UserWithPermission fetchFromRequest(HttpServletRequest request, UserService userService) {

		Object userAttr = request.getAttribute(UserWithPermission.class.getName());

		if (userAttr != null) {
			return (UserWithPermission) userAttr;
		} else {
			UserWithPermission res = userService.findUserWithPermission(getUserId(request));
			request.setAttribute(UserWithPermission.class.getName(), res);
			return res;
		}
	}

}
