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
import com.google.gson.reflect.TypeToken
import io.lavagna.common.Json
import java.util.*

class ApiHook(@Column("API_HOOK_NAME") val name: String,
			  @Column("API_HOOK_SCRIPT") val script: String,
			  @Column("API_HOOK_CONFIGURATION") @Transient val configurationRaw: String?,
			  @Column("API_HOOK_ENABLED") val enabled: Boolean,
			  @Column("API_HOOK_TYPE") val type: Type,
			  @Column("API_HOOK_PROJECTS") @Transient val projectsRaw: String?,
			  @Column("API_HOOK_VERSION") val version: Int,
              @Column("API_HOOK_METADATA") @Transient val metadataRaw: String?) {

    val metadata: Map<String, Object>?;
    val configuration: Map<String, String>?;
    val projects: List<String>?;

    init {
        val typeStringObj = object : TypeToken<Map<String, Object>>() {}.type
        this.metadata = if (metadataRaw == null) { Collections.emptyMap() } else { Json.GSON.fromJson(metadataRaw, typeStringObj)}

        val typeStringString = object : TypeToken<Map<String, String>>() {}.type
        this.configuration = if (configurationRaw == null) { Collections.emptyMap() } else { Json.GSON.fromJson(configurationRaw, typeStringString)}

        val typeListString = object : TypeToken<List<String>>() {}.type
        this.projects = if (projectsRaw == null) { null } else { Json.GSON.fromJson(projectsRaw, typeListString)}
    }

	enum class Type {
        EVENT_EMITTER_HOOK, //react to event emitter action
		WEB_HOOK; // react to ApiHooksController.handleHook
	}
}
