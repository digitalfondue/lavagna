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
package io.lavagna.web.api;

import io.lavagna.model.Permission;
import io.lavagna.model.Role;
import io.lavagna.model.RoleAndMetadata;
import io.lavagna.service.EventEmitter;
import io.lavagna.service.PermissionService;
import io.lavagna.web.api.model.CreateRole;
import io.lavagna.web.api.model.UpdateRole;
import io.lavagna.web.api.model.Users;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PermissionControllerTest {

	@Mock
	private PermissionService permissionService;
	@Mock
	private EventEmitter eventEmitter;

	private PermissionController permissionController;

	private String roleName = "ROLENAME";

	@Before
	public void prepare() {
		permissionController = new PermissionController(permissionService, eventEmitter);
	}

	@Test
	public void assignUsersToRole() {
		Users usersToAdd = new Users();
		usersToAdd.setUserIds(Collections.singleton(1));
		permissionController.assignUsersToRole(roleName, usersToAdd);

		verify(permissionService).assignRoleToUsers(new Role(roleName), usersToAdd.getUserIds());
		verify(eventEmitter).emitAssignRoleToUsers(roleName);
	}

	@Test
	public void createRole() {
		CreateRole newRole = new CreateRole();
		newRole.setName("name");
		permissionController.createRole(newRole);

		verify(permissionService).createRole(new Role("name"));
		verify(eventEmitter).emitCreateRole();
	}

	@Test
	public void deleteRole() {
		when(permissionService.findRoleByName(roleName)).thenReturn(
				new RoleAndMetadata(roleName, true, true, true));
		permissionController.deleteRole(roleName);
		verify(permissionService).deleteRole(new Role(roleName));
		verify(eventEmitter).emitDeleteRole();
	}

	@Test(expected = IllegalArgumentException.class)
	public void deleteUnremovableRole() {
		when(permissionService.findRoleByName(roleName)).thenReturn(
				new RoleAndMetadata(roleName, false, true, true));
		permissionController.deleteRole(roleName);
	}

	@Test
	public void existingPermissions() {
		permissionController.existingPermissions();
	}

	@Test
	public void findAllRolesAndRelatedPermissions() {
		permissionController.findAllRolesAndRelatedPermissions();
		verify(permissionService).findAllRolesAndRelatedPermission();
	}

	@Test
	public void findUserByRole() {
		permissionController.findUserByRole(roleName);
		verify(permissionService).findUserByRole(new Role(roleName));
	}

	@Test
	public void removeRoleToUsers() {
		Users usersToRemove = new Users();
		usersToRemove.setUserIds(Collections.singleton(1));
		permissionController.removeRoleToUsers(roleName, usersToRemove);
		verify(permissionService).removeRoleToUsers(new Role(roleName), usersToRemove.getUserIds());
		verify(eventEmitter).emitRemoveRoleToUsers(roleName);
	}

	@Test
	public void updateRole() {

		when(permissionService.findRoleByName(roleName)).thenReturn(
				new RoleAndMetadata(roleName, false, false, false));

		UpdateRole updateRole = new UpdateRole();
		updateRole.setPermissions(Collections.singleton(Permission.ADMINISTRATION));
		permissionController.updateRole(roleName, updateRole);
		verify(permissionService).updatePermissionsToRole(new Role(roleName), updateRole.getPermissions());
		verify(eventEmitter).emitUpdatePermissionsToRole();
	}

	@Test(expected = IllegalArgumentException.class)
	public void updateReadOnlyRole() {

		when(permissionService.findRoleByName(roleName)).thenReturn(
				new RoleAndMetadata(roleName, false, true, true));

		UpdateRole updateRole = new UpdateRole();
		updateRole.setPermissions(Collections.singleton(Permission.ADMINISTRATION));
		permissionController.updateRole(roleName, updateRole);
	}
}
