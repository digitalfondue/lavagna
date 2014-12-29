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

import io.lavagna.service.UserRepository;
import io.lavagna.web.security.login.oauth.OAuthResultHandler.OAuthResultHandlerAdapter;

import org.scribe.builder.ServiceBuilder;

public class GithubHandler extends OAuthResultHandlerAdapter {

	public GithubHandler(ServiceBuilder serviceBuilder, OAuthRequestBuilder reqBuilder, String apiKey,
			String apiSecret, String callback, UserRepository userRepository, String errorPage) {
		super("oauth.github",//
				"https://api.github.com/user",//
				UserInfo.class, "code",//
				userRepository,//
				errorPage,//
				serviceBuilder.provider(new Github20Api()).apiKey(apiKey).apiSecret(apiSecret).callback(callback)
						.build(), reqBuilder);
	}

	private static class UserInfo implements RemoteUserProfile {
		String login;

		@Override
		public boolean valid(UserRepository userRepository, String provider) {
			return userRepository.userExistsAndEnabled(provider, login);
		}

		@Override
		public String username() {
			return login;
		}
	}

}
