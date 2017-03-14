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
import io.lavagna.model.CalendarInfo;
import io.lavagna.model.User;
import io.lavagna.model.UserWithPassword;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@QueryRepository
public interface UserQuery {

	@Query("INSERT INTO LA_USER(USER_PROVIDER, USER_NAME, USER_PASSWORD, USER_EMAIL, USER_DISPLAY_NAME, USER_ENABLED) VALUES (:provider, :userName, :password, :email, :displayName, :enabled)")
	int createUser(@Bind("provider") String provider, @Bind("userName") String username, @Bind("password") String password,
                   @Bind("email") String email, @Bind("displayName") String displayName, @Bind("enabled") boolean enabled);

	@Query(type = QueryType.TEMPLATE, value = "INSERT INTO LA_USER(USER_PROVIDER, USER_NAME, USER_EMAIL, USER_DISPLAY_NAME, USER_ENABLED, USER_EMAIL_NOTIFICATION, USER_MEMBER_SINCE, USER_SKIP_OWN_NOTIFICATIONS, USER_METADATA) VALUES "
			+ " (:provider, :userName, :email, :displayName, :enabled, :emailNotification, :memberSince, :skipOwnNotifications, :metadata)")
	String createUserFull();

	@Query("SELECT * FROM LA_USER WHERE USER_NAME = :userName AND USER_PROVIDER = :provider")
	User findUserByName(@Bind("provider") String provider, @Bind("userName") String userName);

	@Query("SELECT * FROM LA_USER WHERE USER_ID = :userId")
	User findUserById(@Bind("userId") int userId);

	@Query("SELECT * FROM LA_USER WHERE USER_ID IN (:userIds)")
	List<User> findByIds(@Bind("userIds") Collection<Integer> userIds);

	@Query("SELECT COUNT(*) FROM LA_USER WHERE USER_NAME = :userName AND USER_PROVIDER = :provider AND (USER_ENABLED IS NULL OR USER_ENABLED = :enabled) ")
	Integer userExistsAndEnabled(@Bind("provider") String provider, @Bind("userName") String userName,
			@Bind("enabled") boolean enabled);

	@Query("SELECT COUNT(*) FROM LA_USER WHERE USER_NAME = :userName AND USER_PROVIDER = :provider")
	Integer userExistsAndEnabled(@Bind("provider") String provider, @Bind("userName") String userName);

	String FIND_USER_COMMON_WHERE = " WHERE LOWER(USER_PROVIDER) <> 'system' AND "
			+ "(LOWER(USER_PROVIDER) LIKE CONCAT('%', LOWER(:criteria),'%') OR LOWER(USER_NAME) LIKE CONCAT('%', LOWER(:criteria),'%') OR LOWER(USER_EMAIL) LIKE CONCAT('%', LOWER(:criteria),'%') "
			+ " OR LOWER(USER_DISPLAY_NAME) LIKE CONCAT('%', LOWER(:criteria),'%') ) ORDER BY USER_PROVIDER, USER_NAME LIMIT 10";

	@Query("SELECT * FROM LA_USER " + FIND_USER_COMMON_WHERE)
	List<User> findUsers(@Bind("criteria") String criteria);

	@Query("SELECT * FROM LA_USER "//
			+ "inner join "//
			+ "(select USER_ID_FK from LA_PROJECT_ROLE_PERMISSION "//
			+ "inner join LA_PROJECT_ROLE on LA_PROJECT_ROLE_PERMISSION.project_role_id_fk = project_role_id "//
			+ "inner join LA_PROJECT_USER_ROLE on LA_PROJECT_USER_ROLE.project_role_id_fk = project_role_id  "//
			+ "WHERE PERMISSION = :permission AND LA_PROJECT_ROLE.PROJECT_ID_FK = :projectId "//
			+ "union "//
			+ "select USER_ID_FK  from LA_ROLE_PERMISSION "//
			+ "inner join LA_ROLE on LA_ROLE_PERMISSION.role_id_fk = role_id "//
			+ "inner join LA_USER_ROLE on LA_USER_ROLE .role_id_fk = role_id "//
			+ "WHERE PERMISSION = :permission) as filtered_users on user_id = user_id_fk "//
			+ FIND_USER_COMMON_WHERE)
	List<User> findUsers(@Bind("criteria") String criteria, @Bind("projectId") int projectId,
			@Bind("permission") String permission);

	@Query("UPDATE LA_USER SET USER_EMAIL = :email, USER_DISPLAY_NAME = :displayName, USER_EMAIL_NOTIFICATION = :emailNotification, USER_SKIP_OWN_NOTIFICATIONS = :skipOwnNotifications WHERE USER_ID = :userId")
	int updateProfile(@Bind("email") String email, @Bind("displayName") String displayName,
			@Bind("emailNotification") boolean emailNotification, @Bind("skipOwnNotifications") boolean skipOwnNotifications, @Bind("userId") int userId);

    @Query("UPDATE LA_USER SET USER_METADATA = :metadata WHERE USER_ID = :userId")
    int updateMetadata(@Bind("metadata") String metadata, @Bind("userId") int userId);

	@Query("SELECT * FROM LA_USER ORDER BY USER_PROVIDER, USER_NAME")
	List<User> findAll();

	@Query("UPDATE LA_USER SET USER_ENABLED = :enabled WHERE USER_ID = :userId")
	int toggle(@Bind("enabled") boolean enabled, @Bind("userId") int userId);

	@Query(type = QueryType.TEMPLATE, value = "SELECT CONCAT(CONCAT(USER_PROVIDER, ':'), USER_NAME) AS PROVIDER_USER, USER_ID FROM LA_USER WHERE (USER_PROVIDER, USER_NAME) IN (:users)")
	String findUsersId();

	@Query("INSERT INTO LA_USER_REMEMBER(USER_REMEMBER_HASHED_TOKEN, USER_REMEMBER_ID_FK, USER_REMEMBER_LAST_USE) VALUES (:hashedToken, :userId, :lastUse)")
	int registerRememberMeToken(@Bind("hashedToken") String hashedToken, @Bind("userId") int userId,
			@Bind("lastUse") Date lastUse);

	@Query("DELETE FROM LA_USER_REMEMBER WHERE USER_REMEMBER_HASHED_TOKEN = :hashedToken AND USER_REMEMBER_ID_FK = :userId")
	int deleteToken(@Bind("hashedToken") String hashedToken, @Bind("userId") int userId);

	@Query("SELECT COUNT(*) FROM LA_USER_REMEMBER WHERE USER_REMEMBER_HASHED_TOKEN = :hashedToken AND USER_REMEMBER_ID_FK = :userId")
	Integer tokenExists(@Bind("hashedToken") String hashedToken, @Bind("userId") int userId);

	@Query("DELETE FROM LA_USER_REMEMBER WHERE USER_REMEMBER_ID_FK = :userId")
	int deleteAllTokensForUserId(@Bind("userId") int id);

	@Query("SELECT USER_CALENDAR_TOKEN, USER_CALENDAR_DISABLE_FEED FROM LA_USER_CALENDAR WHERE USER_CALENDAR_ID_FK = :userId")
    CalendarInfo findCalendarInfoFromUserId(@Bind("userId") int userId);

	@Query("SELECT USER_ID FROM LA_USER AS USR "
			+ "INNER JOIN LA_USER_CALENDAR AS UC ON UC.USER_CALENDAR_ID_FK = USR.USER_ID "
			+ "WHERE USER_CALENDAR_TOKEN = :token")
	Integer findUserIdFromCalendarToken(@Bind("token") String token);

	@Query("INSERT INTO LA_USER_CALENDAR(USER_CALENDAR_ID_FK, USER_CALENDAR_TOKEN) VALUES (:userId, :token)")
	int registerCalendarToken(@Bind("userId") int userId,@Bind("token") String token);

	@Query("DELETE FROM LA_USER_CALENDAR WHERE USER_CALENDAR_ID_FK = :userId")
	int deleteCalendarToken(@Bind("userId") int userId);

    @Query("UPDATE LA_USER_CALENDAR SET USER_CALENDAR_DISABLE_FEED = :disabled WHERE USER_CALENDAR_ID_FK = :userId")
    int setCalendarFeedDisabled(@Bind("userId") int userId, @Bind("disabled") boolean isDisabled);

    @Query("SELECT USER_CALENDAR_DISABLE_FEED FROM LA_USER_CALENDAR WHERE USER_CALENDAR_ID_FK = :userId")
    boolean isCalendarFeedDisabled(@Bind("userId") int userId);

    @Query("SELECT USER_PASSWORD FROM LA_USER WHERE USER_NAME = :userName AND USER_PROVIDER = :provider")
    String getHashedPassword(@Bind("provider") String provider, @Bind("userName") String userName);

    @Query("UPDATE LA_USER SET USER_PASSWORD = :password WHERE USER_ID = :userId")
    int setPassword(@Bind("userId") int userId, @Bind("password") String password);

    @Query("select USER_NAME, USER_PASSWORD from LA_USER where USER_PROVIDER = 'password'")
    List<UserWithPassword> findUsersWithPasswords();
}
