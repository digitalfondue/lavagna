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

import com.lambdaworks.crypto.SCryptUtil;
import io.lavagna.service.UserRepository;
import io.lavagna.web.security.LoginHandler.AbstractLoginHandler;
import io.lavagna.web.security.Redirector;
import io.lavagna.web.security.SecurityConfiguration;
import io.lavagna.web.security.SecurityConfiguration.SessionHandler;
import io.lavagna.web.security.SecurityConfiguration.Users;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.removeStart;

public class PasswordLogin extends AbstractLoginHandler {

    static final String USER_PROVIDER = "password";
    private final String errorPage;

    private final UserRepository userRepository;

    public PasswordLogin(Users users, SessionHandler sessionHandler, UserRepository userRepository, String errorPage) {
        super(users, sessionHandler);

        this.errorPage = errorPage;
        this.userRepository = userRepository;
    }

    @Override
    public boolean doAction(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        if (!"POST".equalsIgnoreCase(req.getMethod())) {
            return false;
        }

        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (username != null && users.userExistsAndEnabled(USER_PROVIDER, username) && authenticate(username, password)) {
            String url = Redirector.cleanupRequestedUrl(req.getParameter("reqUrl"), req);
            SecurityConfiguration.User user = users.findUserByName(USER_PROVIDER, username);
            sessionHandler.setUser(user.getId(), user.isAnonymous(), req, resp);
            Redirector.sendRedirect(req, resp, url, Collections.<String, List<String>> emptyMap());
        } else {
            Redirector.sendRedirect(req, resp, req.getContextPath() + "/" + removeStart(errorPage, "/"), Collections.<String, List<String>> emptyMap());
        }
        return true;
    }

    private boolean authenticate(final String username, final String password) {
        String hashed = userRepository.getHashedPassword(USER_PROVIDER, username);

        return SCryptUtil.check(password, hashed);
    }

    @Override
    public Map<String, Object> modelForLoginPage(HttpServletRequest request) {
        Map<String, Object> r = super.modelForLoginPage(request);
        r.put("loginPassword", "block");
        return r;
    }

    @Override
    public List<String> getAllHandlerNames() {
        return Collections.singletonList(USER_PROVIDER);
    }

    @Override
    public String getBaseProviderName() {
        return USER_PROVIDER;
    }

}
