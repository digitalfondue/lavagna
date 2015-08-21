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
import io.lavagna.web.helper.Redirector;

import java.io.IOException;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;

public class HSTSFilter extends AbstractBaseFilter {
    
    private static final Logger LOG = LogManager.getLogger();
    private ConfigurationRepository config;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        WebApplicationContext ctx = getRequiredWebApplicationContext(filterConfig.getServletContext());
        config = ctx.getBean(ConfigurationRepository.class);
        
        if ("true".equals(config.getValueOrNull(Key.USE_HTTPS))) {
            filterConfig.getServletContext().getSessionCookieConfig().setSecure(true);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException, ServletException {
        
        
        Map<Key, String> configuration = config.findConfigurationFor(of(Key.USE_HTTPS, Key.BASE_APPLICATION_URL));
        
        final boolean requestOverHttps = isOverHttps(req);
        final boolean useHttps = "true".equals(configuration.get(Key.USE_HTTPS));
        
        boolean hasConfProblem = false;
        if (req.getServletContext().getSessionCookieConfig().isSecure() != useHttps) {
            LOG.warn("SessionCookieConfig is not aligned with settings. The application must be restarted.");
            hasConfProblem = true;
        }
        if (useHttps && !configuration.get(Key.BASE_APPLICATION_URL).startsWith("https://")) {
            LOG.warn("The base application url {} does not begin with https:// . It's a mandatory requirement if you want to enable full https mode.", configuration.get(Key.BASE_APPLICATION_URL));
            hasConfProblem = hasConfProblem || true;
        }
        
        // IF ANY CONF error, will skip the filter
        if (hasConfProblem) {
            chain.doFilter(req, resp);
            return;
        }
        
        String reqUriWithoutContextPath = reqUriWithoutContextPath(req);

        // TODO: we ignore the websocket because the openshift websocket proxy
        // does not add the X-Forwarded-Proto header. : -> no redirection and
        // STS for the calls under /api/socket/*
        if (useHttps && !requestOverHttps && !reqUriWithoutContextPath.startsWith("/api/socket/")) {
            LOG.debug("use https is true and request is not over https, should redirect request");
            Redirector.sendRedirect(req, resp, reqUriWithoutContextPath);
            return;
        } else if (useHttps && requestOverHttps) {
            LOG.debug("use https is true and request is over https, adding STS header");
            resp.setHeader("Strict-Transport-Security", "max-age=31536000");
        }
        
        chain.doFilter(req, resp);
    }
    
    
    private static String reqUriWithoutContextPath(HttpServletRequest request) {
        return request.getRequestURI().substring(request.getServletContext().getContextPath().length());
    }
    
    private static boolean isOverHttps(HttpServletRequest req) {
        return req.isSecure() || req.getRequestURL().toString().startsWith("https://")
                || StringUtils.equals("https", req.getHeader("X-Forwarded-Proto"));
    }

    @Override
    public void destroy() {
    }

}
