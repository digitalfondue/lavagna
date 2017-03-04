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
package io.lavagna.query;

import ch.digitalfondue.npjt.Bind;
import ch.digitalfondue.npjt.Query;
import ch.digitalfondue.npjt.QueryRepository;
import ch.digitalfondue.npjt.QueryType;
import io.lavagna.model.*;

import java.util.List;

@QueryRepository
public interface PermissionQuery {

	@Query("SELECT ROLE_NAME, ROLE_REMOVABLE, ROLE_HIDDEN, ROLE_READONLY, PERMISSION FROM LA_USER_ROLE "
			+ " INNER JOIN LA_ROLE ON LA_USER_ROLE.ROLE_ID_FK = ROLE_ID "
			+ " LEFT JOIN LA_ROLE_PERMISSION ON LA_ROLE_PERMISSION.ROLE_ID_FK = ROLE_ID "
			+ " WHERE USER_ID_FK = :userId")
	List<RoleAndPermission> findBaseRoleAndPermissionByUserId(@Bind("userId") int userId);

	@Query("SELECT PROJECT_ROLE_NAME AS ROLE_NAME, PROJECT_ROLE_REMOVABLE AS ROLE_REMOVABLE, PROJECT_ROLE_HIDDEN AS ROLE_HIDDEN, PROJECT_ROLE_READONLY AS ROLE_READONLY, "
			+ " PERMISSION FROM LA_PROJECT_USER_ROLE "
			+ " INNER JOIN LA_PROJECT_ROLE ON LA_PROJECT_USER_ROLE.PROJECT_ROLE_ID_FK = PROJECT_ROLE_ID "
			+ " LEFT JOIN LA_PROJECT_ROLE_PERMISSION ON LA_PROJECT_ROLE_PERMISSION.PROJECT_ROLE_ID_FK = PROJECT_ROLE_ID "
			+ " WHERE USER_ID_FK = :userId AND LA_PROJECT_USER_ROLE.PROJECT_ID_FK = :projectId AND LA_PROJECT_ROLE.PROJECT_ID_FK = :projectId")
	List<RoleAndPermission> findRoleAndPermissionByUserIdInProjectId(@Bind("userId") int userId, @Bind("projectId") int projectId);

    @Query("SELECT PROJECT_ROLE_NAME AS ROLE_NAME, LA_PROJECT.PROJECT_NAME FROM LA_PROJECT_USER_ROLE "
        + " INNER JOIN LA_PROJECT_ROLE ON LA_PROJECT_USER_ROLE.PROJECT_ROLE_ID_FK = PROJECT_ROLE_ID "
        + " INNER JOIN LA_PROJECT ON LA_PROJECT_ROLE.PROJECT_ID_FK = LA_PROJECT.PROJECT_ID "
        + " WHERE USER_ID_FK = :userId")
    List<RoleAndProject> findUserRolesByProject(@Bind("userId") int userId);

    @Query("SELECT ROLE_NAME, ROLE_REMOVABLE, ROLE_HIDDEN, ROLE_READONLY FROM LA_USER_ROLE "
        + " INNER JOIN LA_ROLE ON LA_USER_ROLE.ROLE_ID_FK = ROLE_ID "
        + " WHERE USER_ID_FK = :userId")
    List<RoleAndMetadata> findUserRoles(@Bind("userId") int userId);

	@Query("SELECT LA_PROJECT.PROJECT_ID AS PROJECT_ID, LA_PROJECT.PROJECT_SHORT_NAME AS PROJECT_SHORT_NAME, PROJECT_ROLE_NAME AS ROLE_NAME, PROJECT_ROLE_REMOVABLE AS ROLE_REMOVABLE, PERMISSION FROM LA_PROJECT_USER_ROLE "
			+ " INNER JOIN LA_PROJECT_ROLE ON LA_PROJECT_USER_ROLE.PROJECT_ROLE_ID_FK = PROJECT_ROLE_ID "
			+ " INNER JOIN LA_PROJECT ON LA_PROJECT_ROLE.PROJECT_ID_FK = LA_PROJECT.PROJECT_ID "
			+ " LEFT JOIN LA_PROJECT_ROLE_PERMISSION ON LA_PROJECT_ROLE_PERMISSION.PROJECT_ROLE_ID_FK = PROJECT_ROLE_ID "
			+ " WHERE USER_ID_FK = :userId")
	List<ProjectRoleAndPermission> findPermissionsGroupedByProjectForUserId(@Bind("userId") int userId);

	@Query("SELECT ROLE_NAME, ROLE_REMOVABLE, ROLE_HIDDEN, ROLE_READONLY, PERMISSION from LA_ROLE_PERMISSION RIGHT JOIN LA_ROLE ON ROLE_ID = ROLE_ID_FK")
	List<RoleAndPermission> findAllRolesAndRelatedPermission();

	@Query("SELECT ROLE_NAME, ROLE_REMOVABLE, ROLE_HIDDEN, ROLE_READONLY FROM LA_ROLE  WHERE ROLE_NAME = :name")
	RoleAndMetadata findRoleByName(@Bind("name") String name);

	@Query("SELECT PROJECT_ROLE_NAME AS ROLE_NAME, PROJECT_ROLE_REMOVABLE AS ROLE_REMOVABLE, PROJECT_ROLE_HIDDEN AS ROLE_HIDDEN, PROJECT_ROLE_READONLY AS ROLE_READONLY,"
			+ " PERMISSION from LA_PROJECT_ROLE_PERMISSION RIGHT JOIN LA_PROJECT_ROLE ON PROJECT_ROLE_ID = PROJECT_ROLE_ID_FK WHERE PROJECT_ID_FK = :projectId")
	List<RoleAndPermission> findAllRolesAndRelatedPermissionInProjectId(@Bind("projectId") int projectId);

	@Query("SELECT PROJECT_ROLE_NAME AS ROLE_NAME, PROJECT_ROLE_REMOVABLE AS ROLE_REMOVABLE, PROJECT_ROLE_HIDDEN AS ROLE_HIDDEN, PROJECT_ROLE_READONLY AS ROLE_READONLY "
			+ " from LA_PROJECT_ROLE WHERE PROJECT_ID_FK = :projectId AND PROJECT_ROLE_NAME = :name")
	RoleAndMetadata findRoleInProjectIdByName(@Bind("projectId") int projectId,
			@Bind("name") String name);

	@Query("INSERT INTO LA_ROLE(ROLE_NAME, ROLE_REMOVABLE) VALUES (:roleName, TRUE)")
	int createRole(@Bind("roleName") String roleName);

	@Query("INSERT INTO LA_ROLE(ROLE_NAME, ROLE_REMOVABLE, ROLE_HIDDEN, ROLE_READONLY) VALUES (:roleName, :removable, :hidden, :readOnly)")
	int createFullRole(@Bind("roleName") String roleName, @Bind("removable") boolean removable,
			@Bind("hidden") boolean hidden, @Bind("readOnly") boolean readOnly);

	@Query("INSERT INTO LA_PROJECT_ROLE(PROJECT_ROLE_NAME, PROJECT_ID_FK) VALUES (:roleName, :projectId)")
	int createRoleInProjectId(@Bind("roleName") String roleName, @Bind("projectId") int projectId);

	@Query("INSERT INTO LA_PROJECT_ROLE(PROJECT_ROLE_NAME, PROJECT_ID_FK, PROJECT_ROLE_REMOVABLE, PROJECT_ROLE_HIDDEN, PROJECT_ROLE_READONLY) "
			+ " VALUES (:roleName, :projectId, :removable, :hidden, :readOnly)")
	int createFullRoleInProjectId(@Bind("roleName") String roleName, @Bind("projectId") int projectId,
			@Bind("removable") boolean removable, @Bind("hidden") boolean hidden, @Bind("readOnly") boolean readOnly);

	@Query("DELETE FROM LA_ROLE WHERE ROLE_NAME = :roleName AND ROLE_REMOVABLE = TRUE")
	int deleteRole(@Bind("roleName") String roleName);

	@Query("DELETE FROM LA_PROJECT_ROLE WHERE PROJECT_ROLE_NAME = :roleName AND PROJECT_ID_FK = :projectId")
	int deleteRoleInProjectId(@Bind("roleName") String roleName, @Bind("projectId") int projectId);

	@Query("DELETE FROM LA_ROLE_PERMISSION WHERE ROLE_ID_FK = (SELECT ROLE_ID FROM LA_ROLE WHERE ROLE_NAME = :roleName)")
	int deletePermissions(@Bind("roleName") String roleName);

	@Query("DELETE FROM LA_PROJECT_ROLE_PERMISSION WHERE PROJECT_ROLE_ID_FK = (SELECT PROJECT_ROLE_ID FROM LA_PROJECT_ROLE WHERE PROJECT_ROLE_NAME = :roleName AND PROJECT_ID_FK = :projectId)")
	int deletePermissionsInProjectId(@Bind("roleName") String roleName, @Bind("projectId") int projectId);

	//
	@Query("DELETE FROM LA_USER_ROLE WHERE ROLE_ID_FK = (SELECT ROLE_ID FROM LA_ROLE WHERE ROLE_NAME = :roleName)")
	int removeUsersFromRole(@Bind("roleName") String roleName);

	@Query("DELETE FROM LA_PROJECT_USER_ROLE WHERE PROJECT_ROLE_ID_FK = (SELECT PROJECT_ROLE_ID FROM LA_PROJECT_ROLE WHERE PROJECT_ROLE_NAME = :roleName AND PROJECT_ID_FK = :projectId)")
	int removeUsersFromRoleInProjectId(@Bind("roleName") String roleName, @Bind("projectId") int projectId);
	//

	@Query(type = QueryType.TEMPLATE, value = "INSERT INTO LA_ROLE_PERMISSION(ROLE_ID_FK, PERMISSION) VALUES ((SELECT ROLE_ID FROM LA_ROLE WHERE ROLE_NAME = :roleName), :permission)")
	String addPermission();

	@Query(type = QueryType.TEMPLATE, value = "INSERT INTO LA_PROJECT_ROLE_PERMISSION(PROJECT_ROLE_ID_FK, PERMISSION) VALUES ((SELECT PROJECT_ROLE_ID FROM LA_PROJECT_ROLE WHERE PROJECT_ROLE_NAME = :roleName AND PROJECT_ID_FK = :projectId), :permission)")
	String addPermissionInProjectId();

	@Query(type = QueryType.TEMPLATE, value = "INSERT INTO LA_USER_ROLE(USER_ID_FK, ROLE_ID_FK) VALUES ((:userId), (SELECT ROLE_ID FROM LA_ROLE WHERE ROLE_NAME = :roleName))")
	String assignRoleToUser();

	@Query(type = QueryType.TEMPLATE, value = "INSERT INTO LA_PROJECT_USER_ROLE(PROJECT_ID_FK, USER_ID_FK, PROJECT_ROLE_ID_FK) VALUES (:projectId, (:userId), (SELECT PROJECT_ROLE_ID FROM LA_PROJECT_ROLE WHERE PROJECT_ROLE_NAME = :roleName AND PROJECT_ID_FK = :projectId))")
	String assignRoleToUsersInProjectId();

	@Query(type = QueryType.TEMPLATE, value = "DELETE FROM LA_USER_ROLE WHERE USER_ID_FK = (:userId) AND ROLE_ID_FK = (SELECT ROLE_ID FROM LA_ROLE WHERE ROLE_NAME = :roleName)")
	String removeRoleToUsers();

	@Query(type = QueryType.TEMPLATE, value = "DELETE FROM LA_PROJECT_USER_ROLE WHERE USER_ID_FK = (:userId) AND PROJECT_ROLE_ID_FK = (SELECT PROJECT_ROLE_ID FROM LA_PROJECT_ROLE WHERE PROJECT_ROLE_NAME = :roleName AND PROJECT_ID_FK = :projectId) AND PROJECT_ID_FK = :projectId")
	String removeRoleToUsersInProjectId();

	@Query("SELECT USER_ID, USER_PROVIDER, USER_NAME, USER_EMAIL, USER_DISPLAY_NAME, USER_ENABLED, USER_EMAIL_NOTIFICATION, USER_MEMBER_SINCE, USER_SKIP_OWN_NOTIFICATIONS, USER_METADATA FROM LA_USER "
			+ " INNER JOIN LA_USER_ROLE ON USER_ID = USER_ID_FK "
			+ " INNER JOIN LA_ROLE ON ROLE_ID_FK = ROLE_ID WHERE ROLE_NAME = :roleName ORDER BY USER_PROVIDER, USER_NAME")
	List<User> findUserByRole(@Bind("roleName") String roleName);

	@Query("SELECT USER_PROVIDER, USER_NAME FROM LA_USER "
			+ " INNER JOIN LA_USER_ROLE ON USER_ID = USER_ID_FK "
			+ " INNER JOIN LA_ROLE ON ROLE_ID_FK = ROLE_ID WHERE ROLE_NAME = :roleName ORDER BY USER_PROVIDER, USER_NAME")
	List<UserIdentifier> findUserIdentifierByRole(@Bind("roleName") String roleName);

	@Query("SELECT USER_ID, USER_PROVIDER, USER_NAME, USER_EMAIL, USER_DISPLAY_NAME, USER_ENABLED, USER_EMAIL_NOTIFICATION, USER_MEMBER_SINCE, USER_SKIP_OWN_NOTIFICATIONS, USER_METADATA FROM LA_USER "
			+ " INNER JOIN LA_PROJECT_USER_ROLE ON USER_ID = USER_ID_FK "
			+ " INNER JOIN LA_PROJECT_ROLE ON PROJECT_ROLE_ID_FK = PROJECT_ROLE_ID "
			+ " WHERE PROJECT_ROLE_NAME = :roleName AND LA_PROJECT_ROLE.PROJECT_ID_FK = :projectId AND "
			+ " LA_PROJECT_USER_ROLE.PROJECT_ID_FK = :projectId ORDER BY USER_PROVIDER, USER_NAME")
	List<User> findUserByRoleAndProjectId(@Bind("roleName") String roleName, @Bind("projectId") int projectId);

	@Query("SELECT USER_PROVIDER, USER_NAME FROM LA_USER "
			+ " INNER JOIN LA_PROJECT_USER_ROLE ON USER_ID = USER_ID_FK "
			+ " INNER JOIN LA_PROJECT_ROLE ON PROJECT_ROLE_ID_FK = PROJECT_ROLE_ID "
			+ " WHERE PROJECT_ROLE_NAME = :roleName AND LA_PROJECT_ROLE.PROJECT_ID_FK = :projectId AND "
			+ " LA_PROJECT_USER_ROLE.PROJECT_ID_FK = :projectId ORDER BY USER_PROVIDER, USER_NAME")
	List<UserIdentifier> findUserIdentifierByRoleAndProjectId(@Bind("roleName") String roleName,
			@Bind("projectId") int projectId);
}
