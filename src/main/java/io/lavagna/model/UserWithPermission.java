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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lombok.Getter;

import org.apache.commons.lang3.Validate;

@Getter
public class UserWithPermission extends User {

	private final Map<Permission, Permission> basePermissions;
	private final Map<String, Map<Permission, Permission>> permissionsForProject;
	private final Map<Integer, Map<Permission, Permission>> permissionsForProjectId;

	public UserWithPermission(User user, Set<Permission> permissions,
			Map<String, Set<Permission>> permissionsForProject, Map<Integer, Set<Permission>> permissionsForProjectId) {
		super(user.getId(), user.getProvider(), user.getUsername(), user.getEmail(), user.getDisplayName(), user
				.isEnabled(), user.isEmailNotification(), user.getMemberSince());
		// identity map
		this.basePermissions = identityMap(permissions);

		this.permissionsForProject = new HashMap<>();
		for (Entry<String, Set<Permission>> pp : permissionsForProject.entrySet()) {
			this.permissionsForProject.put(pp.getKey(), identityMap(pp.getValue()));
		}

		this.permissionsForProjectId = new HashMap<>();
		for (Entry<Integer, Set<Permission>> pp : permissionsForProjectId.entrySet()) {
			this.permissionsForProjectId.put(pp.getKey(), identityMap(pp.getValue()));
		}
	}

	public Set<String> projectsWithPermission(Permission p) {
		Set<String> s = new HashSet<>();
		for (Entry<String, Map<Permission, Permission>> f : permissionsForProject.entrySet()) {
			if (f.getValue().containsKey(p)) {
				s.add(f.getKey());
			}
		}
		return s;
	}

	public Set<Integer> projectsIdWithPermission(Permission p) {
		Set<Integer> s = new HashSet<>();
		for (Entry<Integer, Map<Permission, Permission>> f : permissionsForProjectId.entrySet()) {
			if (f.getValue().containsKey(p)) {
				s.add(f.getKey());
			}
		}
		return s;
	}

	private static Map<Permission, Permission> identityMap(Set<Permission> permissions) {
		Map<Permission, Permission> res = new EnumMap<>(Permission.class);
		for (Permission p : permissions) {
			res.put(p, p);
		}
		return res;
	}

	public Set<Integer> toProjectIdsFilter(Integer projectId) {
		Set<Integer> projectIdFilter = new HashSet<>();
		boolean hasGlobalRead = getBasePermissions().containsKey(Permission.READ);
		Set<Integer> projectIdsWithReadPermission = projectsIdWithPermission(Permission.READ);
		if (projectId != null) {
			if (!hasGlobalRead) {
				Validate.isTrue(projectIdsWithReadPermission.contains(projectId));
			}
			projectIdFilter.add(projectId);
		} else if (projectId == null && !hasGlobalRead) {
			projectIdFilter.addAll(projectIdsWithReadPermission);
		}
		return projectIdFilter;
	}
}