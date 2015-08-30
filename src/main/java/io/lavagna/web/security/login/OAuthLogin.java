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

import io.lavagna.web.security.LoginHandler.AbstractLoginHandler;
import io.lavagna.web.security.SecurityConfiguration.SessionHandler;
import io.lavagna.web.security.SecurityConfiguration.Users;
import io.lavagna.web.security.login.oauth.BitbucketHandler;
import io.lavagna.web.security.login.oauth.GithubHandler;
import io.lavagna.web.security.login.oauth.GitlabHandler;
import io.lavagna.web.security.login.oauth.GoogleHandler;
import io.lavagna.web.security.login.oauth.OAuthProvider;
import io.lavagna.web.security.login.oauth.OAuthResultHandler;
import io.lavagna.web.security.login.oauth.OAuthResultHandlerFactory;
import io.lavagna.web.security.login.oauth.OAuthResultHandler.OAuthRequestBuilder;
import io.lavagna.web.security.login.oauth.TwitterHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.scribe.builder.ServiceBuilder;
import org.springframework.util.StringUtils;

public class OAuthLogin extends AbstractLoginHandler {

	static final Map<String, OAuthResultHandlerFactory> SUPPORTED_OAUTH_HANDLER;
	static final String USER_PROVIDER = "oauth";

	static {
		Map<String, OAuthResultHandlerFactory> r = new LinkedHashMap<>();
		r.put("bitbucket", BitbucketHandler.FACTORY);
		r.put("gitlab", GitlabHandler.FACTORY);
		r.put("github", GithubHandler.FACTORY);
		r.put("google", GoogleHandler.FACTORY);
		r.put("twitter", TwitterHandler.FACTORY);
		SUPPORTED_OAUTH_HANDLER = Collections.unmodifiableMap(r);
	}

	private final OauthConfigurationFetcher oauthConfigurationFetcher;
	private final String errorPage;
	private final Handler handler;

	public OAuthLogin(Users users, SessionHandler sessionHandler, OauthConfigurationFetcher oauthConfigurationFetcher, Handler handler,
			String errorPage) {
		super(users, sessionHandler);
		this.oauthConfigurationFetcher = oauthConfigurationFetcher;
		this.errorPage = errorPage;
		this.handler = handler;
	}

	@Override
	public boolean doAction(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		OAuthConfiguration conf = oauthConfigurationFetcher.fetch();

		String requestURI = req.getRequestURI();

		if ("POST".equals(req.getMethod())) {
			OAuthProvider authHandler = conf.matchAuthorization(requestURI);
			if (authHandler != null) {
				handler.from(authHandler, conf.baseUrl, users, sessionHandler, errorPage).handleAuthorizationUrl(req, resp);
				return true;
			}
		}

		OAuthProvider callbackHandler = conf.matchCallback(requestURI);
		if (callbackHandler != null) {
			handler.from(callbackHandler, conf.baseUrl, users, sessionHandler, errorPage).handleCallback(req, resp);
			return true;
		}
		return false;
	}

	@Override
	public Map<String, Object> modelForLoginPage(HttpServletRequest request) {

		Map<String, Object> m = super.modelForLoginPage(request);

		OAuthConfiguration conf = oauthConfigurationFetcher.fetch();

		List<String> loginOauthProviders = new ArrayList<>();

		for (String p : SUPPORTED_OAUTH_HANDLER.keySet()) {
			if (conf.hasProvider(p)) {
				loginOauthProviders.add(p);
			}
		}
		m.put("loginOauthProviders", loginOauthProviders);
		m.put("loginOauth", "block");

		return m;
	}

	public static class OAuthConfiguration {
	    
		private final String baseUrl;
		private final List<OAuthProvider> providers;
		
		public OAuthConfiguration(String baseUrl, List<OAuthProvider> providers) {
		    this.baseUrl = baseUrl;
		    this.providers = providers;
		}

		public boolean hasProvider(String provider) {
			for (OAuthProvider o : providers) {
				if (provider.equals(o.getProvider())) {
					return true;
				}
			}
			return false;
		}

		public OAuthProvider matchAuthorization(String requestURI) {
			for (OAuthProvider o : providers) {
				if (o.matchAuthorization(requestURI)) {
					return o;
				}
			}
			return null;
		}

		public OAuthProvider matchCallback(String requestURI) {
			for (OAuthProvider o : providers) {
				if (o.matchCallback(requestURI)) {
					return o;
				}
			}
			return null;
		}
	}
	
	public interface OauthConfigurationFetcher {
	    OAuthConfiguration fetch();
	}

	public static class Handler {

		private final ServiceBuilder serviceBuilder;
		private final OAuthRequestBuilder reqBuilder = new OAuthRequestBuilder();

		public Handler(ServiceBuilder serviceBuilder) {
			this.serviceBuilder = serviceBuilder;
		}

		public OAuthResultHandler from(OAuthProvider oauthProvider, String confBaseUrl, Users users, SessionHandler sessionHandler,
				String errorPage) {
			String baseUrl = StringUtils.trimTrailingCharacter(confBaseUrl, '/');
			String callbackUrl = baseUrl + "/login/oauth/" + oauthProvider.getProvider() + "/callback";
			if (SUPPORTED_OAUTH_HANDLER.containsKey(oauthProvider.getProvider())) {
			    return SUPPORTED_OAUTH_HANDLER.get(oauthProvider.getProvider()).build(serviceBuilder, reqBuilder, 
			            oauthProvider, callbackUrl,users, sessionHandler, errorPage);
			} else {
				throw new IllegalArgumentException("type " + oauthProvider.getProvider() + " is not supported");
			}
		}
	}

    @Override
    public List<String> getAllHandlerNames() {
        List<String> res = new ArrayList<>();
        for (String sub : SUPPORTED_OAUTH_HANDLER.keySet()) {
            res.add(USER_PROVIDER + "." + sub);
        }
        return res;
    }

    @Override
    public String getBaseProviderName() {
        return USER_PROVIDER;
    }
}
