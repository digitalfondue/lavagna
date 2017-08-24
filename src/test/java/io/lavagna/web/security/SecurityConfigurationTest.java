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

import io.lavagna.web.security.SecurityConfiguration.BasicUrlMatcher;
import io.lavagna.web.security.SecurityConfiguration.LoginPageGenerator;
import io.lavagna.web.security.SecurityConfiguration.SessionHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

//TODO COMPLETE
@RunWith(MockitoJUnitRunner.class)
public class SecurityConfigurationTest {

    @Mock
    private SessionHandler sessionHandler;

    @Test
    public void testBuildDefault() {
        SecurityConfiguration pc = new SecurityConfiguration()
                .request("/setup/**").denyAll()
                .request("/**").requireAuthenticated()
                .sessionHandler(sessionHandler)
                .login("/login/**", "/login/", new LoginPageGenerator() {
                    @Override
                    public void generate(HttpServletRequest req,
                            HttpServletResponse resp,
                            Map<String, LoginHandler> handlers)
                            throws IOException {
                    }
                }).logout("/logout/**", "/logout");
        pc.buildMatcherList();
    }

    @Test
    public void testBasicUrlMatcher() {
        SecurityConfiguration pc = new SecurityConfiguration();

        BasicUrlMatcher b = new BasicUrlMatcher(pc, "/setup/**");
        b.denyAll();
        b.permitAll();
        b.requireAuthenticated();
    }
}
