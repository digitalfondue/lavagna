/**
 * This file is part of lavagna.
 * <p/>
 * lavagna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * lavagna is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with lavagna.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.lavagna.service;

import static java.util.Objects.requireNonNull;
import io.lavagna.model.Permission;
import io.lavagna.model.Role;
import io.lavagna.model.User;
import io.lavagna.model.UserToCreate;
import io.lavagna.model.UserWithPermission;
import io.lavagna.service.PermissionService.ProjectRoleAndPermissionFullHolder;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link User} related operations.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final PermissionService permissionService;

	@Autowired
	public UserService(UserRepository userRepository, PermissionService permissionService) {
		this.userRepository = userRepository;
		this.permissionService = permissionService;
	}

	@Transactional(readOnly = false)
	public void createUser(UserToCreate userToCreate) {
		requireNonNull(userToCreate);
		requireNonNull(userToCreate.getProvider());
		requireNonNull(userToCreate.getUsername());

		userRepository.createUser(userToCreate.getProvider(), userToCreate.getUsername(), userToCreate.getEmail(),
				userToCreate.getDisplayName(), userToCreate.isEnabled());

		if (userToCreate.getRoles() == null) {
			return;
		}

		User u = userRepository.findUserByName(userToCreate.getProvider(), userToCreate.getUsername());
		Set<Integer> userId = Collections.singleton(u.getId());
		for (String r : userToCreate.getRoles()) {
			permissionService.assignRoleToUsers(new Role(r), userId);
		}
	}

	public UserWithPermission findUserWithPermission(int userId) {
		User user = userRepository.findById(userId);

		Set<Permission> permissions = permissionService.findBasePermissionByUserId(user.getId());
		ProjectRoleAndPermissionFullHolder permissionsHolder = permissionService
				.findPermissionsGroupedByProjectForUserId(user.getId());
		return new UserWithPermission(user, permissions, permissionsHolder.getPermissionsByProject(),
				permissionsHolder.getPermissionsByProjectId());
	}

	@Transactional(readOnly = false)
	public void createUsers(List<UserToCreate> usersToCreate) {
		for (UserToCreate utc : requireNonNull(usersToCreate)) {
			createUser(utc);
		}
	}

}
