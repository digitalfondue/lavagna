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

import org.springframework.util.StringUtils;

public class OAuthProvider {
    private final String provider;// google, github, bitbucket, twitter
    private final String apiKey;
    private final String apiSecret;
    //support for self hosted oauth2 provider (e.g. gitlab)
    private final boolean hasCustomBaseAndProfileUrl;
    private final String baseProvider;
    private final String baseUrl;
    private final String profileUrl;
    private final Boolean autoCreateMissingAccount;

    public OAuthProvider(String provider, String apiKey, String apiSecret) {
        this(provider, apiKey, apiSecret, false, null, null, null, null);
    }

    public OAuthProvider(String provider, String apiKey, String apiSecret, boolean hasCustomBaseAndProfileUrl, String baseProvider, String baseUrl, String profileUrl, Boolean autoCreateMissingAccount) {
        this.provider = provider;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.hasCustomBaseAndProfileUrl = hasCustomBaseAndProfileUrl;
        this.baseProvider = baseProvider;
        this.baseUrl = baseUrl;
        this.profileUrl = profileUrl;
        this.autoCreateMissingAccount = autoCreateMissingAccount;
    }

    public String baseUrlOrDefault(String defaultBaseUrl) {
        return hasCustomBaseAndProfileUrl ? baseUrl : defaultBaseUrl;
    }

    public String profileUrlOrDefault(String defaultProfileUrl) {
        return hasCustomBaseAndProfileUrl && StringUtils.hasText(profileUrl) ? profileUrl : defaultProfileUrl;
    }

    public boolean matchAuthorization(String requestURI) {
        return requestURI.endsWith("/oauth/" + provider);
    }

    public boolean matchCallback(String requestURI) {
        return requestURI.endsWith("/oauth/" + provider + "/callback");
    }

    public String getProvider() {
        return provider;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public boolean isHasCustomBaseAndProfileUrl() {
        return hasCustomBaseAndProfileUrl;
    }

    public String getBaseProvider() {
        return baseProvider;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public boolean getAutoCreateMissingAccount() {
        return autoCreateMissingAccount != null && autoCreateMissingAccount;
    }
}
