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

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Api;
import org.scribe.oauth.OAuthService;

public class OAuthServiceBuilder {


    public OAuthService build(Api api, String apiKey, String apiSecret, String callback) {
        return new ServiceBuilder().provider(api).apiKey(apiKey).apiSecret(apiSecret).callback(callback).build();
    }

    public OAuthService build(Api api, String apiKey, String apiSecret, String callback, String scope) {
        return new ServiceBuilder().provider(api).apiKey(apiKey).apiSecret(apiSecret).callback(callback).scope(scope).build();
    }
}
