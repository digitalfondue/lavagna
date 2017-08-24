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

import com.google.gson.annotations.SerializedName;
import io.lavagna.web.security.SecurityConfiguration.SessionHandler;
import io.lavagna.web.security.SecurityConfiguration.Users;
import io.lavagna.web.security.login.oauth.OAuthResultHandler.OAuthResultHandlerAdapter;
import org.scribe.builder.ServiceBuilder;

public class GoogleHandler extends OAuthResultHandlerAdapter {

    private GoogleHandler(OAuthServiceBuilder serviceBuilder, OAuthRequestBuilder reqBuilder, String apiKey,
			String apiSecret, String callback, Users users, SessionHandler sessionHandler, String errorPage) {
		super("oauth.google",//
				"https://www.googleapis.com/plus/v1/people/me/openIdConnect",//
				UserInfo.class, "code",//
				users,//
				sessionHandler,//
				errorPage,//
				serviceBuilder.build(new Google20Api(), apiKey, apiSecret, callback, "openid email"), reqBuilder);
	}

	static class UserInfo implements RemoteUserProfile {
		private String email;

		@SerializedName("email_verified")
		private boolean emailVerified;

		@Override
		public boolean valid(Users users, String provider) {
			return emailVerified && users.userExistsAndEnabled(provider, email);
		}

		@Override
		public String username() {
			return email;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public boolean isEmailVerified() {
			return emailVerified;
		}

		public void setEmailVerified(boolean emailVerified) {
			this.emailVerified = emailVerified;
		}
	}


    public static final OAuthResultHandlerFactory FACTORY = new OAuthResultHandlerFactory.Adapter() {

        @Override
        public OAuthResultHandler build(OAuthServiceBuilder serviceBuilder,
                OAuthRequestBuilder reqBuilder, OAuthProvider provider,
                String callback, Users users, SessionHandler sessionHandler,
                String errorPage) {
            return new GoogleHandler(serviceBuilder, reqBuilder, provider.getApiKey(), provider.getApiSecret(), callback, users, sessionHandler, errorPage);
        }

    };
}
