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

import static java.util.EnumSet.of;
import static org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext;
import io.lavagna.model.Key;
import io.lavagna.service.ConfigurationRepository;
import io.lavagna.service.UserRepository;
import io.lavagna.web.helper.CSRFToken;
import io.lavagna.web.helper.Redirector;
import io.lavagna.web.helper.UserSession;
import io.lavagna.web.security.PathConfiguration.UrlMatcher;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.context.WebApplicationContext;

/**
 * <pre>
 * FIXME: obviously I'm not happy with this one...
 *
 * - I was not able to do a dynamic configuration with spring security.
 * - I needed some additional control
 *
 * If there is some kind of alternatives that give me the same features/functionality as the current version...
 * </pre>
 */
public class SecurityFilter implements Filter {

	private static final Logger LOG = LogManager.getLogger();

	private final PathMatcher pathMatcher = new AntPathMatcher();
	private ConfigurationRepository config;
	private UserRepository userRepository;
	private List<UrlMatcher> configuredAppPathConf;
	private List<UrlMatcher> unconfiguredAppPathConf;

	//

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		WebApplicationContext ctx = getRequiredWebApplicationContext(filterConfig.getServletContext());
		config = ctx.getBean(ConfigurationRepository.class);
		userRepository = ctx.getBean(UserRepository.class);

		configuredAppPathConf = ctx.getBean("configuredAppPathConf", PathConfiguration.class).buildMatcherList();
		unconfiguredAppPathConf = ctx.getBean("unconfiguredAppPathConf", PathConfiguration.class).buildMatcherList();

		if ("true".equals(config.getValueOrNull(Key.USE_HTTPS))) {
			filterConfig.getServletContext().getSessionCookieConfig().setSecure(true);
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
			ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		//

		String reqURI = req.getRequestURI();

		// if it's not in the context path of the application, the security
		// filter will not be triggered
		if (!reqURI.startsWith(req.getServletContext().getContextPath())) {
			chain.doFilter(request, response);
			return;
		}

		Map<Key, String> configuration = config.findConfigurationFor(of(Key.SETUP_COMPLETE, Key.USE_HTTPS,
				Key.BASE_APPLICATION_URL, Key.ENABLE_ANON_USER));

		if (!handleHttps(req, resp, configuration)) {
			return;
		}

		if (!handleCSRF(req, resp)) {
			return;
		}

		addHeaders(req, resp);

		// handle with the correct Url matcher list...
		if ("true".equals(configuration.get(Key.SETUP_COMPLETE))) {
			handleRememberMe(req, resp);

			handleAnonymousUser(configuration, req, resp);

			handleWith(req, resp, chain, configuredAppPathConf);
		} else {
			handleWith(req, resp, chain, unconfiguredAppPathConf);
		}
	}

	private void handleAnonymousUser(Map<Key, String> configuration, HttpServletRequest req, HttpServletResponse resp) {

		final boolean enabled = "true".equals(configuration.get(Key.ENABLE_ANON_USER));

		if (enabled && !UserSession.isUserAuthenticated(req)) {
			UserSession.setUser(userRepository.findUserByName("system", "anonymous"), req, resp, userRepository, false);
		}

		// handle the case when the user is logged as a anonymous user but it's
		// no more enabled
		if (!enabled && UserSession.isUserAuthenticated(req) && UserSession.isUserAnonymous(req)) {
			UserSession.invalidate(req, resp, userRepository);
		}
	}

	private void handleRememberMe(HttpServletRequest req, HttpServletResponse resp) {
		UserSession.authenticateUserIfRemembered(req, resp, userRepository);
	}

	/**
	 * Return true if a redirect has been sent and thus the whole flow must be stopped.
	 *
	 * @param req
	 * @param resp
	 * @param configuration
	 * @return
	 * @throws IOException
	 */
	private boolean handleHttps(HttpServletRequest req, HttpServletResponse resp, Map<Key, String> configuration)
			throws IOException {

		final boolean requestOverHttps = isOverHttps(req);
		final boolean useHttps = "true".equals(configuration.get(Key.USE_HTTPS));

		// warn if the configuration is not aligned with the runtime settings
		boolean hasConfProblem = false;
		if (req.getServletContext().getSessionCookieConfig().isSecure() != useHttps) {
			LOG.warn("SessionCookieConfig is not aligned with settings. The application must be restarted.");
			hasConfProblem = true;
		}
		if (useHttps && !configuration.get(Key.BASE_APPLICATION_URL).startsWith("https://")) {
			LOG.warn(
					"The base application url {} does not begin with https:// . It's a mandatory requirement if you want to enable full https mode.",
					configuration.get(Key.BASE_APPLICATION_URL));
			hasConfProblem = hasConfProblem || true;
		}

		// IF ANY CONF error, will skip this part
		if (hasConfProblem) {
			return true;
		}

		String reqUriWithoutContextPath = reqUriWithoutContextPath(req);

		// TODO: we ignore the websocket because the openshift websocket proxy
		// does not add the X-Forwarded-Proto header. : -> no redirection and
		// STS for the calls under /api/socket/*

		if (useHttps && !requestOverHttps && !reqUriWithoutContextPath.startsWith("/api/socket/")) {
			LOG.debug("use https is true and request is not over https, should redirect request");
			Redirector.sendRedirect(req, resp, reqUriWithoutContextPath);
			return false;
		} else if (useHttps && requestOverHttps) {
			LOG.debug("use https is true and request is over https, adding STS header");
			resp.setHeader("Strict-Transport-Security", "max-age=31536000");
			return true;
		}
		return true;
	}

	@Override
	public void destroy() {
	}

	/**
	 * Return false if there is an error
	 *
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	private boolean handleCSRF(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String token = (String) request.getSession().getAttribute(CSRFToken.CSRF_TOKEN);
		if (token == null) {
			token = UUID.randomUUID().toString();
			request.getSession().setAttribute(CSRFToken.CSRF_TOKEN, token);
		}
		response.setHeader(CSRFToken.CSRF_TOKEN_HEADER, token);
		//
		if (mustCheckCSRF(request)) {
			return checkCSRF(request, response);
		}
		//
		return true;
	}

	private static final Pattern WEBSOCKET_FALLBACK = Pattern.compile("^/api/socket/.*$");

	/**
	 * Return true if the filter must check the
	 *
	 * @param request
	 * @return
	 */
	private boolean mustCheckCSRF(HttpServletRequest request) {

		// ignore the websocket fallback...
		if ("POST".equals(request.getMethod())
				&& WEBSOCKET_FALLBACK.matcher(request.getContextPath() + request.getRequestURI()).matches()) {
			return false;
		}

		return !CSRFToken.CSRF_METHOD_DONT_CHECK.matcher(request.getMethod()).matches();
	}

	private static boolean checkCSRF(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String expectedToken = (String) request.getSession().getAttribute(CSRFToken.CSRF_TOKEN);
		String token = request.getHeader(CSRFToken.CSRF_TOKEN_HEADER);
		if (token == null) {
			token = request.getParameter(CSRFToken.CSRF_FORM_PARAMETER);
		}

		if (token == null) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "missing token in header or parameter");
			return false;
		}
		if (expectedToken == null) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "missing token from session");
			return false;
		}
		if (!CSRFToken.safeArrayEquals(token.getBytes("UTF-8"), expectedToken.getBytes("UTF-8"))) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "token is not equal to expected");
			return false;
		}

		return true;
	}

	private void handleWith(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain,
			List<UrlMatcher> matchers) throws IOException, ServletException {

		String reqUriWithoutContextPath = reqUriWithoutContextPath(request);

		for (UrlMatcher urlMatcher : matchers) {
			if (urlMatcher.match(reqUriWithoutContextPath, pathMatcher)) {
				if (urlMatcher.doAction(request, response)) {
					// the action has been handled by the url matcher, no
					// further processing is required
					return;
				} else {
					break;
				}
			}
		}

		filterChain.doFilter(request, response);
	}

	private static String reqUriWithoutContextPath(HttpServletRequest request) {
		return request.getRequestURI().substring(request.getServletContext().getContextPath().length());
	}

	private static boolean canApplyNoCachingHeaders(HttpServletRequest req) {
		String u = req.getRequestURI();
		return !("/".equals(u) || u.matches(".*\\.(css|gif|js|png|html|eot|svg|ttf|woff)$"));
	}

	/**
	 */
	// TODO check if the no caching directives could be removed/or at least
	// changed... (?)
	private static void addHeaders(HttpServletRequest req, HttpServletResponse res) {
		if (canApplyNoCachingHeaders(req)) {
			res.addHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
			res.addHeader("Expires", "0");
			res.addHeader("Pragma", "no-cache");
		}

		res.addHeader("X-Frame-Options", "SAMEORIGIN");
		res.addHeader("X-XSS-Protection", "1; mode=block");
		res.addHeader("x-content-type-options", "nosniff");
	}

	private static boolean isOverHttps(HttpServletRequest req) {
		return req.isSecure() || req.getRequestURL().toString().startsWith("https://")
				|| StringUtils.equals("https", req.getHeader("X-Forwarded-Proto"));
	}
}
