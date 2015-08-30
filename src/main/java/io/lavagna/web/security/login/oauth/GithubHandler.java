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
import io.lavagna.web.security.SecurityConfiguration.Users;
import io.lavagna.web.security.login.oauth.OAuthResultHandler.OAuthResultHandlerAdapter;

import org.scribe.builder.ServiceBuilder;

public class GithubHandler extends OAuthResultHandlerAdapter {

    private GithubHandler(ServiceBuilder serviceBuilder, OAuthRequestBuilder reqBuilder, String apiKey,
			String apiSecret, String callback, Users users, SessionHandler sessionHandler, String errorPage) {
		super("oauth.github",//
				"https://api.github.com/user",//
				UserInfo.class, "code",//
				users,//
				sessionHandler,//
				errorPage,//
				serviceBuilder.provider(new Github20Api()).apiKey(apiKey).apiSecret(apiSecret).callback(callback)
						.build(), reqBuilder);
	}

	private static class UserInfo implements RemoteUserProfile {
		String login;

		@Override
		public boolean valid(Users users, String provider) {
			return users.userExistsAndEnabled(provider, login);
		}

		@Override
		public String username() {
			return login;
		}
	}
	
	public static final OAuthResultHandlerFactory FACTORY = new OAuthResultHandlerFactory() {
        
        @Override
        public OAuthResultHandler build(ServiceBuilder serviceBuilder,
                OAuthRequestBuilder reqBuilder, OAuthProvider provider,
                String callback, Users users, SessionHandler sessionHandler,
                String errorPage) {
            return new GithubHandler(serviceBuilder, reqBuilder, provider.getApiKey(), provider.getApiSecret(), callback, users, sessionHandler, errorPage);
        }
    };

}
