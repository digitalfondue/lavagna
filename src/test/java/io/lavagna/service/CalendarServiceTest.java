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
import io.lavagna.model.BoardColumn;
import io.lavagna.model.CardLabel;
import io.lavagna.model.CardLabelValue;
import io.lavagna.model.ColumnDefinition;
import io.lavagna.model.Permission;
import io.lavagna.model.Project;
import io.lavagna.model.Role;
import io.lavagna.model.User;
import io.lavagna.service.config.TestServiceConfig;

import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;

import lombok.val;
import net.fortuna.ical4j.model.Calendar;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestServiceConfig.class, PersistenceAndServiceConfig.class })
@Transactional
public class CalendarServiceTest {

	@Autowired
	private ProjectService projectService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BoardRepository boardRepository;
	@Autowired
	private BoardColumnRepository boardColumnRepository;
	@Autowired
	private PermissionService permissionService;
	@Autowired
	private CardService cardService;
	@Autowired
	private CalendarService calendarService;
	@Autowired
	private LabelService labelService;
	@Autowired
	private CardLabelRepository cardLabelRepository;

	private Project project;

	private BoardColumn col;

	private User user;

	@Before
	public void prepare() {

		Helper.createUser(userRepository, "test", "test-user");
		user = userRepository.findUserByName("test", "test-user");

		project = projectService.create("test", "TEST", "desc");
		val board = boardRepository.createNewBoard("test-board", "TEST-BRD", null, project.getId());

		val definitions = projectService.findColumnDefinitionsByProjectId(project.getId());
		for (val def : definitions) {
			if (def.getValue() == ColumnDefinition.OPEN) {
				col = boardColumnRepository.addColumnToBoard("col1", def.getId(), BoardColumn.BoardColumnLocation.BOARD,
						board.getId());
			}
		}
		Assert.assertEquals(BoardColumn.BoardColumnLocation.BOARD, col.getLocation());
		Assert.assertEquals(ColumnDefinition.OPEN, col.getStatus());


		val r = new Role("TEST");
		permissionService.createRole(r);
		permissionService.updatePermissionsToRole(r, EnumSet.of(Permission.READ));
		permissionService.assignRolesToUsers(Collections.singletonMap(r, Collections.singleton(user.getId())));
	}

	@Test
	public void testTokenCreation() {
		String token = calendarService.findCalendarTokenFromUser(user);
		Assert.assertNotNull(token);
		Assert.assertEquals(64, token.length());
	}

	@Test
	public void testDoubleFindTokenCreation() {
		String token = calendarService.findCalendarTokenFromUser(user);
		String secondToken = calendarService.findCalendarTokenFromUser(user);

		Assert.assertNotNull(secondToken);
		Assert.assertEquals(64, secondToken.length());
		Assert.assertEquals(secondToken, token);
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

	@Test(expected = SecurityException.class)
	public void testGetUserCalendarWithWrongToken() {
		calendarService.getUserCalendar("abcd");
	}

	@Test
	public void testGetUserCalendarOnEmpty() {
		String token = calendarService.findCalendarTokenFromUser(user);

		Calendar calendar = calendarService.getUserCalendar(token);

		Assert.assertNotNull(calendar);
		Assert.assertEquals(0, calendar.getComponents().size());
	}

	@Test
	public void testGetUserCalendar() {

		val assignedCard = cardService.createCard("card1", col.getId(), new Date(), user);

		val watchedCard = cardService.createCard("card2", col.getId(), new Date(), user);

		val now = new Date();

		val assigned = cardLabelRepository.findLabelByName(project.getId(), "ASSIGNED", CardLabel.LabelDomain.SYSTEM);
		labelService.addLabelValueToCard(assigned, assignedCard.getId(), new CardLabelValue.LabelValue(user.getId()),
				user, now);

		val watched = cardLabelRepository.findLabelByName(project.getId(), "WATCHED_BY", CardLabel.LabelDomain.SYSTEM);
		labelService.addLabelValueToCard(watched, watchedCard.getId(), new CardLabelValue.LabelValue(user.getId()),
				user, now);

		val token = calendarService.findCalendarTokenFromUser(user);

		val dueDate= cardLabelRepository.findLabelByName(project.getId(), "DUE_DATE", CardLabel.LabelDomain.SYSTEM);
		labelService.addLabelValueToCard(dueDate, assignedCard.getId(), new CardLabelValue.LabelValue(now), user, now);
		labelService.addLabelValueToCard(dueDate, watchedCard.getId(), new CardLabelValue.LabelValue(now), user, now);


		val calendar = calendarService.getUserCalendar(token);

		Assert.assertNotNull(calendar);
		Assert.assertEquals(2, calendar.getComponents().size());
	}
}
