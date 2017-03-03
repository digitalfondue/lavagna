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
package io.lavagna.service;

import io.lavagna.config.PersistenceAndServiceConfig;
import io.lavagna.model.*;
import io.lavagna.service.PermissionService.RoleAndPermissions;
import io.lavagna.service.config.TestServiceConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestServiceConfig.class, PersistenceAndServiceConfig.class })
@Transactional
public class PermissionServiceTest {

    private static final Role ROLE = new Role("TEST-ROLE");
    private static final Role ROLE_2 = new Role("TEST-ROLE-2");

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private Project project;
    private Project project2;

    private Set<Integer> usersId;
    private List<User> users;

    @Before
    public void prepareUsers() {
        Helper.createUser(userRepository, "test", "TEST-USER-1");
        Helper.createUser(userRepository, "test", "TEST-USER-2");
        user1 = userRepository.findUserByName("test", "TEST-USER-1");
        user2 = userRepository.findUserByName("test", "TEST-USER-2");
        usersId = new HashSet<>(Arrays.asList(user1.getId(), user2.getId()));
        users = Arrays.asList(user1, user2);

        projectService.create("test-project-test", "TEST-PRJ", "desc");
        project = projectService.findByShortName("TEST-PRJ");

        projectService.create("test-project-test-2", "TEST-PR2", "desc");
        project2 = projectService.findByShortName("TEST-PR2");
    }

    @Test
    public void testCreateRole() {
        Map<String, RoleAndPermissions> l1 = permissionService.findAllRolesAndRelatedPermission();
        assertTrue(!l1.containsKey(ROLE.getName()));
        permissionService.createRole(ROLE);

        Map<String, RoleAndPermissions> l2 = permissionService.findAllRolesAndRelatedPermission();
        assertTrue(l2.containsKey(ROLE.getName()));
    }

    @Test
    public void findByName() {
        permissionService.createRole(new Role("TEST-DERP-DERP"));
        assertNotNull(permissionService.findRoleByName("TEST-DERP-DERP"));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void findByNameNotFound() {
        permissionService.findRoleByName("TEST-DERP-DERP");
    }

    @Test
    public void findByNameInProject() {
        permissionService.createRoleInProjectId(new Role("TEST-DERP-DERP"), project.getId());
        assertNotNull(permissionService.findRoleInProjectByName(project.getId(), "TEST-DERP-DERP"));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void findByNameNotFoundInProject() {
        permissionService.findRoleInProjectByName(project.getId(), "TEST-DERP-DERP");
    }

    @Test
    public void testCreateRoleForProject() {
        Map<String, RoleAndPermissions> l1 = permissionService.findAllRolesAndRelatedPermissionInProjectId(project
            .getId());
        assertTrue(!l1.containsKey(ROLE.getName()));
        permissionService.createRoleInProjectId(ROLE, project.getId());

        Map<String, RoleAndPermissions> l2 = permissionService.findAllRolesAndRelatedPermissionInProjectId(project
            .getId());
        assertTrue(l2.containsKey(ROLE.getName()));
    }

    @Test(expected = DuplicateKeyException.class)
    public void testCreateDuplicateRole() {
        permissionService.createRole(ROLE);
        permissionService.createRole(ROLE);
    }

    @Test(expected = DuplicateKeyException.class)
    public void testCreateDuplicateRoleForProject() {
        permissionService.createRoleInProjectId(ROLE, project.getId());
        permissionService.createRoleInProjectId(ROLE, project.getId());
    }

    @Test
    public void testDeleteRole() {
        permissionService.createRole(ROLE);
        Map<String, RoleAndPermissions> l1 = permissionService.findAllRolesAndRelatedPermission();
        assertTrue(l1.containsKey(ROLE.getName()));

        permissionService.deleteRole(ROLE);

        Map<String, RoleAndPermissions> l2 = permissionService.findAllRolesAndRelatedPermission();
        assertTrue(!l2.containsKey(ROLE.getName()));
    }

    @Test
    public void testDeleteRoleInProject() {
        permissionService.createRoleInProjectId(ROLE, project.getId());
        Map<String, RoleAndPermissions> l1 = permissionService.findAllRolesAndRelatedPermissionInProjectId(project
            .getId());
        assertTrue(l1.containsKey(ROLE.getName()));

        permissionService.deleteRoleInProjectId(ROLE, project.getId());

        Map<String, RoleAndPermissions> l2 = permissionService.findAllRolesAndRelatedPermissionInProjectId(project
            .getId());
        assertTrue(!l2.containsKey(ROLE.getName()));
    }

    @Test
    public void testDeleteNotExistingRole() {
        assertEquals(0, permissionService.deleteRole(ROLE));
    }

    @Test
    public void testDeleteNotExistingRoleInProject() {
        assertEquals(0, permissionService.deleteRoleInProjectId(ROLE, project.getId()));
    }

    @Test
    public void testAddPermissionsToRole() {
        Role role = ROLE;
        permissionService.createRole(role);
        assertTrue(permissionsForRole(permissionService.findAllRolesAndRelatedPermission(), ROLE).isEmpty());

        Set<Permission> perms = EnumSet
            .of(Permission.ADMINISTRATION, Permission.PROJECT_ADMINISTRATION, Permission.CREATE_COLUMN);
        permissionService.updatePermissionsToRole(role, perms);
        assertEquals(perms, permissionsForRole(permissionService.findAllRolesAndRelatedPermission(), ROLE));

        Set<Permission> permsUpdated = EnumSet.of(Permission.RENAME_COLUMN);

        permissionService.updatePermissionsToRole(role, permsUpdated);

        assertEquals(permsUpdated, permissionsForRole(permissionService.findAllRolesAndRelatedPermission(), ROLE));
    }

    @Test
    public void testAddPermissionsToRoleInProject() {
        Role role = ROLE;
        permissionService.createRoleInProjectId(role, project.getId());
        assertTrue(permissionsForRole(permissionService.findAllRolesAndRelatedPermissionInProjectId(project.getId()),
            ROLE).isEmpty());

        Set<Permission> perms = EnumSet.of(Permission.PROJECT_ADMINISTRATION, Permission.CREATE_COLUMN);
        permissionService.updatePermissionsToRoleInProjectId(role, perms, project.getId());
        assertEquals(
            perms,
            permissionsForRole(permissionService.findAllRolesAndRelatedPermissionInProjectId(project.getId()), ROLE));

        Set<Permission> permsUpdated = EnumSet.of(Permission.RENAME_COLUMN);

        permissionService.updatePermissionsToRoleInProjectId(role, permsUpdated, project.getId());

        assertEquals(
            permsUpdated,
            permissionsForRole(permissionService.findAllRolesAndRelatedPermissionInProjectId(project.getId()), ROLE));
    }

    @Test
    public void testAddRoleToUser() {
        assertTrue(permissionService.findBaseRoleAndPermissionByUserId(user1.getId()).isEmpty());
        assertTrue(permissionService.findBaseRoleAndPermissionByUserId(user2.getId()).isEmpty());

        permissionService.createRole(ROLE);
        permissionService.assignRoleToUsers(ROLE, usersId);

        assertEquals(1, permissionService.findBaseRoleAndPermissionByUserId(user1.getId()).size());
        assertEquals(1, permissionService.findBaseRoleAndPermissionByUserId(user2.getId()).size());
    }

    @Test
    public void testFindUserRolesByProjectOnGlobal() {
        permissionService.createRole(ROLE);

        PermissionService.ProjectRoleFullHolder roles = permissionService.findUserRolesByProject(user1.getId());

        assertEquals(0, roles.getGlobalRoles().size());

        permissionService.assignRoleToUsers(ROLE, usersId);

        roles = permissionService.findUserRolesByProject(user1.getId());

        assertEquals(1, roles.getGlobalRoles().size());
    }

    @Test
    public void testFindUserRolesByProjectOnProject() {
        permissionService.createRoleInProjectId(ROLE, project.getId());

        PermissionService.ProjectRoleFullHolder roles = permissionService.findUserRolesByProject(user1.getId());

        assertEquals(0, roles.getRolesByProject().size());

        permissionService.assignRoleToUsersInProjectId(ROLE, usersId, project.getId());

        roles = permissionService.findUserRolesByProject(user1.getId());

        assertEquals(1, roles.getRolesByProject().size());
        assertEquals(1, roles.getRolesByProject().get(project.getName()).size());
    }

    @Test
    public void testAddRoleToUserInProject() {
        assertTrue(
            permissionService.findRoleAndPermissionByUserIdInProjectId(user1.getId(), project.getId()).isEmpty());
        assertTrue(
            permissionService.findRoleAndPermissionByUserIdInProjectId(user2.getId(), project.getId()).isEmpty());

        permissionService.createRoleInProjectId(ROLE, project.getId());
        permissionService.assignRoleToUsersInProjectId(ROLE, usersId, project.getId());

        assertEquals(1, permissionService.findRoleAndPermissionByUserIdInProjectId(user1.getId(), project.getId())
            .size());
        assertEquals(1, permissionService.findRoleAndPermissionByUserIdInProjectId(user2.getId(), project.getId())
            .size());
    }

    @Test(expected = DuplicateKeyException.class)
    public void testAddDuplicateRoleToUser() {
        permissionService.createRole(ROLE);
        permissionService.assignRoleToUsers(ROLE, usersId);
        permissionService.assignRoleToUsers(ROLE, usersId);
    }

    @Test(expected = DuplicateKeyException.class)
    public void testAddDuplicateRoleToUserInProject() {
        permissionService.createRoleInProjectId(ROLE, project.getId());
        permissionService.assignRoleToUsersInProjectId(ROLE, usersId, project.getId());
        permissionService.assignRoleToUsersInProjectId(ROLE, usersId, project.getId());
    }

    @Test
    public void testRemoveRoleToUser() {
        permissionService.createRole(ROLE);
        permissionService.assignRoleToUsers(ROLE, usersId);

        assertEquals(1, permissionService.findBaseRoleAndPermissionByUserId(user1.getId()).size());
        assertEquals(1, permissionService.findBaseRoleAndPermissionByUserId(user2.getId()).size());

        permissionService.removeRoleToUsers(ROLE, usersId);

        assertTrue(permissionService.findBaseRoleAndPermissionByUserId(user1.getId()).isEmpty());
        assertTrue(permissionService.findBaseRoleAndPermissionByUserId(user2.getId()).isEmpty());

    }

    @Test
    public void testRemoveRoleToUserInProject() {
        permissionService.createRoleInProjectId(ROLE, project.getId());
        permissionService.assignRoleToUsersInProjectId(ROLE, usersId, project.getId());

        assertEquals(1, permissionService.findRoleAndPermissionByUserIdInProjectId(user1.getId(), project.getId())
            .size());
        assertEquals(1, permissionService.findRoleAndPermissionByUserIdInProjectId(user2.getId(), project.getId())
            .size());

        permissionService.removeRoleToUsersInProjectId(ROLE, usersId, project.getId());

        assertTrue(
            permissionService.findRoleAndPermissionByUserIdInProjectId(user1.getId(), project.getId()).isEmpty());
        assertTrue(
            permissionService.findRoleAndPermissionByUserIdInProjectId(user2.getId(), project.getId()).isEmpty());

    }

    @Test
    public void testFindBasePermissionByUsername() {
        assertTrue(permissionService.findBasePermissionByUserId(user1.getId()).isEmpty());
        permissionService.createRole(ROLE);
        permissionService.assignRoleToUsers(ROLE, usersId);

        Set<Permission> perms = EnumSet
            .of(Permission.ADMINISTRATION, Permission.PROJECT_ADMINISTRATION, Permission.CREATE_COLUMN);
        permissionService.updatePermissionsToRole(ROLE, perms);

        assertEquals(perms, permissionService.findBasePermissionByUserId(user1.getId()));
    }

    @Test
    public void testFindPermissionByUsernameInProject() {
        assertTrue(permissionService.findPermissionByUsernameInProjectId(user1.getId(), project.getId()).isEmpty());
        permissionService.createRoleInProjectId(ROLE, project.getId());
        permissionService.assignRoleToUsersInProjectId(ROLE, usersId, project.getId());

        Set<Permission> perms = EnumSet.of(Permission.PROJECT_ADMINISTRATION, Permission.CREATE_COLUMN);
        permissionService.updatePermissionsToRoleInProjectId(ROLE, perms, project.getId());

        assertEquals(perms, permissionService.findPermissionByUsernameInProjectId(user1.getId(), project.getId()));
    }

    @Test
    public void testFindUsersByRole() {
        permissionService.createRole(ROLE);
        assertTrue(permissionService.findUserByRole(ROLE).isEmpty());
        permissionService.assignRoleToUsers(ROLE, usersId);
        assertEquals(users, permissionService.findUserByRole(ROLE));
    }

    @Test
    public void testFindUsersByRoleInProject() {
        permissionService.createRoleInProjectId(ROLE, project.getId());
        permissionService.createRoleInProjectId(ROLE_2, project.getId());
        assertTrue(permissionService.findUserByRoleAndProjectId(ROLE, project.getId()).isEmpty());
        permissionService.assignRoleToUsersInProjectId(ROLE, usersId, project.getId());
        permissionService.assignRoleToUsersInProjectId(ROLE_2, usersId, project.getId());
        assertEquals(users, permissionService.findUserByRoleAndProjectId(ROLE, project.getId()));
    }

    @Test
    public void testPermissionsGroupedByProjectIdForUsername() {
        assertTrue(permissionService.findPermissionsGroupedByProjectForUserId(user1.getId()).getPermissionsByProject()
            .isEmpty());

        permissionService.createRoleInProjectId(ROLE, project.getId());
        permissionService.assignRoleToUsersInProjectId(ROLE, usersId, project.getId());

        permissionService.createRoleInProjectId(ROLE, project2.getId());
        permissionService.assignRoleToUsersInProjectId(ROLE, usersId, project2.getId());

        assertTrue(permissionService.findPermissionsGroupedByProjectForUserId(user1.getId()).getPermissionsByProject()
            .isEmpty());

        Set<Permission> permsProject1 = EnumSet.of(Permission.PROJECT_ADMINISTRATION, Permission.CREATE_COLUMN);
        permissionService.updatePermissionsToRoleInProjectId(ROLE, permsProject1, project.getId());

        assertEquals(1, permissionService.findPermissionsGroupedByProjectForUserId(user1.getId())
            .getPermissionsByProject().size());
        assertEquals(permsProject1, permissionService.findPermissionsGroupedByProjectForUserId(user1.getId())
            .getPermissionsByProject().get(project.getShortName()));

        Set<Permission> permsProject2 = EnumSet.of(Permission.READ);
        permissionService.updatePermissionsToRoleInProjectId(ROLE, permsProject2, project2.getId());
        assertEquals(2, permissionService.findPermissionsGroupedByProjectForUserId(user1.getId())
            .getPermissionsByProject().size());

        assertEquals(permsProject2, permissionService.findPermissionsGroupedByProjectForUserId(user1.getId())
            .getPermissionsByProject().get(project2.getShortName()));

    }

    private static Set<Permission> permissionsForRole(Map<String, RoleAndPermissions> l, Role role) {
        Set<Permission> perms = new HashSet<>();
        if (l.containsKey(role.getName())) {
            for (RoleAndPermission rap : l.get(role.getName()).getRoleAndPermissions()) {
                perms.add(rap.getPermission());
            }
        }
        return perms;
    }
}
