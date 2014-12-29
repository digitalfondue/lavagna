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

import io.lavagna.model.Key;
import io.lavagna.service.ConfigurationRepository;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public final class Redirector {

	private Redirector() {
	}

	public static String cleanupRequestedUrl(String r) {
		try {
			return (r == null || !r.startsWith("/")) ? "/" : URLDecoder.decode(r, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return r;
		}
	}

	public static String fetchRequestedUrl(HttpServletRequest req) {
		return cleanupRequestedUrl(req.getParameter("reqUrl"));
	}

	public static void sendRedirect(HttpServletRequest req, HttpServletResponse resp, String page) throws IOException {
		sendRedirect(req, resp, page, Collections.<String, List<String>> emptyMap());
	}

	public static void sendRedirect(HttpServletRequest req, HttpServletResponse resp, String page,
			Map<String, List<String>> params) throws IOException {
		WebApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(req.getServletContext());
		String baseApplicationUrl = ctx.getBean(ConfigurationRepository.class).getValue(Key.BASE_APPLICATION_URL);

		UriComponents urlToRedirect = UriComponentsBuilder.fromHttpUrl(baseApplicationUrl).path(page)
				.queryParams(new LinkedMultiValueMap<>(params)).build();

		resp.sendRedirect(urlToRedirect.toUriString());
	}
}
