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

import com.google.gson.reflect.TypeToken;
import io.lavagna.common.Json;
import io.lavagna.model.Permission;
import io.lavagna.model.User;
import io.lavagna.model.UserToCreate;
import io.lavagna.service.EventEmitter;
import io.lavagna.service.UserRepository;
import io.lavagna.service.UserService;
import io.lavagna.web.helper.ExpectPermission;
import org.apache.commons.lang3.Validate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

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

    @RequestMapping(value = "/api/user/{userId}", method = RequestMethod.POST)
    public void update(@PathVariable("userId") int userId, User user, @RequestBody Edit edit) {
        Validate.isTrue(user.getId() != userId, "cannot update the user");
        User userToEdit = userRepository.findById(userId);

        userRepository.updateProfile(userToEdit, edit.getEmail(), edit.getDisplayName(), user.getEmailNotification(), user.getSkipOwnNotifications());
        eventEmitter.emitUpdateUserProfile(userId);
    }

    @RequestMapping(value = "/api/user/{userId}/password", method = RequestMethod.POST)
    public void resetPassword(@PathVariable("userId") int userId, User user, @RequestBody PasswordReset passwordReset) {
        Validate.isTrue(user.getId() != userId, "cannot update the user");

        userService.changePassword(userId, passwordReset.getPassword());
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

	public static class Update {
		private boolean enabled;

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class PasswordReset {
	    private String password;

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class Edit {
	    private String displayName;
        private String email;

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
