/**
 * This file is part of lavagna.

 * lavagna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * lavagna is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with lavagna.  If not, see //www.gnu.org/licenses/>.
 */
package io.lavagna.model

enum class Key {
    SETUP_COMPLETE, //
    BASE_APPLICATION_URL, //
    AUTHENTICATION_METHOD, //
    //
    LDAP_SERVER_URL, // ldap://localhost:10389
    LDAP_MANAGER_DN, // uid=admin,ou=system
    LDAP_MANAGER_PASSWORD, // secret
    //
    LDAP_USER_SEARCH_BASE, // ou=system
    LDAP_USER_SEARCH_FILTER, // uid={0}
    LDAP_AUTOCREATE_MISSING_ACCOUNT,
    //
    @Deprecated("") // kept for retrocompatibility
    PERSONA_AUDIENCE, // http://localhost:8080 (
    //
    OAUTH_CONFIGURATION,
    //
    @Deprecated("") // kept for retrocompatibility
    USE_HTTPS,
    //
    ENABLE_ANON_USER,
    //
    SMTP_ENABLED,
    SMTP_CONFIG, EMAIL_NOTIFICATION_TIMESPAN,
    //
    TRELLO_API_KEY,
    //
    MAX_UPLOAD_FILE_SIZE, // for uploaded body by the user (import data is not under this limit)
    //
    TEST_PLACEHOLDER
}
