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

class ProjectRoleAndPermission(@Column("PROJECT_ID") val projectId: Int,
                               @Column("PROJECT_SHORT_NAME") val projectShortName: String, @Column("ROLE_NAME") val roleName: String,
                               @Column("ROLE_REMOVABLE") val removable: Boolean,
                               /** can be null  */
                               @Column("PERMISSION") val permission: Permission?) {

    /** is null if permission is null  */
    val category: PermissionCategory?

    init {
        this.category = permission?.category
    }
}
