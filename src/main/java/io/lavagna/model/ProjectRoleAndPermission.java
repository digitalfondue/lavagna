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
package io.lavagna.model;

import lombok.Getter;
import io.lavagna.common.ConstructorAnnotationRowMapper.Column;

@Getter
public class ProjectRoleAndPermission {

	final String projectShortName;
	final int projectId;
	private final String roleName;
	private final boolean removable;

	/** can be null */
	final Permission permission;

	/** is null if permission is null */
	private final PermissionCategory category;

	public ProjectRoleAndPermission(@Column("PROJECT_ID") int projectId,
			@Column("PROJECT_SHORT_NAME") String projectShortName, @Column("ROLE_NAME") String roleName,
			@Column("ROLE_REMOVABLE") boolean removable, @Column("PERMISSION") Permission permission) {
		this.projectShortName = projectShortName;
		this.projectId = projectId;
		this.roleName = roleName;
		this.removable = removable;
		this.permission = permission;
		this.category = permission != null ? permission.getCategory() : null;
	}
}