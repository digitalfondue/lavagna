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

import io.lavagna.common.Json;
import io.lavagna.model.Permission;
import io.lavagna.model.User;
import io.lavagna.model.UserToCreate;
import io.lavagna.service.EventEmitter;
import io.lavagna.service.UserRepository;
import io.lavagna.service.UserService;
import io.lavagna.web.helper.ExpectPermission;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.Validate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.reflect.TypeToken;

@RestController
@ExpectPermission(Permission.ADMINISTRATION)
public class UsersAdministrationController {

	private final Type userToCreateListType = new TypeToken<List<UserToCreate>>() {
	}.getType();

	private final UserRepository userRepository;
	private final UserService userService;
	private final EventEmitter eventEmitter;

	
	public UsersAdministrationController(UserRepository userRepository, UserService userService, EventEmitter eventEmitter) {
		this.userRepository = userRepository;
		this.userService = userService;
		this.eventEmitter = eventEmitter;
	}

	@RequestMapping(value = "/api/user/{userId}/enable", method = RequestMethod.POST)
	public void toggle(@PathVariable("userId") int userId, User user, @RequestBody Update status) {
		Validate.isTrue(user.getId() != userId, "cannot update the status");
		userRepository.toggle(userId, status.enabled);
		eventEmitter.emitUpdateUserProfile(userId);
	}

	@RequestMapping(value = "/api/user/insert", method = RequestMethod.POST)
	public void createUser(@RequestBody UserToCreate userToCreate) {
		userService.createUser(userToCreate);
	}

	@RequestMapping(value = "/api/user/bulk-insert", method = RequestMethod.POST)
	public void createUsers(@RequestParam("file") MultipartFile file) throws IOException {
		try (InputStream is = file.getInputStream();
				InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
			List<UserToCreate> usersToCreate = Json.GSON.fromJson(isr, userToCreateListType);
			userService.createUsers(usersToCreate);
		}
	}

	@Getter
	@Setter
	public static class Update {
		private boolean enabled;
	}
}
