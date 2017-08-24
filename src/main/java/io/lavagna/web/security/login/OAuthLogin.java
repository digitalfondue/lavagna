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
import io.lavagna.web.security.login.oauth.*;
import io.lavagna.web.security.login.oauth.OAuthResultHandler.OAuthRequestBuilder;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class OAuthLogin extends AbstractLoginHandler {

	private static final Map<String, OAuthResultHandlerFactory> SUPPORTED_OAUTH_HANDLER;
	private static final String USER_PROVIDER = "oauth";

	static {
		Map<String, OAuthResultHandlerFactory> r = new LinkedHashMap<>();
		//TODO: move the strings directly in the factory.
		r.put("bitbucket", BitbucketHandler.FACTORY);
		r.put("gitlab", GitlabHandler.FACTORY);
		r.put("github", GithubHandler.FACTORY);
		r.put("google", GoogleHandler.FACTORY);
		r.put("twitter", TwitterHandler.FACTORY);
		SUPPORTED_OAUTH_HANDLER = Collections.unmodifiableMap(r);
	}

	private final OauthConfigurationFetcher oauthConfigurationFetcher;
	private final String errorPage;
	private final OAuthServiceBuilder serviceBuilder;
	private final OAuthRequestBuilder reqBuilder = new OAuthRequestBuilder();

	public OAuthLogin(Users users, SessionHandler sessionHandler, OauthConfigurationFetcher oauthConfigurationFetcher, OAuthServiceBuilder serviceBuilder, String errorPage) {
		super(users, sessionHandler);
		this.oauthConfigurationFetcher = oauthConfigurationFetcher;
		this.serviceBuilder = serviceBuilder;
		this.errorPage = errorPage;
	}

	@Override
	public boolean doAction(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		OAuthConfiguration conf = oauthConfigurationFetcher.fetch();

		String requestURI = req.getRequestURI();

		if ("POST".equals(req.getMethod())) {
			OAuthProvider authHandler = conf.matchAuthorization(requestURI);
			if (authHandler != null) {
				from(authHandler, conf.baseUrl, users, sessionHandler, errorPage).handleAuthorizationUrl(req, resp);
				return true;
			}
		}

		OAuthProvider callbackHandler = conf.matchCallback(requestURI);
		if (callbackHandler != null) {
			from(callbackHandler, conf.baseUrl, users, sessionHandler, errorPage).handleCallback(req, resp);
			return true;
		}
		return false;
	}

	@Override
	public Map<String, Object> modelForLoginPage(HttpServletRequest request) {

		Map<String, Object> m = super.modelForLoginPage(request);

		OAuthConfiguration conf = oauthConfigurationFetcher.fetch();

        if (conf == null) {
            return m;
        }

		List<String> loginOauthProviders = new ArrayList<>();

		for (String p : getAllHandlers().keySet()) {
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

		public OAuthProvider getProviderWithName(String provider) {
            for (OAuthProvider o : providers) {
                if (provider.equals(o.getProvider())) {
                    return o;
                }
            }
            return null;
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
	    /**
	     * Can return null.
	     *
	     * @return
	     */
	    OAuthConfiguration fetch();
	}

    public OAuthResultHandler from(OAuthProvider oauthProvider, String confBaseUrl, Users users, SessionHandler sessionHandler, String errorPage) {
        String baseUrl = StringUtils.trimTrailingCharacter(confBaseUrl, '/');
        String callbackUrl = baseUrl + "/login/oauth/"+ oauthProvider.getProvider() + "/callback";

        Map<String, OAuthResultHandlerFactory> handlers = getAllHandlers();

        if (handlers.containsKey(oauthProvider.getProvider())) {
            return handlers.get(oauthProvider.getProvider()).build(serviceBuilder, reqBuilder, oauthProvider, callbackUrl, users, sessionHandler, errorPage);
        } else {
            throw new IllegalArgumentException("type " + oauthProvider.getProvider() + " is not supported");
        }
    }


	public Map<String, OAuthResultHandlerFactory> getAllHandlers() {

	    Map<String, OAuthResultHandlerFactory> res = new HashMap<>(SUPPORTED_OAUTH_HANDLER);

	    OAuthConfiguration conf = oauthConfigurationFetcher.fetch();
	    if(conf != null && conf.providers != null) {
	        for(OAuthProvider provider : conf.providers) {
	            if(provider.isHasCustomBaseAndProfileUrl()) {
	                res.put(provider.getProvider(), new CustomOAuthResultHandlerFactory(SUPPORTED_OAUTH_HANDLER.get(provider.getBaseProvider())));
	            }
	        }
	    }

	    return res;
	}

	private static class CustomOAuthResultHandlerFactory implements OAuthResultHandlerFactory {

	    private final OAuthResultHandlerFactory factory;

        private CustomOAuthResultHandlerFactory(OAuthResultHandlerFactory factory) {
	        this.factory = factory;
	    }

        @Override
        public OAuthResultHandler build(OAuthServiceBuilder serviceBuilder,
                OAuthRequestBuilder reqBuilder, OAuthProvider oauthProvider,
                String callback, Users users, SessionHandler sessionHandler,
                String errorPage) {
            return factory.build(serviceBuilder, reqBuilder, oauthProvider, callback, users, sessionHandler, errorPage);
        }

        @Override
        public boolean hasConfigurableBaseUrl() {
            return factory.hasConfigurableBaseUrl();
        }

        @Override
        public boolean isConfigurableInstance() {
            return true;
        }

	}

    @Override
    public List<String> getAllHandlerNames() {
        List<String> res = new ArrayList<>();
        for (String sub : getAllHandlers().keySet()) {
            res.add(USER_PROVIDER + "." + sub);
        }
        return res;
    }

    @Override
    public String getBaseProviderName() {
        return USER_PROVIDER;
    }
}
