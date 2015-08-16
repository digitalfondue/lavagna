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

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.lavagna.model.Permission;
import io.lavagna.model.User;
import io.lavagna.model.UserWithPermission;
import io.lavagna.service.CalendarService;
import io.lavagna.service.EventEmitter;
import io.lavagna.service.EventRepository;
import io.lavagna.service.ProjectService;
import io.lavagna.service.UserRepository;
import io.lavagna.web.api.UserController.DisplayNameEmail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private EventEmitter eventEmitter;

	@Mock
	private EventRepository eventRepository;

	@Mock
	private UserWithPermission user;

	@Mock
	private ProjectService projectService;

	@Mock
	private CalendarService calendarService;

	@Mock
	private Environment env;

	private UserController userController;

	@Before
	public void prepare() {
		userController = new UserController(userRepository, eventEmitter, eventRepository,
				projectService, calendarService, env);
	}

	@Test
	public void findAllUsers() {
		userController.findAllUsers();
		verify(userRepository).findAll();
	}

	@Test
	public void keepAlive() {
		Assert.assertTrue(userController.keepAlive());
	}

	@Test
	public void updateUserProfile() {
		DisplayNameEmail d = new DisplayNameEmail();
		d.setDisplayName("displayName");
		d.setEmail("email");
		d.setEmailNotification(true);

		userController.updateUserProfile(user, d);

		verify(userRepository).updateProfile(user, "email", "displayName", true);
	}

	@Test
	public void testGetUserById() {
		when(userRepository.findById(user.getId())).thenReturn(user);

		User u = userController.getUser(user.getId());
		Assert.assertEquals(user, u);
	}

	@Test
	public void testGetUserByName() {
		when(userRepository.findUserByName(user.getProvider(), user.getUsername())).thenReturn(user);

		User u = userController.getUser(user.getProvider(), user.getUsername());
		Assert.assertEquals(user, u);
	}

	@Test
	public void userProfile() {
		Assert.assertEquals(user, userController.userProfile(user));
	}

	@Test
	public void testGetUserProfileWithoutGlobalRead() {
		User testUser = mock(User.class);
		when(userRepository.findUserByName("test", "test")).thenReturn(testUser);

		//
		userController.getUserProfile("test", "test", user, 1);
		//

		// check that we are going in the correct branch
		verify(user).projectsIdWithPermission(Permission.READ);
		verify(eventRepository).getUserActivityForProjects(eq(testUser.getId()), Mockito.<Date>any(),
				Mockito.<Collection<Integer>>any());
		verify(projectService).findProjectsActivityByUserInProjects(eq(testUser.getId()),
				Mockito.<Collection<Integer>>any());
		verify(eventRepository).getLatestActivityByPageAndProjects(eq(testUser.getId()), eq(1),
				Mockito.<Collection<Integer>>any());
	}

	@Test
	public void testGetUserProfileWithGlobalRead() {
		User testUser = mock(User.class);
		when(userRepository.findUserByName("test", "test")).thenReturn(testUser);

		Map<Permission, Permission> permission = new EnumMap<>(Permission.class);
		permission.put(Permission.READ, Permission.READ);
		when(user.getBasePermissions()).thenReturn(permission);

		//
		userController.getUserProfile("test", "test", user, 1);
		//

		// check that we are going in the correct branch
		verify(user, Mockito.never()).projectsWithPermission(Permission.READ);
		verify(eventRepository).getUserActivity(eq(testUser.getId()), Mockito.<Date>any());
		verify(projectService).findProjectsActivityByUser(eq(testUser.getId()));
		verify(eventRepository).getLatestActivityByPage(eq(testUser.getId()), eq(1));
	}

	@Test
	public void testClearAllTokens() {

		userController.clearAllTokens(user);

		verify(userRepository).clearAllTokens(eq(user));
	}

	@Test
	public void testGetCalendarToken() {

		String returnToken = "1234abcd";
		when(calendarService.findCalendarTokenFromUser(user)).thenReturn(returnToken);

		UserController.CalendarToken ct = userController.getCalendarToken(user);

		verify(calendarService).findCalendarTokenFromUser(eq(user));
		Assert.assertEquals(returnToken, ct.getToken());
	}

	@Test
	public void testClearCalendarToken() {

		userController.clearCalendarToken(user);

		verify(userRepository).deleteCalendarToken(eq(user));
		verify(calendarService).findCalendarTokenFromUser(eq(user));
	}

	@Test
	public void testUserCalendar() throws IOException, ValidationException, URISyntaxException {
		HttpServletResponse resp = mock(HttpServletResponse.class);
		final StubServletOutputStream servletOutputStream = new StubServletOutputStream();
		when(resp.getOutputStream()).thenReturn(servletOutputStream);

		String token = "1234abcd";

		Calendar calendar = new Calendar();
		calendar.getProperties().add(new ProdId("-//Lavagna//iCal4j 1.0//EN"));
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(CalScale.GREGORIAN);
		calendar.getProperties().add(Method.PUBLISH);
		VEvent event = new VEvent(new net.fortuna.ical4j.model.Date(), "name");
		event.getProperties().add(new Uid(UUID.randomUUID().toString()));
		Organizer organizer = new Organizer(URI.create("mailto:lavagna"));
		event.getProperties().add(organizer);
		calendar.getComponents().add(event);
		when(calendarService.getUserCalendar(eq(token))).thenReturn(calendar);

		userController.userCalendar(token, resp);

		verify(calendarService).getUserCalendar(eq(token));
	}

	class StubServletOutputStream extends ServletOutputStream {
		public ByteArrayOutputStream baos = new ByteArrayOutputStream();

		public void write(int i) throws IOException {
			baos.write(i);
		}

		@Override
		public boolean isReady() {
			return true;
		}

		@Override
		public void setWriteListener(WriteListener writeListener) {

		}
	}
}
