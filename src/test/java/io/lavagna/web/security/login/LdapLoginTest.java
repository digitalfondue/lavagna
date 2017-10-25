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

import io.lavagna.web.security.SecurityConfiguration.SessionHandler;
import io.lavagna.web.security.SecurityConfiguration.User;
import io.lavagna.web.security.SecurityConfiguration.Users;
import io.lavagna.web.security.login.LdapLogin.LdapAuthenticator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LdapLoginTest {

	@Mock
	private Users users;
	@Mock
    private SessionHandler sessionHandler;
	@Mock
	private LdapAuthenticator ldap;

	@Mock
	private HttpServletResponse resp;
	@Mock
	private HttpServletRequest req;
	@Mock
	private ServletContext context;
	@Mock
	private WebApplicationContext webApplicationContext;

	private LdapLogin ldapLogin;

	@Before
	public void prepare() {
		ldapLogin = new LdapLogin(users, sessionHandler, ldap, "errorPage");
	}

	@Test
	public void testNotPost() throws IOException {
		Assert.assertFalse(ldapLogin.doAction(req, resp));
	}

	@Test
	public void testMissingUsernameAndPassword() throws IOException {
		when(req.getMethod()).thenReturn("POST");
		when(req.getContextPath()).thenReturn("");
		Assert.assertTrue(ldapLogin.doAction(req, resp));
		verify(resp).sendRedirect("/errorPage");
	}

	@Test
	public void testMissingPassword() throws IOException {
		when(req.getMethod()).thenReturn("POST");
		when(req.getParameter("username")).thenReturn("user");
		when(req.getContextPath()).thenReturn("");

		Assert.assertTrue(ldapLogin.doAction(req, resp));
		verify(resp).sendRedirect("/errorPage");
	}

	@Test
	public void testUserNotEnabled() throws IOException {
		when(req.getMethod()).thenReturn("POST");
		when(req.getParameter("username")).thenReturn("user");
		when(req.getParameter("password")).thenReturn("password");
		when(req.getContextPath()).thenReturn("");

		Assert.assertTrue(ldapLogin.doAction(req, resp));
		verify(resp).sendRedirect("/errorPage");
	}

	private void prepareForLdapSearch() {
		when(req.getMethod()).thenReturn("POST");
		when(req.getParameter("username")).thenReturn("user");
		when(req.getParameter("password")).thenReturn("password");
		when(req.getContextPath()).thenReturn("");
		when(users.userExistsAndEnabled(LdapLogin.USER_PROVIDER, "user")).thenReturn(true);
	}

	@Test
	public void ldapReturnFalse() throws IOException {
		prepareForLdapSearch();
		when(ldap.authenticate("user", "password")).thenReturn(false);
        when(ldap.checkUserAvailability("user")).thenReturn(true);
		when(req.getContextPath()).thenReturn("");
		Assert.assertTrue(ldapLogin.doAction(req, resp));
		verify(resp).sendRedirect("/errorPage");
	}

	@Test
	public void ldapReturnTrue() throws IOException {
		prepareForLdapSearch();

		when(ldap.authenticate("user", "password")).thenReturn(true);
		when(ldap.checkUserAvailability("user")).thenReturn(true);
		when(users.findUserByName(LdapLogin.USER_PROVIDER, "user")).thenReturn(new User() {

            public boolean isAnonymous() {
                return false;
            }

            public int getId() {
                return 42;
            }
        });

		Assert.assertTrue(ldapLogin.doAction(req, resp));
		verify(resp).sendRedirect("/");
		verify(sessionHandler).setUser(42, false, req, resp);
	}
}
