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
package io.lavagna.web.security.login.oauth;

import io.lavagna.web.security.SecurityConfiguration.SessionHandler;
import io.lavagna.web.security.SecurityConfiguration.User;
import io.lavagna.web.security.SecurityConfiguration.Users;
import io.lavagna.web.security.login.oauth.OAuthResultHandler.OAuthRequestBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.scribe.builder.api.Api;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HandlersTest {

	@Mock
	private OAuthServiceBuilder sBuilder;
	@Mock
	private SessionHandler sessionHandler;
	@Mock
	private Users users;
	@Mock
	private HttpServletRequest req, req2;
	@Mock
	private HttpServletResponse resp, resp2;

	@Mock
	private OAuthService oauthService;

	@Mock
	private ServletContext servletContext;
	@Mock
	private WebApplicationContext webappContext;
	@Mock
	private OAuthRequestBuilder reqBuilder;
	@Mock
	private OAuthRequest oauthReq;
	@Mock
	private Response oauthRes;
	@Mock
	private User user;

	private MockHttpSession session;

	private String key = "key";
	private String secret = "secret";
	private String callback = "callback";
	private String errPage = "error";

	private OAuthResultHandler bitbucketHandler;
	private OAuthResultHandler googleHandler;
	private OAuthResultHandler githubHandler;
    private OAuthResultHandler gitlabHandler;
    private OAuthResultHandler twitterHandler;

	@Before
	public void prepare() {

		when(sBuilder.build(any(Api.class), any(String.class), any(String.class), any(String.class))).thenReturn(oauthService);
        when(sBuilder.build(any(Api.class), any(String.class), any(String.class), any(String.class), any(String.class))).thenReturn(oauthService);

		session = new MockHttpSession();

		when(req2.getParameter("code")).thenReturn("code");
		when(req2.getParameter("oauth_verifier")).thenReturn("code");

		when(req.getSession()).thenReturn(session);

		when(req2.getSession()).thenReturn(session);

		when(reqBuilder.req(any(Verb.class), any(String.class))).thenReturn(oauthReq);
		when(oauthReq.send()).thenReturn(oauthRes);
		when(users.findUserByName(any(String.class), any(String.class))).thenReturn(user);

		bitbucketHandler = BitbucketHandler.FACTORY.build(sBuilder, reqBuilder, new OAuthProvider("bitbucket", key, secret), callback, users, sessionHandler, errPage);
		githubHandler = GithubHandler.FACTORY.build(sBuilder, reqBuilder, new OAuthProvider("github", key, secret), callback, users, sessionHandler, errPage);
		googleHandler = GoogleHandler.FACTORY.build(sBuilder, reqBuilder, new OAuthProvider("google", key, secret), callback, users, sessionHandler, errPage);
		gitlabHandler = GitlabHandler.FACTORY.build(sBuilder, reqBuilder, new OAuthProvider("gitlab", key, secret), callback, users, sessionHandler, errPage);
		twitterHandler = TwitterHandler.FACTORY.build(sBuilder, reqBuilder, new OAuthProvider("twiiter", key, secret), callback, users, sessionHandler, errPage);
	}

	@Test
	public void handleBitbucketFlowAuth() throws IOException {
	    when(oauthService.getAuthorizationUrl(null)).thenReturn("redirect");
	    bitbucketHandler.handleAuthorizationUrl(req, resp);
        verify(resp).sendRedirect("redirect&state=" + session.getAttribute("EXPECTED_STATE_FOR_oauth.bitbucket"));

        when(req2.getParameter("state")).thenReturn((String) session.getAttribute("EXPECTED_STATE_FOR_oauth.bitbucket"));

        when(oauthRes.getBody()).thenReturn("{\"username\" : \"username\"}");
        when(users.userExistsAndEnabled("oauth.bitbucket", "username")).thenReturn(true);
        when(users.findUserByName("oauth.bitbucket", "username")).thenReturn(user);
        when(req2.getContextPath()).thenReturn("");

        Assert.assertTrue(!session.isInvalid());
        bitbucketHandler.handleCallback(req2, resp2);
        verify(resp2).sendRedirect("/");

        verify(sessionHandler).setUser(user.getId(), user.isAnonymous(), req2, resp2);
	}

	@Test
    public void handleTwitterFlowAuth() throws IOException {
        when(oauthService.getAuthorizationUrl(null)).thenReturn("redirect");
        twitterHandler.handleAuthorizationUrl(req, resp);
        verify(resp).sendRedirect("redirect");

        when(oauthRes.getBody()).thenReturn("{\"screen_name\" : \"username\"}");
        when(users.userExistsAndEnabled("oauth.twitter", "username")).thenReturn(true);
        when(users.findUserByName("oauth.twitter", "username")).thenReturn(user);
        when(req2.getContextPath()).thenReturn("");
        Assert.assertTrue(!session.isInvalid());
        twitterHandler.handleCallback(req2, resp2);
        verify(resp2).sendRedirect("/");
        verify(sessionHandler).setUser(user.getId(), user.isAnonymous(), req2, resp2);
    }

	@Test
	public void handleGithubFlowAuth() throws IOException {
		when(oauthService.getAuthorizationUrl(null)).thenReturn("redirect");
		githubHandler.handleAuthorizationUrl(req, resp);
		verify(resp).sendRedirect("redirect&state=" + session.getAttribute("EXPECTED_STATE_FOR_oauth.github"));

		when(req2.getParameter("state")).thenReturn((String) session.getAttribute("EXPECTED_STATE_FOR_oauth.github"));

		when(oauthRes.getBody()).thenReturn("{\"login\" : \"login\"}");
		when(users.userExistsAndEnabled("oauth.github", "login")).thenReturn(true);
		when(users.findUserByName("oauth.github", "login")).thenReturn(user);
		when(req2.getContextPath()).thenReturn("");

		Assert.assertTrue(!session.isInvalid());
		githubHandler.handleCallback(req2, resp2);
		verify(resp2).sendRedirect("/");

		verify(sessionHandler).setUser(user.getId(), user.isAnonymous(), req2, resp2);
	}

	@Test
	public void handleGoogleFlowAuth() throws IOException {
		when(oauthService.getAuthorizationUrl(null)).thenReturn("redirect");
		googleHandler.handleAuthorizationUrl(req, resp);
		verify(resp).sendRedirect("redirect&state=" + session.getAttribute("EXPECTED_STATE_FOR_oauth.google"));

		when(req2.getParameter("state")).thenReturn((String) session.getAttribute("EXPECTED_STATE_FOR_oauth.google"));
		when(oauthRes.getBody()).thenReturn("{\"email\" : \"email\", \"email_verified\" : true}");
		when(users.userExistsAndEnabled("oauth.google", "email")).thenReturn(true);
		when(users.findUserByName("oauth.google", "email")).thenReturn(user);
		when(req2.getContextPath()).thenReturn("/context-path");

		Assert.assertTrue(!session.isInvalid());
		googleHandler.handleCallback(req2, resp2);
		verify(resp2).sendRedirect("/context-path/");

		verify(sessionHandler).setUser(user.getId(), user.isAnonymous(), req2, resp2);
	}


	@Test
    public void handleGitlabFlowAuth() throws IOException {
        when(oauthService.getAuthorizationUrl(null)).thenReturn("redirect");
        gitlabHandler.handleAuthorizationUrl(req, resp);
        verify(resp).sendRedirect("redirect&state=" + session.getAttribute("EXPECTED_STATE_FOR_oauth.gitlab"));

        when(req2.getParameter("state")).thenReturn((String) session.getAttribute("EXPECTED_STATE_FOR_oauth.gitlab"));
        when(oauthRes.getBody()).thenReturn("{\"username\" : \"username\"}");
        when(users.userExistsAndEnabled("oauth.gitlab", "username")).thenReturn(true);
        when(users.findUserByName("oauth.gitlab", "username")).thenReturn(user);
        when(req2.getContextPath()).thenReturn("/context-path");

        Assert.assertTrue(!session.isInvalid());
        gitlabHandler.handleCallback(req2, resp2);
        verify(resp2).sendRedirect("/context-path/");

        verify(sessionHandler).setUser(user.getId(), user.isAnonymous(), req2, resp2);
    }
}
