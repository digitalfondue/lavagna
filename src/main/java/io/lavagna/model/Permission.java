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

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import org.apache.commons.lang3.Validate;

public enum Permission {
	/* access to the admin tool */
	ADMINISTRATION(PermissionCategory.APPLICATION, true), //

	/* role for admin project */
	PROJECT_ADMINISTRATION(PermissionCategory.PROJECT),

	/* role for reading a project */
	CREATE_PROJECT(PermissionCategory.PROJECT, true),

	/* role for reading a board and related column/card/comments */
	READ(PermissionCategory.BOARD),

	/* can create new board */
	CREATE_BOARD(PermissionCategory.BOARD), //

	UPDATE_BOARD(PermissionCategory.BOARD), //
	DELETE_BOARD(PermissionCategory.BOARD), //

	/* can create new column */
	CREATE_COLUMN(PermissionCategory.COLUMN), //

	/* can reorder column */
	MOVE_COLUMN(PermissionCategory.COLUMN), //

	/* can rename column */
	RENAME_COLUMN(PermissionCategory.COLUMN), //

	/* can create cards */
	CREATE_CARD(PermissionCategory.CARD), //
	UPDATE_CARD(PermissionCategory.CARD), //
	MOVE_CARD(PermissionCategory.CARD), //

	//
	/* can create a comment */
	CREATE_CARD_COMMENT(PermissionCategory.CARD), //
	
	/* can update and delete comments from other users */
	UPDATE_CARD_COMMENT(PermissionCategory.CARD), //
	DELETE_CARD_COMMENT(PermissionCategory.CARD), //
	
	/* can manage a action list */
	MANAGE_ACTION_LIST(PermissionCategory.CARD),//

	// file related
	CREATE_FILE(PermissionCategory.CARD), //
	UPDATE_FILE(PermissionCategory.CARD), //
	DELETE_FILE(PermissionCategory.CARD), //
	//

	// label related
	MANAGE_LABEL_VALUE(PermissionCategory.CARD), //
	//

	/* can update the _current_ user profile */
	UPDATE_PROFILE(PermissionCategory.APPLICATION), //

	SEARCH(PermissionCategory.APPLICATION);

	private final PermissionCategory category;

	/**
	 * only for base permission, if false: cannot be used in Project level permission
	 */
	private final boolean onlyForBase;

	private static final Set<Permission> AVAILABLE_PERMISSION_FOR_PROJECT;

	static {
		Set<Permission> p = EnumSet.noneOf(Permission.class);
		for (Permission perm : Permission.values()) {
			if (!perm.onlyForBase) {
				p.add(perm);
			}
		}
		AVAILABLE_PERMISSION_FOR_PROJECT = Collections.unmodifiableSet(p);
	}

	public static void ensurePermissionForProject(Set<Permission> permissions) {
		Validate.isTrue(Permission.AVAILABLE_PERMISSION_FOR_PROJECT.containsAll(permissions),
				"permission at project level only: " + permissions + " contain a onlyForBase Permission.");
	}

	private Permission(PermissionCategory category, boolean onlyForBase) {
		this.category = category;
		this.onlyForBase = onlyForBase;
	}

	private Permission(PermissionCategory category) {
		this(category, false);
	}

	public PermissionCategory getCategory() {
		return category;
	}

	public boolean isOnlyForBase() {
		return onlyForBase;
	}
}
