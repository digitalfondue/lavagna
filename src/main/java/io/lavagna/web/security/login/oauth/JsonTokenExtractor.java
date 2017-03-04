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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import org.scribe.exceptions.OAuthException;
import org.scribe.extractors.AccessTokenExtractor;
import org.scribe.model.Token;

class JsonTokenExtractor implements AccessTokenExtractor {

    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

	@Override
	public Token extract(String response) {
		try {
			return new Token(GSON.fromJson(response, JsonTokenExtractor.AccessToken.class).getAccessToken(), "", response);
		} catch (JsonSyntaxException | NullPointerException e) {
			throw new OAuthException("Cannot extract an acces token. Response was: " + response, e);
		}
	}

	static class AccessToken {
		@SerializedName("access_token")
		private String accessToken;

		public String getAccessToken() {
			return accessToken;
		}

		public void setAccessToken(String accessToken) {
			this.accessToken = accessToken;
		}
	}

}
