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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DemoLoginTest {

	@Mock
	private Users users;
	@Mock
    private SessionHandler sessionHandler;
	@Mock
	private ServletContext context;
	@Mock
	private HttpServletResponse resp;
	@Mock
	private HttpServletRequest req;
	@Mock
	private HttpSession session;
	@Mock
	private WebApplicationContext webApplicationContext;
	@Mock
	private User user;

	private String errorPage = "errorPage";
	private DemoLogin dl;

	@Before
	public void prepare() {
		dl = new DemoLogin(users, sessionHandler, errorPage);
		when(req.getMethod()).thenReturn("POST");
	}

	@Test
	public void testNotPostLogin() throws IOException {
		HttpServletRequest reqNotPost = mock(HttpServletRequest.class);
		Assert.assertFalse(dl.doAction(reqNotPost, mock(HttpServletResponse.class)));
	}

	@Test
	public void testMissingUserNameAndPassword() throws IOException {
	    when(req.getContextPath()).thenReturn("");
	    Assert.assertTrue(dl.doAction(req, resp));

		verify(resp).sendRedirect("/" + errorPage);
	}

	@Test
	public void testUserNotEnabled() throws IOException {
		when(req.getParameter("username")).thenReturn("user");
		when(req.getParameter("password")).thenReturn("user");
		when(users.userExistsAndEnabled(DemoLogin.USER_PROVIDER, "user")).thenReturn(false);
		when(req.getContextPath()).thenReturn("");
		Assert.assertTrue(dl.doAction(req, resp));
		verify(resp).sendRedirect("/" + errorPage);
	}

	@Test
	public void testUserPwdNotEqual() throws IOException {
		// POST, username and password not equal
		when(req.getParameter("username")).thenReturn("user");
		when(req.getParameter("password")).thenReturn("not same as user");
		when(req.getContextPath()).thenReturn("");
		Assert.assertTrue(dl.doAction(req, resp));
		verify(resp).sendRedirect("/" + errorPage);
	}

	@Test
	public void testSuccess() throws IOException {
		when(users.findUserByName(DemoLogin.USER_PROVIDER, "user")).thenReturn(new User() {
		    public int getId() {
		        return 42;
		    }

		    public boolean isAnonymous() {
		        return false;
		    }
		});
		when(req.getParameter("username")).thenReturn("user");
		when(req.getParameter("password")).thenReturn("user");
		when(users.userExistsAndEnabled(DemoLogin.USER_PROVIDER, "user")).thenReturn(true);
		when(req.getContextPath()).thenReturn("/context-path");
		Assert.assertTrue(dl.doAction(req, resp));
		verify(resp).sendRedirect("/context-path/");
		verify(sessionHandler).setUser(42, false, req, resp);
	}

	@Test
	public void testLogoutWithoutSession() throws IOException, ServletException {
		Assert.assertTrue(dl.handleLogout(req, resp));

		verify(resp).setStatus(HttpServletResponse.SC_OK);
		verify(sessionHandler).invalidate(req, resp);
	}

	@Test
	public void testLogoutWithActiveSession() throws IOException, ServletException {
		Assert.assertTrue(dl.handleLogout(req, resp));
		verify(resp).setStatus(HttpServletResponse.SC_OK);
		verify(sessionHandler).invalidate(req, resp);
	}

	@Test
	public void checkModelForLoginPage() {
		when(req.getSession()).thenReturn(session);
		Map<String, Object> modelForLoginPage = dl.modelForLoginPage(req);
		Assert.assertTrue(modelForLoginPage.containsKey("csrfToken"));
	}
}
