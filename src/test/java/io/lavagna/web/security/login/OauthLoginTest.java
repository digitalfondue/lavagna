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
import io.lavagna.service.ConfigurationRepository;
import io.lavagna.web.security.SecurityConfiguration.SessionHandler;
import io.lavagna.web.security.SecurityConfiguration.Users;
import io.lavagna.web.security.login.OAuthLogin.Handler;
import io.lavagna.web.security.login.OAuthLogin.OAuthProvider;
import io.lavagna.web.security.login.oauth.OAuthResultHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Api;
import org.scribe.oauth.OAuthService;

@RunWith(MockitoJUnitRunner.class)
public class OauthLoginTest {

	@Mock
	private Users users;
	@Mock
    private SessionHandler sessionHandler;
	@Mock
	private ConfigurationRepository configurationRepository;
	@Mock
	private Handler handler;
	@Mock
	private OAuthResultHandler authResultHandler;
	@Mock
	private ServiceBuilder serviceBuilder;
	@Mock
	private HttpServletResponse resp;
	@Mock
	private HttpServletRequest req;

	private String errorPage = "errorPage";

	private static final String oauthJsonConf = "{ \"baseUrl\" : \"http://localhost:8080/\", \"providers\" : ["
			+ "{\"provider\" : \"bitbucket\", \"apiKey\" : \"apiKey\", \"apiSecret\" : \"secret\"},"
			+ "{\"provider\" : \"google\", \"apiKey\": \"apiKey\", \"apiSecret\" : \"secret\"}]}";

	private OAuthLogin oAuthLogin;

	@SuppressWarnings("unchecked")
	@Before
	public void prepare() {
		oAuthLogin = new OAuthLogin(users, sessionHandler, configurationRepository, handler, errorPage);
		when(configurationRepository.getValue(Key.OAUTH_CONFIGURATION)).thenReturn(oauthJsonConf);
		when(serviceBuilder.provider(any(Class.class))).thenReturn(serviceBuilder);
		when(serviceBuilder.provider(any(Api.class))).thenReturn(serviceBuilder);
		when(serviceBuilder.apiKey(any(String.class))).thenReturn(serviceBuilder);
		when(serviceBuilder.apiSecret(any(String.class))).thenReturn(serviceBuilder);
		when(serviceBuilder.callback(any(String.class))).thenReturn(serviceBuilder);
		when(serviceBuilder.scope(any(String.class))).thenReturn(serviceBuilder);
		when(serviceBuilder.build()).thenReturn(mock(OAuthService.class));
	}

	@Test
	public void initiateWithoutPost() throws IOException {
		when(req.getRequestURI()).thenReturn("/login/oauth/google");
		Assert.assertFalse(oAuthLogin.doAction(req, resp));
	}

	@Test
	public void initiateWithPostWrongUrl() throws IOException {
		when(req.getRequestURI()).thenReturn("/login/oauth/derp");
		when(req.getMethod()).thenReturn("POST");
		when(handler.from(any(OAuthProvider.class), any(String.class), eq(users),eq(sessionHandler),  eq(errorPage))).thenReturn(
				authResultHandler);
		Assert.assertFalse(oAuthLogin.doAction(req, resp));
	}

	@Test
	public void initiateWithPost() throws IOException {
		when(req.getRequestURI()).thenReturn("/login/oauth/google");
		when(req.getMethod()).thenReturn("POST");
		when(handler.from(any(OAuthProvider.class), any(String.class), eq(users),eq(sessionHandler),  eq(errorPage))).thenReturn(
				authResultHandler);
		Assert.assertTrue(oAuthLogin.doAction(req, resp));
		verify(authResultHandler).handleAuthorizationUrl(req, resp);
	}

	@Test
	public void callbackHandle() throws IOException {
		when(req.getRequestURI()).thenReturn("/login/oauth/google/callback");
		when(handler.from(any(OAuthProvider.class), any(String.class), eq(users),eq(sessionHandler),  eq(errorPage))).thenReturn(
				authResultHandler);
		Assert.assertTrue(oAuthLogin.doAction(req, resp));
		verify(authResultHandler).handleCallback(req, resp);
	}

	@Test
	public void callbackHandleForWrongProvider() throws IOException {
		when(req.getRequestURI()).thenReturn("/login/oauth/derp/callback");
		when(handler.from(any(OAuthProvider.class), any(String.class), eq(users), eq(sessionHandler), eq(errorPage))).thenReturn(
				authResultHandler);
		Assert.assertFalse(oAuthLogin.doAction(req, resp));
	}

	@Test
	public void checkModelForLoginPage() {
		when(req.getSession()).thenReturn(mock(HttpSession.class));
		Map<String, Object> r = oAuthLogin.modelForLoginPage(req);
		@SuppressWarnings("unchecked")
		List<String> providers = (List<String>) r.get("loginOauthProviders");
		Assert.assertTrue(providers.contains("google"));
		Assert.assertTrue(providers.contains("bitbucket"));
		Assert.assertFalse(providers.contains("github"));
		Assert.assertFalse(providers.contains("twitter"));
		Assert.assertTrue(r.containsKey("csrfToken"));
	}

	@Test
	public void testHandler() {
		Handler h = new Handler(serviceBuilder);
		OAuthProvider oAuthProvider = new OAuthProvider();

		oAuthProvider.apiKey = "key";
		oAuthProvider.apiSecret = "secret";

		for (Entry<String, Class<? extends OAuthResultHandler>> kv : OAuthLogin.SUPPORTED_OAUTH_HANDLER.entrySet()) {
			oAuthProvider.provider = kv.getKey();
			OAuthResultHandler handler = h.from(oAuthProvider, "http://localhost:8080/", users, sessionHandler, errorPage);
			Assert.assertTrue(handler.getClass().equals(kv.getValue()));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testHandlerFailure() {
		Handler h = new Handler(serviceBuilder);
		OAuthProvider oAuthProvider = new OAuthProvider();

		oAuthProvider.apiKey = "key";
		oAuthProvider.apiSecret = "secret";

		oAuthProvider.provider = "blabla";
		h.from(oAuthProvider, "http://localhost:8080/", users, sessionHandler, errorPage);
	}
}
