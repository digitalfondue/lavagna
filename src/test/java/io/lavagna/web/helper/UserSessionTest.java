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

import io.lavagna.common.CookieNames;
import io.lavagna.model.User;
import io.lavagna.service.UserRepository;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserSessionTest {

	private static final String AUTH_USER_ID = UserSession.class.getName() + ".AUTH_USER_ID";

	@Test
	public void isUserAuthenticated() {
		HttpServletRequest req = new MockHttpServletRequest();
		HttpServletResponse resp = new MockHttpServletResponse();
		User user = Mockito.mock(User.class);
		Assert.assertFalse(UserSession.isUserAuthenticated(req));

		UserSession.setUser(user.getId(), user.getAnonymous(), req, resp, Mockito.mock(UserRepository.class));

		Assert.assertTrue(UserSession.isUserAuthenticated(req));

		Assert.assertEquals(user.getId(), UserSession.getUserId(req));
	}

	@Test(expected = NullPointerException.class)
	public void getUserIdFailure() {
		UserSession.getUserId(new MockHttpServletRequest());
	}

	@Test
	public void isUserAnonymous() {
		HttpServletRequest req = new MockHttpServletRequest();
		HttpServletResponse resp = new MockHttpServletResponse();
		UserSession.setUser(1, true, req, resp, Mockito.mock(UserRepository.class));

		Assert.assertTrue(UserSession.isUserAnonymous(req));
		Assert.assertEquals(1, (int) req.getSession().getAttribute(AUTH_USER_ID));
	}

	@Test
	public void testSessionInvalidate() {

		MockHttpServletRequest req = new MockHttpServletRequest();
		HttpServletResponse resp = new MockHttpServletResponse();
		Cookie cookie = new Cookie(CookieNames.getRememberMeCookieName(), "2,056a8421-7448-4753-a932-13dc7e4cd510");
		req.setCookies(cookie);
		UserRepository userRepository = Mockito.mock(UserRepository.class);
		UserSession.setUser(1, true, req, resp, userRepository);
		UserSession.invalidate(req, resp, userRepository);

		Assert.assertNull(req.getSession().getAttribute(AUTH_USER_ID));

	}
}
