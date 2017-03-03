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
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static java.util.EnumSet.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestServiceConfig.class, PersistenceAndServiceConfig.class })
@Transactional
public class EventRepositoryTest {

	@Autowired
	private ProjectService projectService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BoardRepository boardRepository;

	@Autowired
	private BoardColumnRepository boardColumnRepository;

	@Autowired
	private CardService cardService;

	@Autowired
	private CardRepository cardRepository;

	@Autowired
	private CardDataService cardDataService;

	@Autowired
	private CardDataRepository cardDataRepo;

	@Autowired
	private EventRepository eventRepository;

	private Project project;
	private Board board;
	private BoardColumn col1;
	// private BoardColumn col2;
	private Card card1;
	private User user;
	private User userAlt;
	private Date oneYearAgo;

	@Before
	public void prepare() {
		Helper.createUser(userRepository, "test", "test-user");
		user = userRepository.findUserByName("test", "test-user");

		Helper.createUser(userRepository, "test", "test-user-alt");
		userAlt = userRepository.findUserByName("test", "test-user-alt");

		project = projectService.create("test", "TEST", "desc");
		board = boardRepository.createNewBoard("test-board", "TEST-BRD", null, projectService.findByShortName("TEST")
				.getId());

		List<BoardColumnDefinition> definitions = projectService.findColumnDefinitionsByProjectId(projectService
				.findByShortName("TEST").getId());

		for (BoardColumnDefinition def : definitions) {
			if (def.getValue() == ColumnDefinition.OPEN) {
				col1 = boardColumnRepository.addColumnToBoard("col1", def.getId(), BoardColumnLocation.BOARD,
						board.getId());
			}
		}

		cardService.createCard("card1", col1.getId(), new Date(), user);
		List<CardFull> cards = cardRepository.findAllByColumnId(col1.getId());
		card1 = cards.get(0);

		oneYearAgo = DateUtils.addYears(new Date(), -1);
	}

	@Test
	public void testLastUpdateOnCardChange() {
		assertTrue(cardDataService.findAllCommentsByCardId(card1.getId()).isEmpty());
		assertTrue(cardDataRepo.findCountsByBoardIdAndLocation(board.getId(), BoardColumnLocation.BOARD).isEmpty());

		Date now = new Date();
		Date oneDayAgo = DateUtils.addDays(now, -1);

		// simulate CardDataService here
		CardData commentOld = cardDataRepo.createData(card1.getId(), CardType.COMMENT, "test-comment-old");
		eventRepository.insertCardDataEvent(commentOld.getId(), card1.getId(), EventType.COMMENT_CREATE,
				userAlt.getId(), commentOld.getId(), oneDayAgo);

		CardFull cardBeforeUpdate = cardRepository.findFullBy(card1.getId());
		assertEquals((Integer) userAlt.getId(), cardBeforeUpdate.getLastUpdateUserId());

		CardData comment = cardDataRepo.createData(card1.getId(), CardType.COMMENT, "test-comment");
		eventRepository.insertCardDataEvent(comment.getId(), card1.getId(), EventType.COMMENT_CREATE, user.getId(),
				comment.getId(), now);

		assertEquals(2, cardDataService.findAllCommentsByCardId(card1.getId()).size());

		CardFull cardAfterUpdate = cardRepository.findFullBy(card1.getId());

		assertEquals((Integer) user.getId(), cardAfterUpdate.getLastUpdateUserId());
		assertTrue(cardAfterUpdate.getLastUpdateTime().after(cardBeforeUpdate.getLastUpdateTime()));
	}

	@Test
	public void testLastUpdateOnUndo() {
		assertTrue(cardDataService.findAllCommentsByCardId(card1.getId()).isEmpty());
		assertTrue(cardDataRepo.findCountsByBoardIdAndLocation(board.getId(), BoardColumnLocation.BOARD).isEmpty());

		Date now = new Date();
		Date oneDayAgo = DateUtils.addDays(now, -1);
		Date twoDaysAgo = DateUtils.addDays(now, -2);

		CardData comment = cardDataService.createComment(card1.getId(), "test-update-comment", twoDaysAgo,
            userAlt.getId());
		assertEquals(1, cardDataService.findAllCommentsByCardId(card1.getId()).size());

		CardFull initialCardStatus = cardRepository.findFullBy(card1.getId());
		// right now, user alt is the last updater
		assertEquals((Integer) userAlt.getId(), initialCardStatus.getLastUpdateUserId());

		// simulate CardDataService here, user1 deletes user2 comment
		Event event = eventRepository.insertCardDataEvent(comment.getId(), comment.getCardId(),
				EventType.COMMENT_DELETE, user.getId(), comment.getId(), oneDayAgo);

		Event eventBis = eventRepository.getEventById(event.getId());

		Assert.assertEquals(event, eventBis);

		cardDataRepo.softDelete(comment.getId(), of(CardType.COMMENT));

		CardFull cardBeforeUpdate = cardRepository.findFullBy(card1.getId());

		cardDataService.undoDeleteComment(event);

		CardFull cardAfterUpdate = cardRepository.findFullBy(card1.getId());
		assertEquals((Integer) user.getId(), cardAfterUpdate.getLastUpdateUserId());
		assertTrue(cardAfterUpdate.getLastUpdateTime().after(cardBeforeUpdate.getLastUpdateTime()));
	}

	@Test
	public void testGetUserActivity() {
		List<EventsCount> activity = eventRepository.getUserActivity(user.getId(), oneYearAgo);
		Assert.assertEquals(1, activity.size());
		Assert.assertEquals(1, activity.get(0).getCount());
		Assert.assertTrue(DateUtils.isSameDay(new Date(), new Date(activity.get(0).getDate())));
	}

	@Test
	public void testGetUserActivityForProject() {

		List<EventsCount> activity = eventRepository.getUserActivityForProjects(user.getId(), oneYearAgo,
				Arrays.asList(project.getId()));
		Assert.assertEquals(1, activity.size());
		Assert.assertEquals(1, activity.get(0).getCount());
		Assert.assertTrue(DateUtils.isSameDay(new Date(), new Date(activity.get(0).getDate())));
	}

	@Test
	public void testGetUserActivityForEmptyProjectst() {
		List<EventsCount> activity = eventRepository.getUserActivityForProjects(user.getId(), oneYearAgo,
				Collections.<Integer> emptyList());
		Assert.assertEquals(0, activity.size());
	}

    @Test
    public void testGetLatestActivity() {
        Date yesterday = DateUtils.addDays(new Date(), -1);
        List<Event> events = eventRepository.getLatestActivity(user.getId(), yesterday);
        Assert.assertEquals(1, events.size());
    }

    @Test
    public void testGetLatestActivityByProjects() {
        Date yesterday = DateUtils.addDays(new Date(), -1);
        List<Event> events = eventRepository.getLatestActivityByProjects(user.getId(), yesterday,
            Arrays.asList(project.getId()));
        Assert.assertEquals(1, events.size());
    }

    @Test
    public void testGetLatestActivityByProjectsOnFakeProject() {
        List<Event> events = eventRepository.getLatestActivityByProjects(user.getId(), DateUtils.addDays(new Date(), 1),
            Arrays.asList(-1));
        Assert.assertEquals(0, events.size());
    }

	@Test
	public void testGetLatestActivityByPage() {
		List<Event> events = eventRepository.getLatestActivityByPage(user.getId(), 0);
		Assert.assertEquals(1, events.size());
	}

	@Test
	public void testGetLatestActivityByPageAndProjects() {
		List<Event> events = eventRepository.getLatestActivityByPageAndProjects(user.getId(), 0,
				Arrays.asList(project.getId()));
		Assert.assertEquals(1, events.size());
	}

	@Test
	public void testGetLatestActivityByPageAndProjectsOnFakeProject() {
		List<Event> events = eventRepository.getLatestActivityByPageAndProjects(user.getId(), 0, Arrays.asList(-1));
		Assert.assertEquals(0, events.size());
	}
}
