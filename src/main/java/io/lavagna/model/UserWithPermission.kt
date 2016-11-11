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

import org.apache.commons.lang3.Validate
import java.util.*

open class UserWithPermission(user: User,
                              permissions: Set<Permission>,
                              permissionsForProject: Map<String, Set<Permission>>,
                              permissionsForProjectId: Map<Int, Set<Permission>>) : User(user.id, user.provider, user.username, user.email, user.displayName, user.enabled, user.emailNotification, user.memberSince, user.skipOwnNotifications, user.userMetadataRaw) {

    open val basePermissions: Map<Permission, Permission>
    private val permissionsForProject: MutableMap<String, Map<Permission, Permission>>
    private val permissionsForProjectId: MutableMap<Int, Map<Permission, Permission>>

    init {
        // identity map
        this.basePermissions = identityMap(permissions)

        this.permissionsForProject = HashMap<String, Map<Permission, Permission>>()
        for ((key, value) in permissionsForProject) {
            this.permissionsForProject.put(key, identityMap(value))
        }

        this.permissionsForProjectId = HashMap<Int, Map<Permission, Permission>>()
        for ((key, value) in permissionsForProjectId) {
            this.permissionsForProjectId.put(key, identityMap(value))
        }
    }

    open fun projectsWithPermission(p: Permission): Set<String> {
        val s = HashSet<String>()
        for ((key, value) in permissionsForProject) {
            if (value.containsKey(p)) {
                s.add(key)
            }
        }
        return s
    }

    open fun projectsIdWithPermission(p: Permission): Set<Int> {
        val s = HashSet<Int>()
        for ((key, value) in permissionsForProjectId) {
            if (value.containsKey(p)) {
                s.add(key)
            }
        }
        return s
    }

    private fun identityMap(permissions: Set<Permission>): Map<Permission, Permission> {
        val res = EnumMap<Permission, Permission>(Permission::class.java)
        for (p in permissions) {
            res.put(p, p)
        }
        return res
    }

    fun toProjectIdsFilter(projectId: Int?): Set<Int> {
        val projectIdFilter = HashSet<Int>()
        val hasGlobalRead = basePermissions.containsKey(Permission.READ)
        val projectIdsWithReadPermission = projectsIdWithPermission(Permission.READ)
        if (projectId != null) {
            if (!hasGlobalRead) {
                Validate.isTrue(projectIdsWithReadPermission.contains(projectId))
            }
            projectIdFilter.add(projectId)
        } else if (projectId == null && !hasGlobalRead) {
            projectIdFilter.addAll(projectIdsWithReadPermission)
        }
        return projectIdFilter
    }

    fun getPermissionsForProject(): Map<String, Map<Permission, Permission>> {
        return this.permissionsForProject
    }

    fun getPermissionsForProjectId(): Map<Int, Map<Permission, Permission>> {
        return this.permissionsForProjectId
    }
}
