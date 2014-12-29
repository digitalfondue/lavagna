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

import static io.lavagna.web.security.login.LoginHandler.AbstractLoginHandler.logout;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.springframework.web.context.support.WebApplicationContextUtils.getWebApplicationContext;
import io.lavagna.common.Json;
import io.lavagna.model.Key;
import io.lavagna.service.ConfigurationRepository;
import io.lavagna.service.UserRepository;
import io.lavagna.web.helper.Redirector;
import io.lavagna.web.helper.UserSession;
import io.lavagna.web.security.login.DemoLogin;
import io.lavagna.web.security.login.LdapLogin;
import io.lavagna.web.security.login.LoginHandler;
import io.lavagna.web.security.login.OAuthLogin;
import io.lavagna.web.security.login.PersonaLogin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.util.PathMatcher;
import org.springframework.web.context.WebApplicationContext;

import com.samskivert.mustache.Mustache;

public class PathConfiguration {

	private final List<UrlMatcher> urlMatchers = new ArrayList<>();
	private boolean loginUrlDisabled;
	private LoginUrlMatcher loginUrlMatcher;
	private LogoutUrlMatcher logoutUrlMatcher;

	List<UrlMatcher> buildMatcherList() {
		List<UrlMatcher> r = new ArrayList<>();

		if (!loginUrlDisabled) {
			Objects.requireNonNull(loginUrlMatcher, "login urls must be configured or disabled");
			Objects.requireNonNull(logoutUrlMatcher, "logout urls must be configured or disabled");

			r.add(loginUrlMatcher);
			r.add(logoutUrlMatcher);
		}

		r.addAll(urlMatchers);
		return r;
	}

	public PathConfiguration disableLogin() {
		loginUrlDisabled = true;
		return this;
	}

	//
	public LogoutConfigurer login(String loginUrlMatcher, String loginPageUrl, String loginPage) {
		Validate.isTrue(!loginUrlDisabled, "login has been disabled");
		this.loginUrlMatcher = new LoginUrlMatcher(loginUrlMatcher, loginPageUrl, loginPage);
		return new LogoutConfigurer(this);
	}

	public BasicUrlMatcher request(String url) {
		BasicUrlMatcher urlMatcher = new BasicUrlMatcher(this, url);
		urlMatchers.add(urlMatcher);
		return urlMatcher;
	}

	public static class LogoutConfigurer {

		private final PathConfiguration conf;

		private LogoutConfigurer(PathConfiguration conf) {
			this.conf = conf;
		}

		public PathConfiguration logout(String logoutUrlMatcher, String logoutBaseUrl) {
			conf.logoutUrlMatcher = new LogoutUrlMatcher(logoutUrlMatcher, logoutBaseUrl);
			return conf;
		}
	}

	public interface UrlMatcher {
		boolean match(String url, PathMatcher pathMatcher);

		boolean doAction(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException;
	}

	public static class BasicUrlMatcher implements UrlMatcher {
		private final String urlMatcher;
		private final PathConfiguration conf;
		private boolean redirect;
		private Mode mode;

		BasicUrlMatcher(PathConfiguration conf, String urlMatcher) {
			this.conf = conf;
			this.urlMatcher = urlMatcher;
		}

		public PathConfiguration denyAll() {
			mode = Mode.DENY_ALL;
			return conf;
		}

		public PathConfiguration requireAuthenticated() {
			return requireAuthenticated(true);
		}

		public PathConfiguration requireAuthenticated(boolean redirect) {
			mode = Mode.REQUIRE_AUTHENTICATED;
			this.redirect = redirect;
			return conf;
		}

		public PathConfiguration permitAll() {
			mode = Mode.PERMIT_ALL;
			return conf;
		}

		@Override
		public boolean match(String url, PathMatcher pathMatcher) {
			return pathMatcher.match(urlMatcher, url);
		}

		@Override
		public boolean doAction(HttpServletRequest req, HttpServletResponse resp) throws IOException {
			if (mode == Mode.REQUIRE_AUTHENTICATED && !UserSession.isUserAuthenticated(req)) {
				if (redirect) {
					String requestedUrl = extractRequestedUrl(req);
					Redirector.sendRedirect(req, resp, conf.loginUrlMatcher.loginPageUrl,
							singletonMap("reqUrl", singletonList(URLEncoder.encode(requestedUrl, "UTF-8"))));
				} else {
					resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				}
				return true;
			} else if (mode == Mode.DENY_ALL) {
				resp.sendError(HttpServletResponse.SC_FORBIDDEN);
				return true;
			} else {
				return false;
			}
		}
	}

	private static String extractRequestedUrl(HttpServletRequest req) {
		String queryString = req.getQueryString();
		return removeStart(req.getRequestURI(), req.getContextPath())
				+ (queryString != null ? ("?" + queryString) : "");
	}

	public static class LogoutUrlMatcher implements UrlMatcher {
		private final String logoutUrlMatcher;
		private final String logoutBaseUrl;

		LogoutUrlMatcher(String logoutUrlMatcher, String logoutBaseUrl) {
			this.logoutBaseUrl = logoutBaseUrl;
			this.logoutUrlMatcher = logoutUrlMatcher;
		}

		@Override
		public boolean match(String url, PathMatcher pathMatcher) {
			return pathMatcher.match(logoutUrlMatcher, url);
		}

		@Override
		public boolean doAction(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
			Map<String, LoginHandler> handlers = findActiveLoginHandlers(req);
			String subPath = findSubpath(req, logoutBaseUrl);
			if (handlers.containsKey(subPath)) {
				return handlers.get(subPath).handleLogout(req, resp);
			} else {
				// fallback to default logout handler
				return logout(req, resp, getWebApplicationContext(req.getServletContext())
						.getBean(UserRepository.class));
			}
		}
	}

	public static class LoginUrlMatcher implements UrlMatcher {
		private final String urlMatcher;
		private final String loginPageUrl;
		private final String loginPage;

		LoginUrlMatcher(String urlMatcher, String loginPageUrl, String loginPage) {
			this.urlMatcher = urlMatcher;
			this.loginPageUrl = loginPageUrl;
			this.loginPage = loginPage;
		}

		@Override
		public boolean match(String url, PathMatcher pathMatcher) {
			return pathMatcher.match(urlMatcher, url);
		}

		@Override
		public boolean doAction(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
			Map<String, LoginHandler> handlers = findActiveLoginHandlers(req);

			// handle the static page for the login, we expect that it's a GET
			// request _and_ it match the configured path (/login)
			if ("GET".equalsIgnoreCase(req.getMethod()) && loginPageUrl.equals(req.getServletPath())) {
				InputStream is = req.getServletContext().getResourceAsStream(loginPage);
				Map<String, Object> model = new HashMap<>();
				for (LoginHandler lh : handlers.values()) {
					model.putAll(lh.modelForLoginPage(req));
				}
				resp.setStatus(HttpServletResponse.SC_OK);
				resp.setContentType("text/html");
				Mustache.compiler().defaultValue("").compile(new InputStreamReader(is, StandardCharsets.UTF_8))
						.execute(model, resp.getWriter());
				return true;
			}
			// -------------------------------
			// given /login/demo/ -> return demo
			// subPath will be demo/ldap/oauth/persona
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

	private static Map<String, LoginHandler> findActiveLoginHandlers(HttpServletRequest req) {
		Map<String, LoginHandler> res = new HashMap<String, LoginHandler>();
		WebApplicationContext ctx = getWebApplicationContext(req.getServletContext());
		LoginHandlerType[] authMethods = Json.GSON.fromJson(
				ctx.getBean(ConfigurationRepository.class).getValue(Key.AUTHENTICATION_METHOD),
				LoginHandlerType[].class);
		for (LoginHandlerType m : authMethods) {
			res.put(m.pathAfterLogin, ctx.getBean(m.classHandler));
		}
		return res;
	}

	public enum LoginHandlerType {

		DEMO(DemoLogin.class, "demo"), LDAP(LdapLogin.class, "ldap"), OAUTH(OAuthLogin.class, "oauth"), PERSONA(
				PersonaLogin.class, "persona");

		LoginHandlerType(Class<? extends LoginHandler> classHandler, String pathAfterLogin) {
			this.classHandler = classHandler;
			this.pathAfterLogin = pathAfterLogin;
		}

		private final Class<? extends LoginHandler> classHandler;
		private final String pathAfterLogin;

	}

	private enum Mode {
		DENY_ALL, UNAUTHENTICATED, REQUIRE_AUTHENTICATED, PERMIT_ALL, LOGIN, LOGOUT
	}
}
