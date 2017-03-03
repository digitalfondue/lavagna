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
import io.lavagna.model.CardLabel.LabelDomain;
import io.lavagna.model.CardLabel.LabelType;
import io.lavagna.model.CardLabelValue.LabelValue;
import io.lavagna.service.config.TestServiceConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static io.lavagna.common.Constants.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

//TODO complete cover
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestServiceConfig.class, PersistenceAndServiceConfig.class })
@Transactional
public class BulkOperationServiceTest {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ProjectService projectService;
	@Autowired
	private BoardRepository boardRepository;
	@Autowired
	private BoardColumnRepository boardColumnRepository;
	@Autowired
	private CardService cardService;
	@Autowired
	private CardRepository cardRepository;
	@Autowired
	private CardLabelRepository cardLabelRepository;

	@Autowired
	private BulkOperationService bulkOperationService;

	//
	private CardFull card1;
	private CardFull card2;
	private CardFull card3;
	private User user;
	private User user2;
    private Project project;

	//

	@Before
	public void prepare() {
		Helper.createUser(userRepository, "test", "test-user");
		user = userRepository.findUserByName("test", "test-user");

		Helper.createUser(userRepository, "test", "test-user-2");

		user2 = userRepository.findUserByName("test", "test-user-2");

		project = projectService.create("test", "TEST", "desc");
		Board board = boardRepository.createNewBoard("test-board", "TEST-BRD", null, project.getId());

		List<BoardColumnDefinition> definitions = projectService.findColumnDefinitionsByProjectId(project.getId());
		boardColumnRepository.addColumnToBoard("col1", definitions.get(0).getId(),
				BoardColumn.BoardColumnLocation.BOARD, board.getId());
		List<BoardColumn> cols = boardColumnRepository.findAllColumnsFor(board.getId(),
				BoardColumn.BoardColumnLocation.BOARD);
		BoardColumn col1 = cols.get(0);
		cardService.createCard("card1", col1.getId(), new Date(), user);
		cardService.createCard("card2", col1.getId(), new Date(), user);
		cardService.createCard("card3", col1.getId(), new Date(), user);
		List<CardFull> cards = cardRepository.findAllByColumnId(col1.getId());

		card1 = cards.get(0);
		card2 = cards.get(1);
		card3 = cards.get(2);
	}

	private boolean hasLabel(int cardId, String name) {
		Map<CardLabel, List<CardLabelValue>> res = cardLabelRepository.findCardLabelValuesByCardId(cardId);
		for (CardLabel cl : res.keySet()) {
			if (cl.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasLabelValue(int cardId, String name, LabelValue value) {
		Map<CardLabel, List<CardLabelValue>> res = cardLabelRepository.findCardLabelValuesByCardId(cardId);
		for (CardLabel cl : res.keySet()) {
			if (cl.getName().equals(name) && hasLabelValue(res.get(cl), value)) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasLabelValue(List<CardLabelValue> clv, LabelValue value) {
		for (CardLabelValue c : clv) {
			if (c.getValue().equals(value)) {
				return true;
			}
		}
		return false;
	}

	@Test
	public void testAssign() {
		assertFalse(hasLabel(card1.getId(), SYSTEM_LABEL_ASSIGNED));
		assertFalse(hasLabel(card2.getId(), SYSTEM_LABEL_ASSIGNED));
		assertFalse(hasLabel(card3.getId(), SYSTEM_LABEL_ASSIGNED));

		bulkOperationService.assign("TEST", Arrays.asList(card1.getId(), card3.getId()), new LabelValue(null, null,
				null, null, user.getId(), null), user);

		assertTrue(hasLabel(card1.getId(), SYSTEM_LABEL_ASSIGNED));
		assertTrue(hasLabel(card3.getId(), SYSTEM_LABEL_ASSIGNED));
		assertFalse(hasLabel(card2.getId(), SYSTEM_LABEL_ASSIGNED));

	}

	@Test
	public void testRemoveAssign() {
		LabelValue toUser = new LabelValue(null, null, null, null, user.getId(), null);
		bulkOperationService.assign("TEST", Arrays.asList(card1.getId(), card3.getId()), toUser, user);
		assertTrue(hasLabel(card1.getId(), SYSTEM_LABEL_ASSIGNED));
		assertTrue(hasLabel(card3.getId(), SYSTEM_LABEL_ASSIGNED));
		assertFalse(hasLabel(card2.getId(), SYSTEM_LABEL_ASSIGNED));

		bulkOperationService.removeAssign("TEST", Arrays.asList(card1.getId(), card2.getId(), card3.getId()), toUser,
				user);

		assertFalse(hasLabel(card1.getId(), SYSTEM_LABEL_ASSIGNED));
		assertFalse(hasLabel(card2.getId(), SYSTEM_LABEL_ASSIGNED));
		assertFalse(hasLabel(card3.getId(), SYSTEM_LABEL_ASSIGNED));
	}

	@Test
    public void testWatch() {
        assertFalse(hasLabel(card1.getId(), SYSTEM_LABEL_WATCHED_BY));
        assertFalse(hasLabel(card2.getId(), SYSTEM_LABEL_WATCHED_BY));
        assertFalse(hasLabel(card3.getId(), SYSTEM_LABEL_WATCHED_BY));

        bulkOperationService.watch("TEST", Arrays.asList(card1.getId(), card3.getId()), user);

        assertTrue(hasLabel(card1.getId(), SYSTEM_LABEL_WATCHED_BY));
        assertTrue(hasLabel(card3.getId(), SYSTEM_LABEL_WATCHED_BY));
        assertFalse(hasLabel(card2.getId(), SYSTEM_LABEL_WATCHED_BY));
    }

	@Test
    public void testRemoveWatch() {
        bulkOperationService.watch("TEST", Arrays.asList(card1.getId(), card3.getId()), user);
        assertTrue(hasLabel(card1.getId(), SYSTEM_LABEL_WATCHED_BY));
        assertTrue(hasLabel(card3.getId(), SYSTEM_LABEL_WATCHED_BY));
        assertFalse(hasLabel(card2.getId(), SYSTEM_LABEL_WATCHED_BY));

        bulkOperationService.removeWatch("TEST", Arrays.asList(card1.getId(), card2.getId(), card3.getId()), user);

        assertFalse(hasLabel(card1.getId(), SYSTEM_LABEL_WATCHED_BY));
        assertFalse(hasLabel(card2.getId(), SYSTEM_LABEL_WATCHED_BY));
        assertFalse(hasLabel(card3.getId(), SYSTEM_LABEL_WATCHED_BY));
    }

	@Test
	public void testReAssign() {
		LabelValue toUser = new LabelValue(null, null, null, null, user.getId(), null);
		bulkOperationService.assign("TEST", Arrays.asList(card1.getId(), card2.getId(), card3.getId()), toUser, user);
		assertTrue(hasLabelValue(card1.getId(), SYSTEM_LABEL_ASSIGNED, toUser));
		assertTrue(hasLabelValue(card2.getId(), SYSTEM_LABEL_ASSIGNED, toUser));
		assertTrue(hasLabelValue(card3.getId(), SYSTEM_LABEL_ASSIGNED, toUser));

		LabelValue toUser2 = new LabelValue(null, null, null, null, user2.getId(), null);

		bulkOperationService.reAssign("TEST", Arrays.asList(card1.getId(), card3.getId()), toUser2, user);
		assertTrue(hasLabelValue(card1.getId(), SYSTEM_LABEL_ASSIGNED, toUser2));
		assertFalse(hasLabelValue(card1.getId(), SYSTEM_LABEL_ASSIGNED, toUser));

		assertTrue(hasLabelValue(card3.getId(), SYSTEM_LABEL_ASSIGNED, toUser2));
		assertFalse(hasLabelValue(card3.getId(), SYSTEM_LABEL_ASSIGNED, toUser));

		assertTrue(hasLabelValue(card2.getId(), SYSTEM_LABEL_ASSIGNED, toUser));
		assertFalse(hasLabelValue(card2.getId(), SYSTEM_LABEL_ASSIGNED, toUser2));
	}

	@Test
	public void testSetDueDate() {
		LabelValue date = new LabelValue(new Date());
		bulkOperationService.setDueDate("TEST", Arrays.asList(card1.getId(), card2.getId(), card3.getId()), date, user);
		assertTrue(hasLabel(card1.getId(), SYSTEM_LABEL_DUE_DATE));
		assertTrue(hasLabel(card2.getId(), SYSTEM_LABEL_DUE_DATE));
		assertTrue(hasLabel(card3.getId(), SYSTEM_LABEL_DUE_DATE));
	}

	@Test
	public void testRemoveDueDate() {
		LabelValue date = new LabelValue(new Date());
		bulkOperationService.setDueDate("TEST", Arrays.asList(card1.getId(), card2.getId(), card3.getId()), date, user);
		assertTrue(hasLabel(card1.getId(), SYSTEM_LABEL_DUE_DATE));
		assertTrue(hasLabel(card2.getId(), SYSTEM_LABEL_DUE_DATE));
		assertTrue(hasLabel(card3.getId(), SYSTEM_LABEL_DUE_DATE));

		bulkOperationService.removeDueDate("TEST", Arrays.asList(card1.getId(), card2.getId(), card3.getId()), user);

		assertFalse(hasLabel(card1.getId(), SYSTEM_LABEL_DUE_DATE));
		assertFalse(hasLabel(card2.getId(), SYSTEM_LABEL_DUE_DATE));
		assertFalse(hasLabel(card3.getId(), SYSTEM_LABEL_DUE_DATE));
	}

	@Test
	public void testSetMilestone() {

	    CardLabel milestone = cardLabelRepository.findLabelByName(project.getId(), SYSTEM_LABEL_MILESTONE, LabelDomain.SYSTEM);

	    LabelValue milestone10 = new LabelValue(null, null, null, null, null, cardLabelRepository.addLabelListValue(milestone.getId(), "1.0").getId());
	    LabelValue milestone11 = new LabelValue(null, null, null, null, null, cardLabelRepository.addLabelListValue(milestone.getId(), "1.1").getId());

	    bulkOperationService.setMilestone("TEST", Arrays.asList(card1.getId()), milestone10, user);
	    assertTrue(hasLabel(card1.getId(), SYSTEM_LABEL_MILESTONE));
        assertFalse(hasLabel(card2.getId(), SYSTEM_LABEL_MILESTONE));
        assertFalse(hasLabel(card3.getId(), SYSTEM_LABEL_MILESTONE));

        bulkOperationService.setMilestone("TEST", Arrays.asList(card1.getId(), card2.getId(), card3.getId()), milestone11, user);
        assertTrue(hasLabel(card1.getId(), SYSTEM_LABEL_MILESTONE));
        assertTrue(hasLabel(card2.getId(), SYSTEM_LABEL_MILESTONE));
        assertTrue(hasLabel(card3.getId(), SYSTEM_LABEL_MILESTONE));
	}

	@Test
	public void testRemoveMilestone() {
	    CardLabel milestone = cardLabelRepository.findLabelByName(project.getId(), SYSTEM_LABEL_MILESTONE, LabelDomain.SYSTEM);
        LabelValue milestone10 = new LabelValue(null, null, null, null, null, cardLabelRepository.addLabelListValue(milestone.getId(), "1.0").getId());

        bulkOperationService.setMilestone("TEST", Arrays.asList(card1.getId(), card2.getId(), card3.getId()), milestone10, user);
        assertTrue(hasLabel(card1.getId(), SYSTEM_LABEL_MILESTONE));
        assertTrue(hasLabel(card2.getId(), SYSTEM_LABEL_MILESTONE));
        assertTrue(hasLabel(card3.getId(), SYSTEM_LABEL_MILESTONE));

        bulkOperationService.removeMilestone("TEST", Arrays.asList(card1.getId(), card2.getId(), card3.getId()), user);
        assertFalse(hasLabel(card1.getId(), SYSTEM_LABEL_MILESTONE));
        assertFalse(hasLabel(card2.getId(), SYSTEM_LABEL_MILESTONE));
        assertFalse(hasLabel(card3.getId(), SYSTEM_LABEL_MILESTONE));
	}

	@Test
    public void testUserLabel() {
	    CardLabel userLabel = cardLabelRepository.addLabel(project.getId(), true, LabelType.NULL, LabelDomain.USER, "UserLabel", 0);

	    assertFalse(hasLabel(card1.getId(), "UserLabel"));
        assertFalse(hasLabel(card2.getId(), "UserLabel"));
        assertFalse(hasLabel(card3.getId(), "UserLabel"));

	    bulkOperationService.addUserLabel("TEST", userLabel.getId(), new LabelValue(), Arrays.asList(card1.getId()), user);

	    assertTrue(hasLabel(card1.getId(), "UserLabel"));
        assertFalse(hasLabel(card2.getId(), "UserLabel"));
        assertFalse(hasLabel(card3.getId(), "UserLabel"));

	    bulkOperationService.addUserLabel("TEST", userLabel.getId(), new LabelValue(), Arrays.asList(card1.getId(), card2.getId(), card3.getId()), user);

	    assertTrue(hasLabel(card1.getId(), "UserLabel"));
        assertTrue(hasLabel(card2.getId(), "UserLabel"));
        assertTrue(hasLabel(card3.getId(), "UserLabel"));

        bulkOperationService.removeUserLabel("TEST", userLabel.getId(), new LabelValue(), Arrays.asList(card1.getId(), card2.getId(), card3.getId()), user);

        assertFalse(hasLabel(card1.getId(), "UserLabel"));
        assertFalse(hasLabel(card2.getId(), "UserLabel"));
        assertFalse(hasLabel(card3.getId(), "UserLabel"));
	}

}
