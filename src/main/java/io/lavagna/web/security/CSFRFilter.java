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

import static org.apache.commons.lang3.tuple.ImmutablePair.of;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CSFRFilter extends AbstractBaseFilter {
    
    private static final Logger LOG = LogManager.getLogger();

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException, ServletException {
        
        String token = (String) req.getSession().getAttribute(CSRFToken.CSRF_TOKEN);
        if (token == null) {
            token = UUID.randomUUID().toString();
            req.getSession().setAttribute(CSRFToken.CSRF_TOKEN, token);
        }
        resp.setHeader(CSRFToken.CSRF_TOKEN_HEADER, token);
        
        if (mustCheckCSRF(req)) {
            ImmutablePair<Boolean, ImmutablePair<Integer, String>> res = checkCSRF(req);
            if (!res.left) {
                LOG.info("wrong csrf");
                resp.sendError(res.right.left, res.right.right);
                return;
            }
        }
        
        //continue...
        chain.doFilter(req, resp);
    }


    private static final Pattern WEBSOCKET_FALLBACK = Pattern.compile("^/api/socket/.*$");
    
    /**
     * Return true if the filter must check the request
     *
     * @param request
     * @return
     */
    private boolean mustCheckCSRF(HttpServletRequest request) {

        // ignore the websocket fallback...
        if ("POST".equals(request.getMethod()) && WEBSOCKET_FALLBACK.matcher(request.getContextPath() + request.getRequestURI()).matches()) {
            return false;
        }

        return !CSRFToken.CSRF_METHOD_DONT_CHECK.matcher(request.getMethod()).matches();
    }

    private static ImmutablePair<Boolean, ImmutablePair<Integer, String>> checkCSRF(HttpServletRequest request) throws IOException {
        String expectedToken = (String) request.getSession().getAttribute(CSRFToken.CSRF_TOKEN);
        String token = request.getHeader(CSRFToken.CSRF_TOKEN_HEADER);
        if (token == null) {
            token = request.getParameter(CSRFToken.CSRF_FORM_PARAMETER);
        }

        if (token == null) {
            return of(false, of(HttpServletResponse.SC_FORBIDDEN, "missing token in header or parameter"));
        }
        if (expectedToken == null) {
            return of(false, of(HttpServletResponse.SC_FORBIDDEN, "missing token from session"));
        }
        if (!CSRFToken.safeArrayEquals(token.getBytes("UTF-8"), expectedToken.getBytes("UTF-8"))) {
            return of(false, of(HttpServletResponse.SC_FORBIDDEN, "token is not equal to expected"));
        }

        return of(true, null);
    }
    
}
