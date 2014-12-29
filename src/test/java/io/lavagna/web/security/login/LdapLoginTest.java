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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.lavagna.model.Key;
import io.lavagna.model.User;
import io.lavagna.service.ConfigurationRepository;
import io.lavagna.service.Ldap;
import io.lavagna.service.UserRepository;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.web.context.WebApplicationContext;

@RunWith(MockitoJUnitRunner.class)
public class LdapLoginTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private ConfigurationRepository configurationRepository;
	@Mock
	private Ldap ldap;

	@Mock
	private HttpServletResponse resp;
	@Mock
	private HttpServletRequest req;
	@Mock
	private ServletContext context;
	@Mock
	private WebApplicationContext webApplicationContext;

	private final String baseUrl = "http://test.com:8444/";

	private LdapLogin ldapLogin;

	@Before
	public void prepare() {
		ldapLogin = new LdapLogin(userRepository, ldap, "errorPage");

		when(context.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE)).thenReturn(
				webApplicationContext);
		when(webApplicationContext.getBean(ConfigurationRepository.class)).thenReturn(configurationRepository);
		when(configurationRepository.getValue(Key.BASE_APPLICATION_URL)).thenReturn(baseUrl);
	}

	@Test
	public void testNotPost() throws IOException {
		Assert.assertFalse(ldapLogin.doAction(req, resp));
	}

	@Test
	public void testMissingUsernameAndPassword() throws IOException {
		when(req.getMethod()).thenReturn("POST");
		when(req.getServletContext()).thenReturn(context);
		when(context.getContextPath()).thenReturn("");
		Assert.assertTrue(ldapLogin.doAction(req, resp));
		verify(resp).sendRedirect(baseUrl + "errorPage");
	}

	@Test
	public void testMissingPassword() throws IOException {
		when(req.getMethod()).thenReturn("POST");
		when(req.getParameter("username")).thenReturn("user");
		when(req.getServletContext()).thenReturn(context);
		when(context.getContextPath()).thenReturn("");

		Assert.assertTrue(ldapLogin.doAction(req, resp));
		verify(resp).sendRedirect(baseUrl + "errorPage");
	}

	@Test
	public void testUserNotEnabled() throws IOException {
		when(req.getMethod()).thenReturn("POST");
		when(req.getParameter("username")).thenReturn("user");
		when(req.getParameter("password")).thenReturn("password");
		when(req.getServletContext()).thenReturn(context);
		when(context.getContextPath()).thenReturn("");

		Assert.assertTrue(ldapLogin.doAction(req, resp));
		verify(resp).sendRedirect(baseUrl + "errorPage");
	}

	private void prepareForLdapSearch() {
		when(req.getMethod()).thenReturn("POST");
		when(req.getParameter("username")).thenReturn("user");
		when(req.getParameter("password")).thenReturn("password");
		when(req.getServletContext()).thenReturn(context);
		when(context.getContextPath()).thenReturn("");
		when(userRepository.userExistsAndEnabled(LdapLogin.USER_PROVIDER, "user")).thenReturn(true);
	}

	@Test
	public void ldapReturnFalse() throws IOException {
		prepareForLdapSearch();
		when(ldap.authenticate("user", "password")).thenReturn(false);
		Assert.assertTrue(ldapLogin.doAction(req, resp));
		verify(resp).sendRedirect(baseUrl + "errorPage");
	}

	@Test
	public void ldapReturnTrue() throws IOException {
		prepareForLdapSearch();

		when(ldap.authenticate("user", "password")).thenReturn(true);
		when(userRepository.findUserByName(LdapLogin.USER_PROVIDER, "user")).thenReturn(
				new User(42, LdapLogin.USER_PROVIDER, "username", null, null, true, true, new Date()));
		when(req.getSession()).thenReturn(mock(HttpSession.class));
		when(req.getSession(true)).thenReturn(mock(HttpSession.class));

		Assert.assertTrue(ldapLogin.doAction(req, resp));
		verify(resp).sendRedirect(baseUrl);
		verify(req.getSession()).invalidate();
	}
}
