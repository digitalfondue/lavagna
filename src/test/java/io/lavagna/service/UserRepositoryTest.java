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

import io.lavagna.config.PersistenceAndServiceConfig;
import io.lavagna.model.Permission;
import io.lavagna.model.Project;
import io.lavagna.model.Role;
import io.lavagna.model.User;
import io.lavagna.service.config.TestServiceConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestServiceConfig.class, PersistenceAndServiceConfig.class })
@Transactional
public class UserRepositoryTest {

	private static final String TEST_USER_NAME = "test-user";

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private PermissionService permissionService;

	@Test
	public void createUserTest() {
		int res = Helper.createUser(userRepository, "test", TEST_USER_NAME);
		Assert.assertEquals(1, res);
	}

	@Test
	public void createUserFullTeste() {
		int res = userRepository.createUser("test", TEST_USER_NAME, null,"test@test", "ds", false);
		Assert.assertEquals(1, res);

	}

	@Test(expected = DuplicateKeyException.class)
	public void testUserNameUniquenessConstraint() {
		Assert.assertEquals(1, Helper.createUser(userRepository, "test", TEST_USER_NAME));
		Helper.createUser(userRepository, "test", TEST_USER_NAME);
	}

	@Test
	public void findExistingUser() {
		Helper.createUser(userRepository, "test", TEST_USER_NAME);
		User user = userRepository.findUserByName("test", TEST_USER_NAME);
		Assert.assertNotNull(user);
		Assert.assertEquals(TEST_USER_NAME, user.getUsername());
		Assert.assertEquals("test", user.getProvider());
	}

	@Test
	public void findById() {
		Helper.createUser(userRepository, "test", TEST_USER_NAME);
		User user = userRepository.findUserByName("test", TEST_USER_NAME);

		User uid = userRepository.findById(user.getId());
		Assert.assertNotNull(uid);

		Assert.assertEquals(user.getId(), uid.getId());
		Assert.assertEquals(user.getProvider(), uid.getProvider());
		Assert.assertEquals(user.getUsername(), uid.getUsername());
	}

	@Test
	public void findByIds() {
		Helper.createUser(userRepository, "test", TEST_USER_NAME + "1");
		Helper.createUser(userRepository, "test", TEST_USER_NAME + "2");
		User user1 = userRepository.findUserByName("test", TEST_USER_NAME + "1");
		User user2 = userRepository.findUserByName("test", TEST_USER_NAME + "2");
		List<User> res = userRepository.findByIds(Arrays.asList(user1.getId(), user2.getId()));
		Assert.assertTrue(res.size() == 2);
		Assert.assertTrue(res.contains(user1));
		Assert.assertTrue(res.contains(user2));

		Assert.assertTrue(userRepository.findByIds(Collections.<Integer>emptyList()).isEmpty());
	}

	@Test
	public void existUserAndEnabled() {
		Assert.assertFalse(userRepository.userExistsAndEnabled("test", TEST_USER_NAME));
		Helper.createUser(userRepository, "test", TEST_USER_NAME);
		Assert.assertTrue(userRepository.userExistsAndEnabled("test", TEST_USER_NAME));

		User u = userRepository.findUserByName("test", TEST_USER_NAME);

		userRepository.toggle(u.getId(), false);
		Assert.assertFalse(userRepository.userExistsAndEnabled("test", TEST_USER_NAME));

		userRepository.toggle(u.getId(), true);
		Assert.assertTrue(userRepository.userExistsAndEnabled("test", TEST_USER_NAME));
	}

	@Test
    public void existUser() {
        Assert.assertFalse(userRepository.userExists("test", TEST_USER_NAME));
        Helper.createUser(userRepository, "test", TEST_USER_NAME);
        Assert.assertTrue(userRepository.userExists("test", TEST_USER_NAME));

        User u = userRepository.findUserByName("test", TEST_USER_NAME);

        userRepository.toggle(u.getId(), false);
        Assert.assertTrue(userRepository.userExists("test", TEST_USER_NAME));

        userRepository.toggle(u.getId(), true);
        Assert.assertTrue(userRepository.userExists("test", TEST_USER_NAME));
    }

	@Test
	public void updateProfile() {
		Helper.createUser(userRepository, "test", TEST_USER_NAME);
		User user = userRepository.findUserByName("test", TEST_USER_NAME);
		Assert.assertNull(user.getEmail());
		Assert.assertNull(user.getDisplayName());

		userRepository.updateProfile(user, "email@email.getEmail()", "display name", true, true);

		User userUpdate = userRepository.findUserByName("test", TEST_USER_NAME);
		Assert.assertEquals("email@email.getEmail()", userUpdate.getEmail());
		Assert.assertEquals("display name", userUpdate.getDisplayName());

		userRepository.updateProfile(user, null, null, true, true);

		User userUpdate2 = userRepository.findUserByName("test", TEST_USER_NAME);
		Assert.assertNull(userUpdate2.getEmail());
		Assert.assertNull(userUpdate2.getDisplayName());
	}

	@Test
	public void findUsers() {
		Assert.assertTrue(userRepository.findUsers("-us").isEmpty());

		Helper.createUser(userRepository, "test", TEST_USER_NAME);

		Assert.assertFalse(userRepository.findUsers("-us").isEmpty());
		Assert.assertFalse(userRepository.findUsers("test-user").isEmpty());
		Assert.assertTrue(userRepository.findUsers("test-user-").isEmpty());
	}

	@Test
	public void findUsersInProject() {
	    Project project = projectService.create("TEST", "TEST", "desc");

	    Assert.assertTrue(userRepository.findUsers("-us", project.getId(), Permission.READ).isEmpty());
	    Helper.createUser(userRepository, "test", TEST_USER_NAME);

	    //user does not have role READ
	    Assert.assertTrue(userRepository.findUsers("-us", project.getId(), Permission.READ).isEmpty());

	    User user = userRepository.findUserByName("test", TEST_USER_NAME);

	    Role globalRead = new Role("READ");
	    permissionService.createRole(globalRead);
	    permissionService.updatePermissionsToRole(globalRead, EnumSet.of(Permission.READ));
	    permissionService.assignRoleToUsers(globalRead, Collections.singleton(user.getId()));

	    Assert.assertEquals(1, userRepository.findUsers("-us", project.getId(), Permission.READ).size());


        Helper.createUser(userRepository, "test", TEST_USER_NAME + "2");
        User user2 = userRepository.findUserByName("test", TEST_USER_NAME+"2");

        Role projectRead = new Role("READ");
        permissionService.createRoleInProjectId(projectRead, project.getId());
        permissionService.updatePermissionsToRoleInProjectId(projectRead, EnumSet.of(Permission.READ), project.getId());
        permissionService.assignRoleToUsersInProjectId(projectRead, Collections.singleton(user2.getId()), project.getId());


        Assert.assertEquals(2, userRepository.findUsers("-us", project.getId(), Permission.READ).size());
	}

	@Test
	public void findAll() {
		int initialCount = userRepository.findAll().size();

		Helper.createUser(userRepository, "test", TEST_USER_NAME + "0");
		Helper.createUser(userRepository, "test", TEST_USER_NAME + "1");

		Assert.assertTrue(userRepository.findAll().size() == initialCount + 2);
	}

	@Test
	public void testToggle() {
		Helper.createUser(userRepository, "test", TEST_USER_NAME);
		User u = userRepository.findUserByName("test", TEST_USER_NAME);
		Assert.assertTrue(u.getEnabled());
		userRepository.toggle(u.getId(), false);
		Assert.assertFalse(userRepository.findUserByName("test", TEST_USER_NAME).getEnabled());
		userRepository.toggle(u.getId(), true);
		Assert.assertTrue(userRepository.findUserByName("test", TEST_USER_NAME).getEnabled());
	}

	@Test(expected = EmptyResultDataAccessException.class)
	public void findNonExistingUser() {
		userRepository.findUserByName("test", TEST_USER_NAME);
	}

	@Test
	public void testFindUsersId() {
		Helper.createUser(userRepository, "test-1", TEST_USER_NAME + "0");
		Helper.createUser(userRepository, "test-2", TEST_USER_NAME + "1");

		Map<String, Integer> found = userRepository.findUsersId(Arrays.asList("test-1:" + TEST_USER_NAME + "0",
				"test-2:" + TEST_USER_NAME + "1", "testtest"));

		User u1 = userRepository.findUserByName("test-1", TEST_USER_NAME + "0");
		User u2 = userRepository.findUserByName("test-2", TEST_USER_NAME + "1");

		Assert.assertEquals(found.get("test-1:" + TEST_USER_NAME + "0").intValue(), u1.getId());
		Assert.assertEquals(found.get("test-2:" + TEST_USER_NAME + "1").intValue(), u2.getId());
		Assert.assertFalse(found.containsKey("testtest"));
	}

	@Test
	public void testDeleteAllTokens() {
		Helper.createUser(userRepository, "test", TEST_USER_NAME);
		User u = userRepository.findUserByName("test", TEST_USER_NAME);
		String token1 = userRepository.createRememberMeToken(u.getId());
		String token2 = userRepository.createRememberMeToken(u.getId());
		String token3 = userRepository.createRememberMeToken(u.getId());
		Assert.assertTrue(userRepository.rememberMeTokenExists(u.getId(), token1));
		Assert.assertTrue(userRepository.rememberMeTokenExists(u.getId(), token2));
		Assert.assertTrue(userRepository.rememberMeTokenExists(u.getId(), token3));

		userRepository.clearAllTokens(u);

		Assert.assertFalse(userRepository.rememberMeTokenExists(u.getId(), token1));
		Assert.assertFalse(userRepository.rememberMeTokenExists(u.getId(), token2));
		Assert.assertFalse(userRepository.rememberMeTokenExists(u.getId(), token3));
	}

	@Test
	public void testRememberMeTokenFlow() {
		Helper.createUser(userRepository, "test", TEST_USER_NAME);
		User u = userRepository.findUserByName("test", TEST_USER_NAME);

		Assert.assertFalse(userRepository.rememberMeTokenExists(u.getId(), "42"));
		String token = userRepository.createRememberMeToken(u.getId());
		Assert.assertTrue(userRepository.rememberMeTokenExists(u.getId(), token));
		userRepository.deleteRememberMeToken(u.getId(), token);
		Assert.assertFalse(userRepository.rememberMeTokenExists(u.getId(), token));
	}
}
