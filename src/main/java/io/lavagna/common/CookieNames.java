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

package io.lavagna.common;

public class CookieNames {

    private static String SESSION_COOKIE_NAME = "LAVAGNA_SESSION_ID";
    private static String REMEMBER_ME_COOKIE = "LAVAGNA_REMEMBER_ME";
    
    public static final String PROPERTY_NAME = "cookiePrefix";

    public static void updatePrefix(String prefix) {
        SESSION_COOKIE_NAME = prefix + "-LAVAGNA_SESSION_ID";
        REMEMBER_ME_COOKIE = prefix + "-LAVAGNA_REMEMBER_ME";
    }
    
    public static String getSessionCookieName() {
        return SESSION_COOKIE_NAME;
    }
    
    public static String getRememberMeCookieName() {
        return REMEMBER_ME_COOKIE;
    }
}
