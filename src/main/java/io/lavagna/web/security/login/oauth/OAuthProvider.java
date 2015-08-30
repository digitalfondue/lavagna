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

import lombok.Getter;

@Getter
public class OAuthProvider {
    private final String provider;// google, github, bitbucket, twitter
    private final String apiKey;
    private final String apiSecret;
    
    //support for self hosted oauth2 provider (e.g. gitlab)
    private final boolean hasCustomBaseAndProfileUrl;
    private final String baseUrl;
    private final String profileUrl;
    
    public OAuthProvider(String provider, String apiKey, String apiSecret) {
        this(provider, apiKey, apiSecret, false, null, null);
    }
    
    public OAuthProvider(String provider, String apiKey, String apiSecret, boolean hasCustomBaseAndProfileUrl, String baseUrl, String profileUrl) {
        this.provider = provider;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.hasCustomBaseAndProfileUrl = hasCustomBaseAndProfileUrl;
        this.baseUrl = baseUrl;
        this.profileUrl = profileUrl;
    }

    public boolean matchAuthorization(String requestURI) {
        return requestURI.endsWith("/oauth/" + provider);
    }

    public boolean matchCallback(String requestURI) {
        return requestURI.endsWith("/oauth/" + provider + "/callback");
    }
}