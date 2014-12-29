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

import io.lavagna.common.Json;
import io.lavagna.model.Key;
import io.lavagna.service.ConfigurationRepository;
import io.lavagna.service.UserRepository;
import io.lavagna.web.security.login.LoginHandler.AbstractLoginHandler;
import io.lavagna.web.security.login.oauth.BitbucketHandler;
import io.lavagna.web.security.login.oauth.GithubHandler;
import io.lavagna.web.security.login.oauth.GoogleHandler;
import io.lavagna.web.security.login.oauth.OAuthResultHandler;
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

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.scribe.builder.ServiceBuilder;
import org.springframework.util.StringUtils;

public class OAuthLogin extends AbstractLoginHandler {

	static final Map<String, Class<? extends OAuthResultHandler>> SUPPORTED_OAUTH_HANDLER;

	static {
		Map<String, Class<? extends OAuthResultHandler>> r = new LinkedHashMap<>();
		r.put("bitbucket", BitbucketHandler.class);
		r.put("github", GithubHandler.class);
		r.put("google", GoogleHandler.class);
		r.put("twitter", TwitterHandler.class);
		SUPPORTED_OAUTH_HANDLER = Collections.unmodifiableMap(r);
	}

	private final ConfigurationRepository configurationRepository;
	private final String errorPage;
	private final Handler handler;

	public OAuthLogin(UserRepository userRepository, ConfigurationRepository configurationRepository, Handler handler,
			String errorPage) {
		super(userRepository);
		this.configurationRepository = configurationRepository;
		this.errorPage = errorPage;
		this.handler = handler;

	}

	@Override
	public boolean doAction(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		OAuthConfiguration conf = Json.GSON.fromJson(configurationRepository.getValue(Key.OAUTH_CONFIGURATION),
				OAuthConfiguration.class);

		String requestURI = req.getRequestURI();

		if ("POST".equals(req.getMethod())) {
			OAuthProvider authHandler = conf.matchAuthorization(requestURI);
			if (authHandler != null) {
				handler.from(authHandler, conf.baseUrl, userRepository, errorPage).handleAuthorizationUrl(req, resp);
				return true;
			}
		}

		OAuthProvider callbackHandler = conf.matchCallback(requestURI);
		if (callbackHandler != null) {
			handler.from(callbackHandler, conf.baseUrl, userRepository, errorPage).handleCallback(req, resp);
			return true;
		}
		return false;
	}

	@Override
	public Map<String, Object> modelForLoginPage(HttpServletRequest request) {

		Map<String, Object> m = super.modelForLoginPage(request);

		OAuthConfiguration conf = Json.GSON.fromJson(configurationRepository.getValue(Key.OAUTH_CONFIGURATION),
				OAuthConfiguration.class);

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

	static class OAuthConfiguration {
		String baseUrl;
		List<OAuthProvider> providers;

		public boolean hasProvider(String provider) {
			for (OAuthProvider o : providers) {
				if (provider.equals(o.provider)) {
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

	public static class Handler {

		private final ServiceBuilder serviceBuilder;
		private final OAuthRequestBuilder reqBuilder = new OAuthRequestBuilder();

		public Handler(ServiceBuilder serviceBuilder) {
			this.serviceBuilder = serviceBuilder;
		}

		// TODO: refactor
		public OAuthResultHandler from(OAuthProvider oauthProvider, String confBaseUrl, UserRepository userRepository,
				String errorPage) {
			String baseUrl = StringUtils.trimTrailingCharacter(confBaseUrl, '/');
			String callbackUrl = baseUrl + "/login/oauth/" + oauthProvider.provider + "/callback";
			if (SUPPORTED_OAUTH_HANDLER.containsKey(oauthProvider.provider)) {
				try {
					return ConstructorUtils.invokeConstructor(SUPPORTED_OAUTH_HANDLER.get(oauthProvider.provider),
							serviceBuilder, reqBuilder, oauthProvider.apiKey, oauthProvider.apiSecret, callbackUrl,
							userRepository, errorPage);
				} catch (ReflectiveOperationException iea) {
					throw new IllegalStateException(iea);
				}
			} else {
				throw new IllegalArgumentException("type " + oauthProvider.provider + " is not supported");
			}
		}
	}

	static class OAuthProvider {
		String provider;// google, github, bitbucket, twitter
		String apiKey;
		String apiSecret;

		public boolean matchAuthorization(String requestURI) {
			return requestURI.endsWith("login/oauth/" + provider);
		}

		public boolean matchCallback(String requestURI) {
			return requestURI.endsWith("login/oauth/" + provider + "/callback");
		}
	}
}
