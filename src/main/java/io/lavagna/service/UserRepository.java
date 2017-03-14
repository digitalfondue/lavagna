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

import io.lavagna.common.Json;
import io.lavagna.model.*;
import io.lavagna.model.util.CalendarTokenNotFoundException;
import io.lavagna.query.UserQuery;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.trimToNull;

/**
 * CRUD operation over {@link User}
 */
@Repository
@Transactional(readOnly = true)
public class UserRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private final UserQuery queries;


    public UserRepository(NamedParameterJdbcTemplate jdbc, UserQuery queries) {
        this.jdbc = jdbc;
        this.queries = queries;
    }

    public User findUserByName(String provider, String name) {
        return queries.findUserByName(provider, name);
    }

    public User findById(int id) {
        return queries.findUserById(id);
    }

    public List<User> findByIds(Collection<Integer> ids) {
        return ids.isEmpty() ? Collections.<User>emptyList() : queries.findByIds(ids);
    }

    public boolean userExistsAndEnabled(String provider, String name) {
        return !Integer.valueOf(0).equals(queries.userExistsAndEnabled(provider, name, true));
    }

    public boolean userExists(String provider, String name) {
        return !Integer.valueOf(0).equals(queries.userExistsAndEnabled(provider, name));
    }

    public List<User> findUsers(String criteria) {
        return queries.findUsers(criteria);
    }

    /**
     * Find users that have access to specific project
     *
     * @param criteria
     * @param projectId
     * @param permission
     * @return
     */
    public List<User> findUsers(String criteria, int projectId, Permission permission) {
        return queries.findUsers(criteria, projectId, permission.toString());//
    }

    @Transactional(readOnly = false)
    public void createUsers(Collection<User> users) {
        List<SqlParameterSource> params = new ArrayList<>(users.size());
        for (User user : users) {
            params.add(prepareUserParameterSource(user));
        }
        jdbc.batchUpdate(queries.createUserFull(), params.toArray(new SqlParameterSource[params.size()]));
    }

    private static SqlParameterSource prepareUserParameterSource(User user) {
        return new MapSqlParameterSource("provider", trimToNull(user.getProvider()))
            .addValue("userName", trimToNull(user.getUsername())).addValue("email", trimToNull(user.getEmail()))
            .addValue("displayName", trimToNull(user.getDisplayName())).addValue("enabled", user.getEnabled())
            .addValue("emailNotification", user.getEmailNotification())
            .addValue("memberSince", ObjectUtils.firstNonNull(user.getMemberSince(), new Date()))
            .addValue("skipOwnNotifications", user.getSkipOwnNotifications())
            .addValue("metadata", user.getUserMetadataRaw());
    }

    @Transactional(readOnly = false)
    public int createUser(String provider, String userName, String password, String email, String displayName, boolean enabled) {

        return queries.createUser(provider, userName, password, email, displayName, enabled);
    }

    @Transactional(readOnly = false)
    public int updateProfile(User user, String email, String displayName, boolean emailNotification, boolean skipOwnNotifications) {
        return queries.updateProfile(trimToNull(email), trimToNull(displayName), emailNotification, skipOwnNotifications,
            user.getId());
    }

    @Transactional(readOnly = false)
    public int updateMetadata(int userId, UserMetadata metadata) {
        return queries.updateMetadata(Json.GSON.toJson(metadata), userId);
    }

    @Transactional(readOnly = false)
    public int toggle(int userId, boolean enabled) {
        return queries.toggle(enabled, userId);
    }

    public List<User> findAll() {
        return queries.findAll();
    }

    public Map<String, Integer> findUsersId(List<String> users) {

        List<String[]> usersToFind = new ArrayList<>(users.size());
        for (String user : users) {
            String[] splittedString = StringUtils.split(user, ':');
            if (splittedString.length > 1) {
                String provider = splittedString[0];
                String username = StringUtils.join(ArrayUtils.subarray(splittedString, 1, splittedString.length), ':');
                usersToFind.add(new String[] { provider, username });
            }
        }

        if (usersToFind.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<String, Integer> res = new HashMap<>();
        MapSqlParameterSource param = new MapSqlParameterSource("users", usersToFind);

        jdbc.query(queries.findUsersId(), param, new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                res.put(rs.getString("PROVIDER_USER"), rs.getInt("USER_ID"));
            }
        });

        return res;
    }

    @Transactional(readOnly = false)
    public String createRememberMeToken(int userId) {
        String token = UUID.randomUUID().toString();// <- this use secure random
        String hashedToken = DigestUtils.sha256Hex(token);

        queries.registerRememberMeToken(hashedToken, userId, new Date());

        return token;
    }

    @Transactional(readOnly = false)
    public void deleteRememberMeToken(int userId, String token) {
        queries.deleteToken(DigestUtils.sha256Hex(token), userId);
    }

    public boolean rememberMeTokenExists(int userId, String token) {
        String hashedToken = DigestUtils.sha256Hex(token);
        return queries.tokenExists(hashedToken, userId).equals(1);
    }

    @Transactional(readOnly = false)
    public void clearAllTokens(User user) {
        queries.deleteAllTokensForUserId(user.getId());
    }

    public CalendarInfo findCalendarInfoFromUserId(User user) throws CalendarTokenNotFoundException {
        try {
            return queries.findCalendarInfoFromUserId(user.getId());
        } catch (EmptyResultDataAccessException ex) {
            throw new CalendarTokenNotFoundException();
        }
    }

    public int findUserIdFromCalendarToken(String token) {
        return queries.findUserIdFromCalendarToken(token);
    }

    @Transactional(readOnly = false)
    public int registerCalendarToken(User user, String token) {
        return queries.registerCalendarToken(user.getId(), token);
    }

    @Transactional(readOnly = false)
    public int deleteCalendarToken(User user) {
        return queries.deleteCalendarToken(user.getId());
    }

    @Transactional(readOnly = false)
    public int setCalendarFeedDisabled(User user, boolean isDisabled) {
        return queries.setCalendarFeedDisabled(user.getId(), isDisabled);
    }

    public boolean isCalendarFeedDisabled(User user) {
        return queries.isCalendarFeedDisabled(user.getId());
    }

    public String getHashedPassword(String provider, String username) {
        return queries.getHashedPassword(provider, username);
    }

    @Transactional(readOnly = false)
    public int setUserPassword(int userId, String hashedPassword) {
        return queries.setPassword(userId, hashedPassword);
    }

    public Map<String, String> findUsersWithPasswords() {
        Map<String, String> userWithPassword = new HashMap<>();
        for(UserWithPassword uwp : queries.findUsersWithPasswords()) {
            userWithPassword.put(uwp.getUsername(), uwp.getPassword());
        }
        return userWithPassword;
    }
}
