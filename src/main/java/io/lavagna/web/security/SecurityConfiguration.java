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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.util.PathMatcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.apache.commons.lang3.StringUtils.removeStart;

public class SecurityConfiguration {

	private final List<UrlMatcher> urlMatchers = new ArrayList<>();
	private boolean loginUrlDisabled;
	private LoginUrlMatcher loginUrlMatcher;
	private LogoutUrlMatcher logoutUrlMatcher;
	private RequestMatcher requestMatcher = new AlwaysTrueRequestMatcher();

	private LoginHandlerFinder loginHandlerFinder = new LoginHandlerFinder() {
        @Override
        public Map<String, LoginHandler> find() {
            return Collections.emptyMap();
        }
	};

	private SessionHandler sessionHandler;

	List<UrlMatcher> buildMatcherList() {
		List<UrlMatcher> r = new ArrayList<>();

		if (!loginUrlDisabled) {
			Objects.requireNonNull(loginUrlMatcher, "login urls must be configured or disabled");
			Objects.requireNonNull(logoutUrlMatcher, "logout urls must be configured or disabled");
			Objects.requireNonNull(sessionHandler, "sessionHandler must be defined or login url must be disabled");
			r.add(loginUrlMatcher);
			r.add(logoutUrlMatcher);
		}
		r.addAll(urlMatchers);
		return r;
	}

	public SecurityConfiguration requestMatcher(RequestMatcher requestMatcher) {
	    this.requestMatcher = requestMatcher;
	    return this;
	}

	public SecurityConfiguration loginHandlerFinder(LoginHandlerFinder loginHandlerFinder) {
	    this.loginHandlerFinder = loginHandlerFinder;
	    return this;
	}

	public SecurityConfiguration sessionHandler(SessionHandler sessionHandler) {
	    this.sessionHandler = sessionHandler;
	    return this;
	}

	public boolean matchRequest(HttpServletRequest request) {
	    return requestMatcher.match(request);
	}

	public SecurityConfiguration disableLogin() {
		loginUrlDisabled = true;
		return this;
	}

	//
	public LogoutConfigurer login(String loginUrlMatcher, String loginPageUrl, LoginPageGenerator loginPageGenerator) {
		Validate.isTrue(!loginUrlDisabled, "login has been disabled");
		this.loginUrlMatcher = new LoginUrlMatcher(loginUrlMatcher, loginPageUrl, loginPageGenerator, this);
		return new LogoutConfigurer(this);
	}

	public BasicUrlMatcher request(String url) {
		BasicUrlMatcher urlMatcher = new BasicUrlMatcher(this, url);
		urlMatchers.add(urlMatcher);
		return urlMatcher;
	}

	public static class LogoutConfigurer {

		private final SecurityConfiguration conf;

		private LogoutConfigurer(SecurityConfiguration conf) {
			this.conf = conf;
		}

		public SecurityConfiguration logout(String logoutUrlMatcher, String logoutBaseUrl) {
			conf.logoutUrlMatcher = new LogoutUrlMatcher(logoutUrlMatcher, logoutBaseUrl, conf);
			return conf;
		}
	}

	public interface UrlMatcher {
		boolean match(String url, PathMatcher pathMatcher);

		boolean doAction(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException;
	}

	public static class BasicUrlMatcher implements UrlMatcher {
		private final String urlMatcher;
		private final SecurityConfiguration conf;
		private boolean redirect;
		private String redirectTo;
		private Mode mode;

		BasicUrlMatcher(SecurityConfiguration conf, String urlMatcher) {
			this.conf = conf;
			this.urlMatcher = urlMatcher;
		}

		public SecurityConfiguration denyAll() {
			mode = Mode.DENY_ALL;
			return conf;
		}

		public SecurityConfiguration requireAuthenticated() {
			return requireAuthenticated(true);
		}

		public SecurityConfiguration requireAuthenticated(boolean redirect) {
			mode = Mode.REQUIRE_AUTHENTICATED;
			this.redirect = redirect;
			return conf;
		}

		public SecurityConfiguration redirectTo(String redirectTo) {
		    mode = Mode.REDIRECT;
		    this.redirectTo = redirectTo;
		    return conf;
		}

		public SecurityConfiguration permitAll() {
			mode = Mode.PERMIT_ALL;
			return conf;
		}

		@Override
		public boolean match(String url, PathMatcher pathMatcher) {
			return pathMatcher.match(urlMatcher, url);
		}

		@Override
		public boolean doAction(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			if (mode == Mode.REQUIRE_AUTHENTICATED && !conf.sessionHandler.isUserAuthenticated(req)) {
				if (redirect) {
					String requestedUrl = extractRequestedUrl(req);
					Redirector.sendRedirect(req, resp, req.getContextPath() + "/" + removeStart(conf.loginUrlMatcher.loginPageUrl, "/"), singletonMap("reqUrl", singletonList(URLEncoder.encode(requestedUrl, "UTF-8"))));
				} else {
					resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				}
				return true;
			} else if (mode == Mode.DENY_ALL) {
				resp.sendError(HttpServletResponse.SC_FORBIDDEN);
				return true;
			} else if(mode == Mode.REDIRECT) {
			    Redirector.sendRedirect(req, resp, req.getContextPath() + "/" + removeStart(redirectTo, "/"), Collections.<String, List<String>>emptyMap());
			    return true;
			} else {
				return false;
			}
		}
	}

	private static String extractRequestedUrl(HttpServletRequest req) {
		String queryString = req.getQueryString();
		return req.getRequestURI() + (queryString != null ? ("?" + queryString) : "");
	}

	public static class LogoutUrlMatcher implements UrlMatcher {
		private final String logoutUrlMatcher;
		private final String logoutBaseUrl;
		private final SecurityConfiguration conf;

		LogoutUrlMatcher(String logoutUrlMatcher, String logoutBaseUrl, SecurityConfiguration conf) {
			this.logoutBaseUrl = logoutBaseUrl;
			this.logoutUrlMatcher = logoutUrlMatcher;
			this.conf = conf;
		}

		@Override
		public boolean match(String url, PathMatcher pathMatcher) {
			return pathMatcher.match(logoutUrlMatcher, url);
		}

		@Override
		public boolean doAction(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
			Map<String, LoginHandler> handlers = conf.loginHandlerFinder.find();
			String subPath = findSubpath(req, logoutBaseUrl);
			if (handlers.containsKey(subPath)) {
				return handlers.get(subPath).handleLogout(req, resp);
			} else {
	             // fallback to default logout handler
			    conf.sessionHandler.invalidate(req, resp);
			    return true;
			}
		}
	}

	public static class LoginUrlMatcher implements UrlMatcher {
		private final String urlMatcher;
		private final String loginPageUrl;
		private final LoginPageGenerator loginPageGenerator;
		private final SecurityConfiguration conf;

		LoginUrlMatcher(String urlMatcher, String loginPageUrl, LoginPageGenerator loginPageGenerator, SecurityConfiguration conf) {
			this.urlMatcher = urlMatcher;
			this.loginPageUrl = loginPageUrl;
			this.loginPageGenerator = loginPageGenerator;
			this.conf = conf;
		}

		@Override
		public boolean match(String url, PathMatcher pathMatcher) {
			return pathMatcher.match(urlMatcher, url);
		}

		@Override
		public boolean doAction(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
			Map<String, LoginHandler> handlers = conf.loginHandlerFinder.find();

			// handle the static page for the login, we expect that it's a GET
			// request _and_ it match the configured path (/login)
			if ("GET".equalsIgnoreCase(req.getMethod()) && loginPageUrl.equals(req.getServletPath())) {
			    loginPageGenerator.generate(req, resp, handlers);
				return true;
			}
			// -------------------------------
			// given /login/demo/ -> return demo
			// subPath will be demo/ldap/oauth
			String subPath = findSubpath(req, loginPageUrl);

			if (handlers.containsKey(subPath)) {
				return handlers.get(subPath).doAction(req, resp);
			} else {
				return false;
			}
		}

	}

	// given /login/demo/ -> return demo
	private static String findSubpath(HttpServletRequest req, String firstPath) {
		return StringUtils.substringBefore(StringUtils.substring(req.getServletPath(), firstPath.length() + 1), "/");
	}

	private enum Mode {
		DENY_ALL, UNAUTHENTICATED, REQUIRE_AUTHENTICATED, PERMIT_ALL, LOGIN, LOGOUT, REDIRECT
	}

	public interface RequestMatcher {
	    boolean match(HttpServletRequest request);
	}

	public interface LoginHandlerFinder {
	    Map<String, LoginHandler> find();
	}

	public interface LoginPageGenerator {
	    void generate(HttpServletRequest req, HttpServletResponse resp, Map<String, LoginHandler> handlers) throws IOException;
	}

	public interface SessionHandler {
	    void invalidate(HttpServletRequest req, HttpServletResponse resp);
	    boolean isUserAuthenticated(HttpServletRequest req);
        boolean isUserAnonymous(HttpServletRequest req);

        void setUser(int userId, boolean isUserAnonymous, HttpServletRequest req, HttpServletResponse resp);
        void setUser(int userId, boolean isUserAnonymous, HttpServletRequest req, HttpServletResponse resp, boolean addRememberMeCookie);
	}

	public interface Users {
	    boolean userExistsAndEnabled(String provider, String name);
	    User findUserByName(String provider, String name);
	}

	public interface User {
	    int getId();
	    boolean isAnonymous();
	}

	public static class AlwaysTrueRequestMatcher implements RequestMatcher {
        @Override
        public boolean match(HttpServletRequest request) {
            return true;
        }
	}
}
