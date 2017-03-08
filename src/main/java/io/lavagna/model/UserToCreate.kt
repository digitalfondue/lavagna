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

class UserToCreate {

    var provider: String? = null
    var username: String? = null
    var password: String? = null
    var email: String? = null
    var displayName: String? = null
    var enabled: Boolean = false
    var roles: List<String>? = null

    constructor() {
    }

    constructor(provider: String, username: String) {
        this.provider = provider
        this.username = username
        enabled = true
    }
}
