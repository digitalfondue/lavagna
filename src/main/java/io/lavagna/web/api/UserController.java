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
package io.lavagna.web.api;

import io.lavagna.model.Event;
import io.lavagna.model.EventsCount;
import io.lavagna.model.Permission;
import io.lavagna.model.ProjectWithEventCounts;
import io.lavagna.model.User;
import io.lavagna.model.UserWithPermission;
import io.lavagna.service.CalendarService;
import io.lavagna.service.EventEmitter;
import io.lavagna.service.EventRepository;
import io.lavagna.service.ProjectService;
import io.lavagna.service.UserRepository;
import io.lavagna.web.helper.ExpectPermission;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import lombok.Getter;
import lombok.Setter;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

	private final UserRepository userRepository;
	private final EventEmitter eventEmitter;
	private final EventRepository eventRepository;
	private final ProjectService projectService;
	private final CalendarService calendarService;

	@Autowired
	public UserController(UserRepository userRepository, EventEmitter eventEmitter, EventRepository eventRepository,
			ProjectService projectService, CalendarService calendarService) {
		this.userRepository = userRepository;
		this.eventEmitter = eventEmitter;
		this.eventRepository = eventRepository;
		this.projectService = projectService;
		this.calendarService = calendarService;
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

	@RequestMapping(value = "/api/self/feed/{page}", method = RequestMethod.GET)
	// user is resolved through UserArgumentResolver
	public List<Event> userFeed(@PathVariable("page") int page, UserWithPermission user) {
		return eventRepository.getUserFeedByPage(user.getId(), page);
	}

	@ExpectPermission(Permission.UPDATE_PROFILE)
	@RequestMapping(value = "/api/self", method = RequestMethod.POST)
	public int updateUserProfile(UserWithPermission user, @RequestBody DisplayNameEmail toUpdate) {
		int result = userRepository.updateProfile(user, toUpdate.getEmail(), toUpdate.getDisplayName(),
				toUpdate.isEmailNotification());
		eventEmitter.emitUpdateUserProfile(user.getId());
		return result;
	}

	@RequestMapping(value = "/api/user/{userId}", method = RequestMethod.GET)
	public User getUser(@PathVariable("userId") int userId) {
		return userRepository.findById(userId);
	}

	@RequestMapping(value = "/api/user/profile/{provider}/{name}", method = RequestMethod.GET)
	public UserPublicProfile getUserProfile(@PathVariable("provider") String provider,
			@PathVariable("name") String name, UserWithPermission currentUser,
			@RequestParam(value = "page", defaultValue = "0") int page) {
		User user = userRepository.findUserByName(provider, name);

		final List<EventsCount> dailyActivity;
		final List<ProjectWithEventCounts> activeProjects;
		final List<Event> latestActivity;
		Date fromDate = DateUtils.setDays(DateUtils.addMonths(new Date(), -11), 1);
		if (currentUser.getBasePermissions().containsKey(Permission.READ)) {
			dailyActivity = eventRepository.getUserActivity(user.getId(), fromDate);
			activeProjects = projectService.findProjectsActivityByUser(user.getId());
			latestActivity = eventRepository.getLatestActivityByPage(user.getId(), page);
		} else {
			Collection<Integer> visibleProjectsIds = currentUser.projectsIdWithPermission(Permission.READ);

			dailyActivity = eventRepository.getUserActivityForProjects(user.getId(), fromDate, visibleProjectsIds);
			activeProjects = projectService.findProjectsActivityByUserInProjects(user.getId(),
					visibleProjectsIds);
			latestActivity = eventRepository.getLatestActivityByPageAndProjects(user.getId(), page, visibleProjectsIds);
		}

		return new UserPublicProfile(user, dailyActivity, activeProjects, latestActivity);
	}

	@RequestMapping(value = "/api/user/{provider}/{name}", method = RequestMethod.GET)
	public User getUser(@PathVariable("provider") String provider, @PathVariable("name") String name) {
		return userRepository.findUserByName(provider, name);
	}

	@RequestMapping(value = "/api/self/calendar-token", method = RequestMethod.GET)
	public CalendarToken getCalendarToken(UserWithPermission user) {
		CalendarToken ct = new CalendarToken();
		ct.setToken(calendarService.findCalendarTokenFromUser(user));
		return ct;
	}

	@RequestMapping(value = "/api/calendar/{token}/calendar.ics",
			method = RequestMethod.GET, produces = "text/calendar")
	public void userCalendar(@PathVariable("token") String userToken, HttpServletResponse response)
			throws IOException, ValidationException {
		final Calendar calendar = calendarService.getUserCalendar(userToken);
		response.setContentType("text/calendar");
		final CalendarOutputter output = new CalendarOutputter();
		output.output(calendar, response.getOutputStream());
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

	@Getter
	@Setter
	public static class DisplayNameEmail {
		private String email;
		private String displayName;
		private boolean emailNotification;
	}

	@Getter
	public static class UserPublicProfile {
		private final User user;
		private final List<EventsCount> dailyActivity;
		private final List<ProjectWithEventCounts> activeProjects;
		private final List<Event> latestActivity;

		public UserPublicProfile(User user, List<EventsCount> dailyActivity,
				List<ProjectWithEventCounts> activeProjects, List<Event> latestActivity) {
			// we remove the email
			this.user = new User(user.getId(), user.getProvider(), user.getUsername(), null, user.getDisplayName(),
					user.isEnabled(), user.isEmailNotification(), user.getMemberSince());
			this.activeProjects = activeProjects;
			this.dailyActivity = dailyActivity;
			this.latestActivity = latestActivity;
		}
	}

	@Getter
	@Setter
	public static class CalendarToken {
		private String token;
	}
}
