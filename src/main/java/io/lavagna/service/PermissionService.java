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

import io.lavagna.model.*;
import io.lavagna.query.PermissionQuery;
import org.apache.commons.lang3.Validate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Map.Entry;

@Service
@Transactional(readOnly = true)
public class PermissionService {

    private final NamedParameterJdbcTemplate jdbc;
    private final PermissionQuery queries;
    private final UserRepository userRepository;


    public PermissionService(NamedParameterJdbcTemplate jdbc, PermissionQuery queries, UserRepository userRepository) {
        this.jdbc = jdbc;
        this.queries = queries;
        this.userRepository = userRepository;
    }

    /**
     * A role can be without any permission.
     */
    public Map<String, RoleAndPermissions> findBaseRoleAndPermissionByUserId(int userId) {
        return toMap(queries.findBaseRoleAndPermissionByUserId(userId));
    }

    public Map<String, RoleAndPermissions> findRoleAndPermissionByUserIdInProjectId(int userId, int projectId) {
        return toMap(queries.findRoleAndPermissionByUserIdInProjectId(userId, projectId));
    }

    public Set<Permission> findBasePermissionByUserId(int userId) {
        return toSet(queries.findBaseRoleAndPermissionByUserId(userId));
    }

    public RoleAndMetadata findRoleByName(String name) {
        return queries.findRoleByName(name);
    }

    public RoleAndMetadata findRoleInProjectByName(int projectId, String name) {
        return queries.findRoleInProjectIdByName(projectId, name);
    }

    public ProjectRoleAndPermissionFullHolder findPermissionsGroupedByProjectForUserId(int userId) {
        List<ProjectRoleAndPermission> found = queries.findPermissionsGroupedByProjectForUserId(userId);
        Map<String, Set<Permission>> res = new HashMap<>();
        Map<Integer, Set<Permission>> resById = new HashMap<>();
        for (ProjectRoleAndPermission p : found) {
            if (p.getPermission() == null) {
                continue;
            }

            if (!res.containsKey(p.getProjectShortName())) {
                res.put(p.getProjectShortName(), EnumSet.noneOf(Permission.class));
            }
            res.get(p.getProjectShortName()).add(p.getPermission());

            if (!resById.containsKey(p.getProjectId())) {
                resById.put(p.getProjectId(), EnumSet.noneOf(Permission.class));
            }
            resById.get(p.getProjectId()).add(p.getPermission());
        }
        return new ProjectRoleAndPermissionFullHolder(res, resById);
    }

    public ProjectRoleFullHolder findUserRolesByProject(int userId) {

        Set<String> globalRoles = new HashSet<>();
        for (RoleAndMetadata role : queries.findUserRoles(userId)) {
            globalRoles.add(role.getRoleName());
        }

        Map<String, Set<String>> res = new HashMap<>();
        for (RoleAndProject rp : queries.findUserRolesByProject(userId)) {
            if (!res.containsKey(rp.getProjectName())) {
                res.put(rp.getProjectName(), new HashSet<String>());
            }
            res.get(rp.getProjectName()).add(rp.getRoleName());
        }

        return new ProjectRoleFullHolder(globalRoles, res);
    }

    public Set<Permission> findPermissionByUsernameInProjectId(int userId, int projectId) {
        return toSet(queries.findRoleAndPermissionByUserIdInProjectId(userId, projectId));
    }

    public Map<String, RoleAndPermissions> findAllRolesAndRelatedPermission() {
        return toMap(queries.findAllRolesAndRelatedPermission());
    }

    public Map<String, RoleAndPermissionsWithUsers> findAllRolesAndRelatedPermissionWithUsers() {
        Map<String, RoleAndPermissionsWithUsers> res = new TreeMap<>();
        for (RoleAndPermission rap : queries.findAllRolesAndRelatedPermission()) {
            if (!res.containsKey(rap.getRoleName())) {
                res.put(rap.getRoleName(),
                    new RoleAndPermissionsWithUsers(rap, queries.findUserIdentifierByRole(rap.getRoleName())));
            }
            if (rap.getPermission() != null) {
                res.get(rap.getRoleName()).getRoleAndPermissions().add(rap);
            }
        }
        return res;
    }

    public Map<String, RoleAndPermissionsWithUsers> findAllRolesAndRelatedPermissionWithUsersInProjectId(
        int projectId) {
        Map<String, RoleAndPermissionsWithUsers> res = new TreeMap<>();
        for (RoleAndPermission rap : queries.findAllRolesAndRelatedPermissionInProjectId(projectId)) {
            if (!res.containsKey(rap.getRoleName())) {
                res.put(rap.getRoleName(),
                    new RoleAndPermissionsWithUsers(rap, queries.findUserIdentifierByRoleAndProjectId(
                        rap.getRoleName(), projectId)));
            }
            if (rap.getPermission() != null) {
                res.get(rap.getRoleName()).getRoleAndPermissions().add(rap);
            }
        }
        return res;
    }

    public Map<String, RoleAndPermissions> findAllRolesAndRelatedPermissionInProjectId(int projectId) {
        return toMap(queries.findAllRolesAndRelatedPermissionInProjectId(projectId));
    }

    /**
     * Return 1 if created
     *
     * @param role
     * @return
     */
    @Transactional(readOnly = false)
    public int createRole(Role role) {
        return queries.createRole(Objects.requireNonNull(role).getName());
    }

    @Transactional(readOnly = false)
    public int createRoleInProjectId(Role role, int projectId) {
        return queries.createRoleInProjectId(Objects.requireNonNull(role).getName(), projectId);
    }

    @Transactional(readOnly = false)
    public int createFullRoleInProjectId(Role role, int projectId, boolean removable, boolean hidden,
        boolean readOnly) {
        return queries.createFullRoleInProjectId(Objects.requireNonNull(role).getName(), projectId, removable, hidden,
            readOnly);
    }

    @Transactional(readOnly = false)
    public void createMissingRolesWithPermissions(Map<RoleAndPermission, Set<Permission>> rolesWithPermissions) {

        Set<String> currentRoles = findAllRolesAndRelatedPermission().keySet();

        for (Entry<RoleAndPermission, Set<Permission>> kv : rolesWithPermissions.entrySet()) {
            RoleAndPermission rp = kv.getKey();
            if (!currentRoles.contains(rp.getRoleName())) {
                queries.createFullRole(rp.getRoleName(), rp.getRemovable(), rp.getHidden(), rp.getHidden());
            }
            updatePermissionsToRole(new Role(rp.getRoleName()), kv.getValue());
        }
    }

    @Transactional(readOnly = false)
    public void createMissingRolesWithPermissionForProject(int projectId, Map<RoleAndPermission, Set<Permission>> p) {
        createMissingRolesWithPermissionForProjects(Collections.singletonMap(projectId, p));
    }

    @Transactional(readOnly = false)
    public void createMissingRolesWithPermissionForProjects(Map<Integer, Map<RoleAndPermission, Set<Permission>>> r) {
        for (Entry<Integer, Map<RoleAndPermission, Set<Permission>>> projIdToRolesAndPermissions : r.entrySet()) {

            int projectId = projIdToRolesAndPermissions.getKey();

            Set<String> currentRoles = findAllRolesAndRelatedPermissionInProjectId(projectId).keySet();

            for (Entry<RoleAndPermission, Set<Permission>> kv : projIdToRolesAndPermissions.getValue().entrySet()) {
                RoleAndPermission rp = kv.getKey();
                if (!currentRoles.contains(rp.getRoleName())) {
                    createFullRoleInProjectId(new Role(rp.getRoleName()), projectId, rp.getRemovable(), rp.getHidden(),
                        rp.getReadOnly());
                }
                updatePermissionsToRoleInProjectId(new Role(rp.getRoleName()), kv.getValue(), projectId);
            }
        }
    }

    /**
     * Return 1 if deleted
     *
     * @param role
     * @return
     */
    @Transactional(readOnly = false)
    public int deleteRole(Role role) {
        Objects.requireNonNull(role);
        queries.removeUsersFromRole(role.getName());
        queries.deletePermissions(role.getName());
        return queries.deleteRole(role.getName());
    }

    @Transactional(readOnly = false)
    public int deleteRoleInProjectId(Role role, int projectId) {
        Objects.requireNonNull(role);
        queries.removeUsersFromRoleInProjectId(role.getName(), projectId);
        queries.deletePermissionsInProjectId(role.getName(), projectId);
        return queries.deleteRoleInProjectId(role.getName(), projectId);
    }

    @Transactional(readOnly = false)
    public void updatePermissionsToRole(Role role, Set<Permission> enabledPermissions) {
        Objects.requireNonNull(role);
        Objects.requireNonNull(enabledPermissions);

        // step 1: remove all permissions
        queries.deletePermissions(role.getName());
        // step 2: add the enabled permission
        jdbc.batchUpdate(queries.addPermission(), from(role, enabledPermissions));
    }

    @Transactional(readOnly = false)
    public void updatePermissionsToRoleInProjectId(Role role, Set<Permission> permissions, int projectId) {
        Objects.requireNonNull(role);
        Objects.requireNonNull(permissions);
        Permission.Companion.ensurePermissionForProject(permissions);

        // step 1: remove all permissions
        queries.deletePermissionsInProjectId(role.getName(), projectId);
        // step 2: add the enabled permission
        jdbc.batchUpdate(queries.addPermissionInProjectId(), addProjectId(from(role, permissions), projectId));
    }

    private void checkRoleCondition(String roleName, Set<Integer> usersId) {
        if ("ANONYMOUS".equals(roleName) && !usersId.isEmpty()) {
            Validate.isTrue(usersId.size() == 1);
            Validate.isTrue(userRepository.findById(usersId.iterator().next()).getAnonymous());
        }
    }

    @Transactional(readOnly = false)
    public void assignRolesToUsers(Map<Role, Set<Integer>> rolesToUsersId) {
        for (Entry<Role, Set<Integer>> roleToUsersId : rolesToUsersId.entrySet()) {
            assignRoleToUsers(roleToUsersId.getKey(), roleToUsersId.getValue());
        }
    }

    @Transactional(readOnly = false)
    public void assignRoleToUsers(Role role, Set<Integer> userIds) {
        Objects.requireNonNull(role);
        Objects.requireNonNull(userIds);

        checkRoleCondition(role.getName(), userIds);

        jdbc.batchUpdate(queries.assignRoleToUser(), fromUserIdAndRoleName(role, userIds));
    }

    @Transactional(readOnly = false)
    public void assignRoleToUsersInProjectId(Role role, Set<Integer> userIds, int projectId) {
        Objects.requireNonNull(role);
        Objects.requireNonNull(userIds);

        checkRoleCondition(role.getName(), userIds);

        jdbc.batchUpdate(queries.assignRoleToUsersInProjectId(),
            addProjectId(fromUserIdAndRoleName(role, userIds), projectId));
    }

    @Transactional(readOnly = false)
    public void removeRoleToUsers(Role role, Set<Integer> userIds) {
        Objects.requireNonNull(role);
        Objects.requireNonNull(userIds);

        checkRoleCondition(role.getName(), userIds);

        jdbc.batchUpdate(queries.removeRoleToUsers(), fromUserIdAndRoleName(role, userIds));
    }

    @Transactional(readOnly = false)
    public void removeRoleToUsersInProjectId(Role role, Set<Integer> userIds, int projectId) {
        Objects.requireNonNull(role);
        Objects.requireNonNull(userIds);

        checkRoleCondition(role.getName(), userIds);

        jdbc.batchUpdate(queries.removeRoleToUsersInProjectId(),
            addProjectId(fromUserIdAndRoleName(role, userIds), projectId));
    }

    public List<User> findUserByRole(Role role) {
        Objects.requireNonNull(role);
        return queries.findUserByRole(role.getName());
    }

    public List<User> findUserByRoleAndProjectId(Role role, int projectId) {
        Objects.requireNonNull(role);
        return queries.findUserByRoleAndProjectId(role.getName(), projectId);
    }

    private static Map<String, RoleAndPermissions> toMap(List<RoleAndPermission> l) {
        Map<String, RoleAndPermissions> res = new TreeMap<>();
        for (RoleAndPermission rap : l) {
            if (!res.containsKey(rap.getRoleName())) {
                res.put(rap.getRoleName(), new RoleAndPermissions(rap));
            }
            if (rap.getPermission() != null) {
                res.get(rap.getRoleName()).roleAndPermissions.add(rap);
            }
        }
        return res;
    }

    private static Set<Permission> toSet(List<RoleAndPermission> rp) {
        Set<Permission> permissions = EnumSet.noneOf(Permission.class);
        for (RoleAndPermission rap : rp) {
            if (rap.getPermission() != null) {
                permissions.add(rap.getPermission());
            }
        }
        return permissions;
    }

    private static MapSqlParameterSource[] fromUserIdAndRoleName(Role role, Set<Integer> userIds) {
        List<MapSqlParameterSource> ret = new ArrayList<>(userIds.size());
        for (Integer userId : userIds) {
            ret.add(new MapSqlParameterSource("userId", userId).addValue("roleName", role.getName()));
        }

        return ret.toArray(new MapSqlParameterSource[ret.size()]);
    }

    private static MapSqlParameterSource[] addProjectId(MapSqlParameterSource[] s, int projectId) {
        for (MapSqlParameterSource param : s) {
            param.addValue("projectId", projectId);
        }
        return s;
    }

    private static MapSqlParameterSource[] from(Role role, Set<Permission> l) {
        List<MapSqlParameterSource> ret = new ArrayList<>(l.size());

        for (Permission p : l) {
            ret.add(new MapSqlParameterSource("permission", p.toString()).addValue("roleName", role.getName()));
        }

        return ret.toArray(new MapSqlParameterSource[ret.size()]);
    }

    public static class RoleAndPermissions {
        private final String name;
        private final boolean removable;
        private final boolean hidden;
        private final boolean readOnly;
        private final List<RoleAndPermission> roleAndPermissions = new ArrayList<>();

        private RoleAndPermissions(RoleAndPermission base) {
            this.name = base.getRoleName();
            this.removable = base.getRemovable();
            this.hidden = base.getHidden();
            this.readOnly = base.getReadOnly();
        }

        public String getName() {
            return this.name;
        }

        public boolean isRemovable() {
            return this.removable;
        }

        public boolean isHidden() {
            return this.hidden;
        }

        public boolean isReadOnly() {
            return this.readOnly;
        }

        public List<RoleAndPermission> getRoleAndPermissions() {
            return this.roleAndPermissions;
        }
    }

    public static class RoleAndPermissionsWithUsers extends RoleAndPermissions {

        private final List<UserIdentifier> assignedUsers;

        public RoleAndPermissionsWithUsers(RoleAndPermission base, List<UserIdentifier> assignedUsers) {
            super(base);
            this.assignedUsers = assignedUsers;
        }

        public List<UserIdentifier> getAssignedUsers() {
            return this.assignedUsers;
        }
    }

    public static class ProjectRoleAndPermissionFullHolder {
        private final Map<String, Set<Permission>> permissionsByProject;
        private final Map<Integer, Set<Permission>> permissionsByProjectId;

        @java.beans.ConstructorProperties({ "permissionsByProject",
            "permissionsByProjectId" }) public ProjectRoleAndPermissionFullHolder(
            Map<String, Set<Permission>> permissionsByProject,
            Map<Integer, Set<Permission>> permissionsByProjectId) {
            this.permissionsByProject = permissionsByProject;
            this.permissionsByProjectId = permissionsByProjectId;
        }

        public Map<String, Set<Permission>> getPermissionsByProject() {
            return this.permissionsByProject;
        }

        public Map<Integer, Set<Permission>> getPermissionsByProjectId() {
            return this.permissionsByProjectId;
        }
    }

    public static class ProjectRoleFullHolder {
        private final Set<String> globalRoles;
        private final Map<String, Set<String>> rolesByProject;

        @java.beans.ConstructorProperties({ "globalRoles", "rolesByProject" }) public ProjectRoleFullHolder(
            Set<String> globalRoles, Map<String, Set<String>> rolesByProject) {
            this.globalRoles = globalRoles;
            this.rolesByProject = rolesByProject;
        }

        public Set<String> getGlobalRoles() {
            return this.globalRoles;
        }

        public Map<String, Set<String>> getRolesByProject() {
            return this.rolesByProject;
        }
    }
}
