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

import java.util.Objects;

import lombok.Getter;
import io.lavagna.common.ConstructorAnnotationRowMapper.Column;

@Getter
public class RoleAndPermission {
	private final String roleName;

	private final boolean removable;

	private final boolean hidden;

	private final boolean readOnly;

	/** can be null */
	private final Permission permission;

	/** if permission is null, category is null too */
	private final PermissionCategory category;

	public RoleAndPermission(@Column("ROLE_NAME") String roleName, @Column("ROLE_REMOVABLE") boolean removable,
			@Column("ROLE_HIDDEN") boolean hidden, @Column("ROLE_READONLY") boolean readOnly,
			@Column("PERMISSION") Permission permission) {
		this.roleName = roleName;
		this.removable = removable;
		this.hidden = hidden;
		this.readOnly = readOnly;
		this.permission = permission;
		this.category = permission != null ? permission.getCategory() : null;
	}

	@Override
	public boolean equals(Object obj) {
		return (obj != null && obj instanceof RoleAndPermission) ? Objects.equals(roleName,
				((RoleAndPermission) obj).roleName) : false;
	}

	@Override
	public int hashCode() {
		return roleName.hashCode();
	}
}