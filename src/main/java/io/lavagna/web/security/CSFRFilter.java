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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.tuple.ImmutablePair.of;

public class CSFRFilter extends AbstractBaseFilter {

    private static final String CSRF_TOKEN_HEADER = "X-CSRF-TOKEN";
    private static final String CSRF_FORM_PARAMETER = "_csrf";
    private static final Pattern CSRF_METHOD_DONT_CHECK = Pattern.compile("^GET|HEAD|OPTIONS$");

    private static final Logger LOG = LogManager.getLogger();

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException, ServletException {

        String token = CSRFToken.getToken(req);
        if (token == null) {
            token = UUID.randomUUID().toString();
            CSRFToken.setToken(req, token);
        }
        resp.setHeader(CSRF_TOKEN_HEADER, token);

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
        if ("POST".equals(request.getMethod()) && WEBSOCKET_FALLBACK.matcher(StringUtils.removeStart(request.getRequestURI(), request.getContextPath())).matches()) {
            return false;
        }

        return !CSRF_METHOD_DONT_CHECK.matcher(request.getMethod()).matches();
    }

    private static ImmutablePair<Boolean, ImmutablePair<Integer, String>> checkCSRF(HttpServletRequest request) throws IOException {
        String expectedToken = CSRFToken.getToken(request);
        String token = request.getHeader(CSRF_TOKEN_HEADER);
        if (token == null) {
            token = request.getParameter(CSRF_FORM_PARAMETER);
        }

        if (token == null) {
            return of(false, of(HttpServletResponse.SC_FORBIDDEN, "missing token in header or parameter"));
        }
        if (expectedToken == null) {
            return of(false, of(HttpServletResponse.SC_FORBIDDEN, "missing token from session"));
        }
        if (!safeArrayEquals(token.getBytes("UTF-8"), expectedToken.getBytes("UTF-8"))) {
            return of(false, of(HttpServletResponse.SC_FORBIDDEN, "token is not equal to expected"));
        }

        return of(true, null);
    }


 // ------------------------------------------------------------------------
    // this function has been imported from KeyCzar.

    /*
     * Copyright 2008 Google Inc.
     *
     * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
     * with the License. You may obtain a copy of the License at
     *
     * http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
     * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
     * the specific language governing permissions and limitations under the License.
     */

    /**
     * An array comparison that is safe from timing attacks. If two arrays are of equal length, this code will always
     * check all elements, rather than exiting once it encounters a differing byte.
     *
     * @param a1
     *            An array to compare
     * @param a2
     *            Another array to compare
     * @return True if these arrays are both null or if they have equal length and equal bytes in all elements
     */
    private static boolean safeArrayEquals(byte[] a1, byte[] a2) {
        if (a1 == null || a2 == null) {
            return a1 == a2;
        }
        if (a1.length != a2.length) {
            return false;
        }
        byte result = 0;
        for (int i = 0; i < a1.length; i++) {
            result |= a1[i] ^ a2[i];
        }
        return result == 0;
    }
}
