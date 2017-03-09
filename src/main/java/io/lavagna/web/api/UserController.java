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

import com.lambdaworks.crypto.SCryptUtil;
import io.lavagna.model.*;
import io.lavagna.service.*;
import io.lavagna.web.api.model.DisplayNameEmail;
import io.lavagna.web.api.model.UserPublicProfile;
import io.lavagna.web.helper.ExpectPermission;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final EventEmitter eventEmitter;
    private final EventRepository eventRepository;
    private final ProjectService projectService;


    public UserController(UserRepository userRepository, UserService userService, EventEmitter eventEmitter, EventRepository eventRepository,
        ProjectService projectService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.eventEmitter = eventEmitter;
        this.eventRepository = eventRepository;
        this.projectService = projectService;
    }

    @RequestMapping(value = "/api/self", method = RequestMethod.GET)
    // user is resolved through UserArgumentResolver
    public UserWithPermission userProfile(UserWithPermission user) {
        return user;
    }

    @RequestMapping(value = "/api/self/clear-all-tokens", method = RequestMethod.POST)
    public void clearAllTokens(UserWithPermission currentUser) {
        userRepository.clearAllTokens(currentUser);
    }

    @ExpectPermission(Permission.UPDATE_PROFILE)
    @RequestMapping(value = "/api/self/metadata", method = RequestMethod.POST)
    public int updateMetadata(UserWithPermission user, @RequestBody UserMetadata metadata) {
        int result = userRepository.updateMetadata(user.getId(), metadata);
        eventEmitter.emitUpdateUserProfile(user.getId());
        return result;
    }

    @ExpectPermission(Permission.UPDATE_PROFILE)
    @RequestMapping(value = "/api/self/password", method = RequestMethod.POST)
    public int changePassword(UserWithPermission user, @RequestBody PasswordChange passwordChange) {
        String currentHashedPassword = userRepository.getHashedPassword(user.getProvider(), user.getUsername());

        Validate.isTrue(SCryptUtil.check(passwordChange.getCurrentPassword(), currentHashedPassword));

        return userService.changePassword(user.getId(), passwordChange.getNewPassword());
    }

    @ExpectPermission(Permission.UPDATE_PROFILE)
    @RequestMapping(value = "/api/self", method = RequestMethod.POST)
    public int updateUserProfile(UserWithPermission user, @RequestBody DisplayNameEmail toUpdate) {
        int result = userRepository.updateProfile(user, toUpdate.getEmail(), toUpdate.getDisplayName(),
            toUpdate.getEmailNotification(), toUpdate.getSkipOwnNotifications());
        eventEmitter.emitUpdateUserProfile(user.getId());
        return result;
    }

    @RequestMapping(value = "/api/user/{userId}", method = RequestMethod.GET)
    public User getUser(@PathVariable("userId") int userId) {
        return userRepository.findById(userId);
    }

    @RequestMapping(value = "/api/user/bulk", method = RequestMethod.GET)
    public Map<Integer, User> getUsers(@RequestParam("ids") List<Integer> userIds) {
        Map<Integer, User> found = new HashMap<>();
        for (User u : userRepository.findByIds(userIds)) {
            found.put(u.getId(), u);
        }
        return found;
    }

    @RequestMapping(value = "/api/user/activity/{provider}/{name}", method = RequestMethod.GET)
    public List<Event> getUserActivity(@PathVariable("provider") String provider,
        @PathVariable("name") String name, UserWithPermission currentUser) {

        User user = userRepository.findUserByName(provider, name);

        Date lastWeek = DateUtils.setMinutes(DateUtils.setHours(DateUtils.addDays(new Date(), -6), 0), 0);
        if (currentUser.getBasePermissions().containsKey(Permission.READ)) {
            return eventRepository.getLatestActivity(user.getId(), lastWeek);
        } else {
            Collection<Integer> visibleProjectsIds = currentUser.projectsIdWithPermission(Permission.READ);
            return eventRepository.getLatestActivityByProjects(user.getId(), lastWeek, visibleProjectsIds);
        }

    }

    @RequestMapping(value = "/api/user/profile/{provider}/{name}", method = RequestMethod.GET)
    public UserPublicProfile getUserProfile(@PathVariable("provider") String provider,
        @PathVariable("name") String name, UserWithPermission currentUser,
        @RequestParam(value = "page", defaultValue = "0") int page) {

        User user = userRepository.findUserByName(provider, name);

        final List<EventsCount> dailyActivity;
        final List<ProjectWithEventCounts> activeProjects;
        final List<Event> activitiesByPage;
        Date lastYear = DateUtils.setDays(DateUtils.addMonths(new Date(), -11), 1);
        if (currentUser.getBasePermissions().containsKey(Permission.READ)) {
            dailyActivity = eventRepository.getUserActivity(user.getId(), lastYear);
            activeProjects = projectService.findProjectsActivityByUser(user.getId());
            activitiesByPage = eventRepository.getLatestActivityByPage(user.getId(), page);
        } else {
            Collection<Integer> visibleProjectsIds = currentUser.projectsIdWithPermission(Permission.READ);

            dailyActivity = eventRepository.getUserActivityForProjects(user.getId(), lastYear, visibleProjectsIds);
            activeProjects = projectService.findProjectsActivityByUserInProjects(user.getId(),
                visibleProjectsIds);
            activitiesByPage = eventRepository.getLatestActivityByPageAndProjects(user.getId(), page,
                visibleProjectsIds);
        }

        return new UserPublicProfile(user, dailyActivity, activeProjects, activitiesByPage);
    }

    @RequestMapping(value = "/api/user/{provider}/{name}", method = RequestMethod.GET)
    public User getUser(@PathVariable("provider") String provider, @PathVariable("name") String name) {
        return userRepository.findUserByName(provider, name);
    }

    @RequestMapping(value = "/api/keep-alive", method = RequestMethod.GET)
    public boolean keepAlive() {
        return true;
    }

    @ExpectPermission(Permission.ADMINISTRATION)
    @RequestMapping(value = "/api/user/list", method = RequestMethod.GET)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @ExpectPermission(Permission.PROJECT_ADMINISTRATION)
    @RequestMapping(value = "/api/project/{projectShortName}/user/list", method = RequestMethod.GET)
    public List<User> findAllUsersForProject() {
        return findAllUsers();
    }

    public static class PasswordChange {
        private String newPassword;
        private String currentPassword;

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }

        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }
    }

}
