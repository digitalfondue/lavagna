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

import com.google.gson.JsonParseException;
import io.lavagna.common.Json;
import io.lavagna.model.User;
import io.lavagna.model.UserMetadata;
import io.lavagna.model.UserToCreate;
import io.lavagna.service.EventEmitter;
import io.lavagna.service.UserRepository;
import io.lavagna.service.UserService;
import io.lavagna.web.api.UsersAdministrationController.Update;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UsersAdministrationControllerTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private UserService userService;

	@Mock
	private EventEmitter eventEmitter;

	private UsersAdministrationController usersAdministrationController;

	@Before
	public void prepare() {
		usersAdministrationController = new UsersAdministrationController(userRepository, userService, eventEmitter);
	}

	@Test
	public void toggleUser() {
		User u = new User(42, "demo", "a", null, null, true, true, new Date(), false, Json.GSON.toJson(new UserMetadata(false, false)));
		Update up = new Update();
		up.setEnabled(false);
		usersAdministrationController.toggle(0, u, up);
		verify(userRepository).toggle(0, false);
	}

	@Test
	public void createUser() {
		UserToCreate utc = new UserToCreate();
		utc.setProvider("demo");
		utc.setUsername("username");
		usersAdministrationController.createUser(utc);
		verify(userService).createUser(utc);
	}

	@Test
	public void createUsers() throws JsonParseException, IOException {
		MultipartFile mpf = mock(MultipartFile.class);
		when(mpf.getInputStream()).thenReturn(
				new ByteArrayInputStream("[{\"provider\" : \"demo\", \"username\" : \"username\"}]".getBytes("UTF-8")));
		usersAdministrationController.createUsers(mpf);
		verify(userService).createUsers(Mockito.<List<UserToCreate>> any());
	}

	@Test(expected = IllegalArgumentException.class)
	public void sameUser() {
		User u = new User(0, "demo", "a", null, null, true, true, new Date(), false, Json.GSON.toJson(new UserMetadata(false, false)));
		Update up = new Update();
		up.setEnabled(false);
		usersAdministrationController.toggle(0, u, up);
		verify(userRepository, never()).toggle(anyInt(), anyBoolean());
	}
}
