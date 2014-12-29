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

import org.scribe.builder.ServiceBuilder;

public class BitbucketHandler extends AbstractOAuth1Handler {

	public BitbucketHandler(ServiceBuilder serviceBuilder, OAuthRequestBuilder reqBuilder, String apiKey,
			String apiSecret, String callback, UserRepository userRepository, String errorPage) {
		super("oauth.bitbucket",//
				"https://bitbucket.org/api/1.0/user",//
				UserInfo.class, "oauth_verifier", //
				userRepository,//
				errorPage,//
				serviceBuilder.provider(new Bitbucket10Api()).apiKey(apiKey).apiSecret(apiSecret).callback(callback)
						.build(), reqBuilder);
	}

	private static class UserInfo implements RemoteUserProfile {

		User user;

		@Override
		public boolean valid(UserRepository userRepository, String provider) {
			return userRepository.userExistsAndEnabled(provider, user.username);
		}

		@Override
		public String username() {
			return user.username;
		}
	}

	private static class User {
		String username;
	}
}
