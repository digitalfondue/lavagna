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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.lavagna.model.Key;
import io.lavagna.model.User;
import io.lavagna.service.ConfigurationRepository;
import io.lavagna.service.UserRepository;
import io.lavagna.web.security.SecurityConfiguration.SessionHandler;
import io.lavagna.web.security.login.PersonaLogin.VerifierResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.client.RestTemplate;

@RunWith(MockitoJUnitRunner.class)
public class PersonaLoginTest {

	@Mock
	private UserRepository userRepository;
	
	@Mock
	private SessionHandler sessionHandler;
	
	@Mock
	private ConfigurationRepository configurationRepository;
	@Mock
	private RestTemplate restTemplate;
	@Mock
	private ServletContext context;
	@Mock
	private HttpServletResponse resp;
	@Mock
	private HttpServletRequest req;
	@Mock
	private HttpSession session;
	@Mock
	private Map<String, String[]> parameterMap;
	@Mock
	private RequestDispatcher requestDispatcher;

	@Mock
	private User user;

	private String logoutPage = "logoutPage";
	private PersonaLogin personaLogin;

	@Before
	public void prepare() {
		personaLogin = new PersonaLogin(userRepository, sessionHandler, configurationRepository, restTemplate, logoutPage);
	}

	public void prepareSuccessfulPreconditions() {
		when(req.getMethod()).thenReturn("POST");
		when(req.getParameterMap()).thenReturn(parameterMap);
		when(parameterMap.containsKey("assertion")).thenReturn(true);
		when(configurationRepository.getValue(Key.PERSONA_AUDIENCE)).thenReturn("audience");
	}

	@Test
	public void notPostRequest() throws IOException {
		Assert.assertTrue(personaLogin.doAction(req, resp));
		verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

	@Test
	public void missingAssertionElement() throws IOException {
		when(req.getMethod()).thenReturn("POST");
		when(req.getParameterMap()).thenReturn(parameterMap);
		Assert.assertTrue(personaLogin.doAction(req, resp));
		verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

	@Test
	public void verifierStatusNotOkay() throws IOException {
		prepareSuccessfulPreconditions();

		VerifierResponse verifier = new VerifierResponse();
		when(restTemplate.postForObject(any(String.class), any(), eq(VerifierResponse.class))).thenReturn(verifier);

		Assert.assertTrue(personaLogin.doAction(req, resp));
		verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

	@Test
	public void verifierWrongAudience() throws IOException {
		prepareSuccessfulPreconditions();

		VerifierResponse verifier = new VerifierResponse();
		verifier.setStatus("okay");
		verifier.setAudience("wrongOne");
		when(restTemplate.postForObject(any(String.class), any(), eq(VerifierResponse.class))).thenReturn(verifier);

		Assert.assertTrue(personaLogin.doAction(req, resp));
		verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

	@Test
	public void verifierUserNotEnabled() throws IOException {
		prepareSuccessfulPreconditions();

		VerifierResponse verifier = new VerifierResponse();
		verifier.setStatus("okay");
		verifier.setAudience("audience");
		verifier.setEmail("email");
		when(restTemplate.postForObject(any(String.class), any(), eq(VerifierResponse.class))).thenReturn(verifier);
		when(userRepository.userExistsAndEnabled(PersonaLogin.USER_PROVIDER, "email")).thenReturn(false);

		Assert.assertTrue(personaLogin.doAction(req, resp));
		verify(resp).setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

	@Test
	public void verifierSuccess() throws IOException {
		prepareSuccessfulPreconditions();

		VerifierResponse verifier = new VerifierResponse();
		verifier.setStatus("okay");
		verifier.setAudience("audience");
		verifier.setEmail("email");
		when(restTemplate.postForObject(any(String.class), any(), eq(VerifierResponse.class))).thenReturn(verifier);
		when(userRepository.userExistsAndEnabled(PersonaLogin.USER_PROVIDER, "email")).thenReturn(true);
		when(userRepository.findUserByName(PersonaLogin.USER_PROVIDER, "email")).thenReturn(
				new User(42, PersonaLogin.USER_PROVIDER, "username", null, null, true, true, new Date()));
		when(req.getSession()).thenReturn(session);
		when(req.getSession(true)).thenReturn(session);
		when(resp.getWriter()).thenReturn(mock(PrintWriter.class));

		Assert.assertTrue(personaLogin.doAction(req, resp));
		verify(resp).setStatus(HttpServletResponse.SC_OK);
		verify(sessionHandler).setUser(42, false, req, resp);
	}

	@Test
	public void logoutNotPost() throws IOException, ServletException {
		when(req.getRequestDispatcher(logoutPage)).thenReturn(requestDispatcher);
		personaLogin.handleLogout(req, resp);
		verify(req).getRequestDispatcher(logoutPage);
		verify(requestDispatcher).forward(req, resp);
	}

	@Test
	public void logoutPostUserNotAuth() throws IOException, ServletException {
		when(req.getMethod()).thenReturn("POST");
		when(req.getSession()).thenReturn(session);
		PrintWriter pw = mock(PrintWriter.class);
		when(resp.getWriter()).thenReturn(pw);
		personaLogin.handleLogout(req, resp);
		verify(resp).setStatus(HttpServletResponse.SC_OK);
		verify(resp).setContentType("application/json");
	}

	@Test
	public void logoutPostUserAuth() throws IOException, ServletException {

		MockHttpSession unauthMockSession = new MockHttpSession();
		MockHttpSession mockSession = new MockHttpSession();
		when(req.getSession()).thenReturn(unauthMockSession, mockSession);
		when(req.getSession(true)).thenReturn(mockSession);

		when(req.getMethod()).thenReturn("POST");
		PrintWriter pw = mock(PrintWriter.class);
		when(resp.getWriter()).thenReturn(pw);
		Assert.assertFalse(mockSession.isInvalid());

		personaLogin.handleLogout(req, resp);

		verify(resp).setStatus(HttpServletResponse.SC_OK);
		verify(resp).setContentType("application/json");
		verify(sessionHandler).invalidate(req, resp);
	}

}
