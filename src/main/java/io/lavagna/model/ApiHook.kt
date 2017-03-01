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

import ch.digitalfondue.npjt.ConstructorAnnotationRowMapper.Column

class ApiHook(@Column("API_HOOK_NAME") val name: String,
			  @Column("API_HOOK_SCRIPT") val script: String,
			  @Column("API_HOOK_CONFIGURATION") val configuration: String?,
			  @Column("API_HOOK_ENABLED") val enabled: Boolean,
			  @Column("API_HOOK_TYPE") val type: Type,
			  @Column("API_HOOK_PROJECTS") val projects: String?,
			  @Column("API_HOOK_VERSION") val version: Int,
              @Column("API_HOOK_METADATA") val metadata: String?) {

	enum class Type {
        EVENT_EMITTER_HOOK, //react to event emitter action
		WEB_HOOK; // react to ApiHooksController.handleHook
	}
}
