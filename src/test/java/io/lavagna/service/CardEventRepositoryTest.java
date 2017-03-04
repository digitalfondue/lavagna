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
import io.lavagna.model.*;
import io.lavagna.model.BoardColumn.BoardColumnLocation;
import io.lavagna.model.Event.EventType;
import io.lavagna.service.config.TestServiceConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestServiceConfig.class, PersistenceAndServiceConfig.class })
@Transactional
public class CardEventRepositoryTest {

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private CardService cardService;

	@Autowired
	private CardRepository cardRepository;

	@Autowired
	private CardDataService cardDataService;

	@Autowired
	private BoardRepository boardRepository;

	@Autowired
	private BoardColumnRepository boardColumnRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProjectService projectService;

	private User user;
	private User user2;

	private Card card1;

	@Before
	public void prepare() {
		Helper.createUser(userRepository, "test", "test-user");
		Helper.createUser(userRepository, "test", "test-user2");
		user = userRepository.findUserByName("test", "test-user");
		user2 = userRepository.findUserByName("test", "test-user2");
		Project p = projectService.create("test", "TEST", "desc");
		Board board = boardRepository.createNewBoard("test-board", "TEST-BRD", null, p.getId());
		List<BoardColumnDefinition> definitions = projectService.findColumnDefinitionsByProjectId(projectService
				.findByShortName("TEST").getId());
		BoardColumn col1 = boardColumnRepository
				.addColumnToBoard("col1", definitions.get(0).getId(), BoardColumnLocation.BOARD, board.getId());
		cardService.createCard("card1", col1.getId(), new Date(), user);
		List<CardFull> cards = cardRepository.findAllByColumnId(col1.getId());
		card1 = cards.get(0);
	}

	@Test
	public void findUsersIdFor() {
		CardData cd = cardDataService.createComment(card1.getId(), "test", new Date(), user.getId());

		Set<Integer> res1 = eventRepository.findUsersIdFor(cd.getId(), EventType.COMMENT_CREATE);
		Assert.assertTrue(res1.size() == 1);
		Assert.assertTrue(res1.contains(user.getId()));

		Set<Integer> res2 = eventRepository.findUsersIdFor(cd.getId(), EventType.COMMENT_UPDATE);
		Assert.assertTrue(res2.isEmpty());

		cardDataService.updateComment(cd.getId(), "test 2", new Date(), user2);
		cardDataService.updateComment(cd.getId(), "test", new Date(), user);

		Set<Integer> res3 = eventRepository.findUsersIdFor(cd.getId(), EventType.COMMENT_UPDATE);
		Assert.assertTrue(res3.size() == 2);
		Assert.assertTrue(res3.contains(user.getId()));
		Assert.assertTrue(res3.contains(user2.getId()));
	}
}
