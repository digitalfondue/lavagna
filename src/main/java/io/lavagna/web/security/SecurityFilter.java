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

import io.lavagna.web.security.SecurityConfiguration.UrlMatcher;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext;

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
public class SecurityFilter extends AbstractBaseFilter {

	private final PathMatcher pathMatcher = new AntPathMatcher();
	private final SortedMap<String, ImmutablePair<SecurityConfiguration, List<UrlMatcher>>> pathsToCheck = new TreeMap<>();

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		WebApplicationContext ctx = getRequiredWebApplicationContext(filterConfig.getServletContext());
		for(Entry<String, SecurityConfiguration> kv : ctx.getBeansOfType(SecurityConfiguration.class).entrySet()) {
		    pathsToCheck.put(kv.getKey(), ImmutablePair.of(kv.getValue(), kv.getValue().buildMatcherList()));
		}
	}


	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException, ServletException {

		addHeaders(req, resp);

		for(ImmutablePair<SecurityConfiguration, List<UrlMatcher>> path : pathsToCheck.values()) {
		    if(path.left.matchRequest(req)) {
		        handleWith(req, resp, chain, path.right);
		        return;
		    }
		}

		chain.doFilter(req, resp);
	}



	private void handleWith(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain, List<UrlMatcher> matchers) throws IOException, ServletException {

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
		return !("/".equals(u) || u.matches(".*\\.(css|gif|js|png|html|eot|svg|ttf|woff|woff2)$"));
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
		res.addHeader("x-body-type-options", "nosniff");
	}

}
