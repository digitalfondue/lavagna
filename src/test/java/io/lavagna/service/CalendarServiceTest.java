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
import io.lavagna.model.User;
import io.lavagna.service.config.TestServiceConfig;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestServiceConfig.class, PersistenceAndServiceConfig.class })
public class CalendarServiceTest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CalendarService calendarService;

	private User user;

	@Before
	public void prepare() {
		Helper.createUser(userRepository, "test", "test-user");
		user = userRepository.findUserByName("test", "test-user");
	}

	@Test
	public void testTokenCreation() {
		String token = calendarService.findCalendarTokenFromUser(user);
		Assert.assertNotNull(token);
		Assert.assertEquals(64, token.length());
	}

	@Test
	public void testDeleteTokenCreation() {
		String token = calendarService.findCalendarTokenFromUser(user);
		userRepository.deleteCalendarToken(user);
		String newToken = calendarService.findCalendarTokenFromUser(user);

		Assert.assertNotNull(newToken);
		Assert.assertEquals(64, newToken.length());
		Assert.assertNotEquals(newToken, token);
	}
}
