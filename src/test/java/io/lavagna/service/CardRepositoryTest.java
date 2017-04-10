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

import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestServiceConfig.class, PersistenceAndServiceConfig.class })
@Transactional
public class CardRepositoryTest {

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
	private ProjectService projectService;

	private Project project;

	private Board board;

	private BoardColumn col1;

	private BoardColumn col2;

	private User user;

	private static void checkCard(Card card, String expectedName, int expectedOrder) {
		Assert.assertEquals(expectedName, card.getName());
		Assert.assertEquals(expectedOrder, card.getOrder());
	}

	@Before
	public void prepare() {
		Helper.createUser(userRepository, "test", "test-user");
		user = userRepository.findUserByName("test", "test-user");
		project = projectService.create("test", "TEST", "desc");
		boardRepository
				.createNewBoard("test-board", "TESTBRD", null, project.getId());
		board = boardRepository.findBoardByShortName("TESTBRD");
		Map<ColumnDefinition, BoardColumnDefinition> definitions = projectService.findMappedColumnDefinitionsByProjectId(projectService.findByShortName("TEST").getId());

		boardColumnRepository.addColumnToBoard("col1", definitions.get(ColumnDefinition.OPEN).getId(), BoardColumnLocation.BOARD,
				board.getId());
		boardColumnRepository.addColumnToBoard("col2", definitions.get(ColumnDefinition.CLOSED).getId(), BoardColumnLocation.BOARD,
				board.getId());
		List<BoardColumn> cols = boardColumnRepository.findAllColumnsFor(board.getId(), BoardColumnLocation.BOARD);
		col1 = cols.get(0);
		col2 = cols.get(1);
	}

	@Test
	public void testCreateCardAndFetch() {
		Assert.assertTrue(cardRepository.findAllByBoardIdAndLocation(board.getId(), BoardColumnLocation.BOARD).isEmpty());
		Assert.assertTrue(cardRepository.findAllByColumnId(col1.getId()).isEmpty());
		Assert.assertTrue(cardRepository.findAllByColumnId(col2.getId()).isEmpty());

		Assert.assertEquals("card1", cardService.createCard("card1", col1.getId(), new Date(), user).getName());
		Assert.assertEquals(1, cardRepository.findAllByBoardIdAndLocation(board.getId(), BoardColumnLocation.BOARD).size());

		List<CardFull> cardsCol1 = cardRepository.findAllByColumnId(col1.getId());
		Assert.assertEquals(1, cardsCol1.size());
		Assert.assertTrue(cardRepository.findAllByColumnId(col2.getId()).isEmpty());

		Card cardById = cardRepository.findBy(cardsCol1.get(0).getId());
		Assert.assertEquals(cardsCol1.get(0).getId(), cardById.getId());
		Assert.assertEquals(cardsCol1.get(0).getName(), cardById.getName());
	}

	@Test
	public void testCreateCardAndFetchFull() {
		Assert.assertTrue(cardRepository.findAllByBoardIdAndLocation(board.getId(), BoardColumnLocation.BOARD).isEmpty());
		Assert.assertTrue(cardRepository.findAllByColumnId(col1.getId()).isEmpty());
		Assert.assertTrue(cardRepository.findAllByColumnId(col2.getId()).isEmpty());

		Assert.assertEquals("card1", cardService.createCard("card1", col1.getId(), new Date(), user).getName());
		Assert.assertEquals(1, cardRepository.findAllByBoardIdAndLocation(board.getId(), BoardColumnLocation.BOARD).size());

		List<CardFull> cardsCol1 = cardRepository.findAllByColumnId(col1.getId());
		Assert.assertEquals(1, cardsCol1.size());
		Assert.assertTrue(cardRepository.findAllByColumnId(col2.getId()).isEmpty());

		Card cardById = cardRepository.findBy(cardsCol1.get(0).getId());
		Assert.assertEquals(cardsCol1.get(0).getId(), cardById.getId());
		Assert.assertEquals(cardsCol1.get(0).getName(), cardById.getName());

		CardFull cardFullById = cardRepository.findFullBy(cardsCol1.get(0).getId());
		Assert.assertEquals(cardById.getId(), cardFullById.getId());
		Assert.assertEquals(cardById.getName(), cardFullById.getName());
	}

	@Test
	public void testFindFullById() {
		Card c1 = cardService.createCard("card1", col1.getId(), new Date(), user);

		Card found = cardRepository.findFullBy(c1.getId());

		Assert.assertEquals(c1.getId(), found.getId());
		Assert.assertEquals(c1.getName(), found.getName());
	}

	@Test
	public void testFindFullBySeq() {
		Card c1 = cardService.createCard("card1", col1.getId(), new Date(), user);

		Card found = cardRepository.findFullBy(board.getShortName(), c1.getSequence());

		Assert.assertEquals(c1.getId(), found.getId());
		Assert.assertEquals(c1.getName(), found.getName());
	}

	@Test
	public void testFindAllByIds() {
		Card c1 = cardService.createCard("card1", col1.getId(), new Date(), user);
		Card c2 = cardService.createCard("card2", col1.getId(), new Date(), user);
		List<CardFull> res = cardRepository.findAllByIds(Arrays.asList(c1.getId(), c2.getId()));
		Assert.assertTrue(res.size() == 2);

		Assert.assertTrue(cardRepository.findAllByIds(Collections.<Integer>emptyList()).isEmpty());
	}

	@Test
	public void testExistCardWith() {
	    Card c1 = cardService.createCard("card1", col1.getId(), new Date(), user);
	    Assert.assertFalse(cardRepository.existCardWith(board.getShortName(), c1.getSequence()+1));
	    Assert.assertTrue(cardRepository.existCardWith(board.getShortName(), c1.getSequence()));
	}

	@Test
	public void testCreateCardFromTopAndOrder() {
		Card card1 = cardService.createCardFromTop("card1", col1.getId(), new Date(), user);
		Assert.assertEquals("card1", card1.getName());
		Assert.assertEquals(1, cardRepository.findAllByBoardIdAndLocation(board.getId(), BoardColumnLocation.BOARD).size());

		Card card0 = cardService.createCardFromTop("card0", col1.getId(), new Date(), user);
		Assert.assertEquals("card0", card0.getName());

		List<CardFull> cards = cardRepository.findAllByColumnId(col1.getId());
		Assert.assertEquals(2, cards.size());

		checkCard(cards.get(0), "card0", 0);
		checkCard(cards.get(1), "card1", 1);
	}

	@Test
	public void testUpdateCardOrderEmpty() {// nothing should happen
		cardRepository.updateCardOrder(Collections.<Integer> emptyList(), col1.getId());
	}

	@Test
	public void testUpdateCardOrder() {
		cardService.createCard("card1", col1.getId(), new Date(), user);
		cardService.createCard("card2", col1.getId(), new Date(), user);
		cardService.createCard("card3", col1.getId(), new Date(), user);

		List<CardFull> cards = cardRepository.findAllByColumnId(col1.getId());
		checkCard(cards.get(0), "card1", 1);
		checkCard(cards.get(1), "card2", 2);
		checkCard(cards.get(2), "card3", 3);

		// invert the order
		cardRepository.updateCardOrder(Arrays.asList(cards.get(2).getId(), cards.get(1).getId(), cards.get(0).getId()),
				col1.getId());

		List<CardFull> cardsUpdated = cardRepository.findAllByColumnId(col1.getId());
		checkCard(cardsUpdated.get(0), "card3", 1);
		checkCard(cardsUpdated.get(1), "card2", 2);
		checkCard(cardsUpdated.get(2), "card1", 3);
	}

	@Test
	public void testMoveCardToColumnAndReorder() {
		cardService.createCard("card1", col1.getId(), new Date(), user);
		cardService.createCard("card2", col1.getId(), new Date(), user);
		cardService.createCard("card3", col1.getId(), new Date(), user);

		List<CardFull> cards = cardRepository.findAllByColumnId(col1.getId());

		Assert.assertEquals(3, cards.size());
		Assert.assertTrue(cardRepository.findAllByColumnId(col2.getId()).isEmpty());

		// move the first card in the second column
		cardRepository.moveCardToColumnAndReorder(cards.get(0).getId(), col1.getId(), col2.getId(),
				Arrays.asList(cards.get(0).getId()));

		List<CardFull> cardsCol1 = cardRepository.findAllByColumnId(col1.getId());
		List<CardFull> cardsCol2 = cardRepository.findAllByColumnId(col2.getId());

		Assert.assertEquals(2, cardsCol1.size());
		Assert.assertEquals(1, cardsCol2.size());

		checkCard(cardsCol1.get(0), "card2", 2);
		checkCard(cardsCol1.get(1), "card3", 3);

		checkCard(cardsCol2.get(0), "card1", 1);

	}

	@Test
	public void testMoveCardsToColumnWithoutReorder() {
		cardService.createCard("card1", col1.getId(), new Date(), user);
		cardService.createCard("card2", col1.getId(), new Date(), user);
		cardService.createCard("card3", col1.getId(), new Date(), user);

		List<CardFull> cards = cardRepository.findAllByColumnId(col1.getId());

		Assert.assertEquals(3, cards.size());
		Assert.assertTrue(cardRepository.findAllByColumnId(col2.getId()).isEmpty());

		cardService.moveCardsToColumn(Arrays.asList(cards.get(0).getId(), cards.get(1).getId(), cards.get(2).getId()),
				col1.getId(), col2.getId(), user.getId(), EventType.CARD_MOVE, new Date());

		List<CardFull> cardsCol1 = cardRepository.findAllByColumnId(col1.getId());
		List<CardFull> cardsCol2 = cardRepository.findAllByColumnId(col2.getId());

		Assert.assertEquals(0, cardsCol1.size());
		Assert.assertEquals(3, cardsCol2.size());
	}

	@Test
	public void testUpdateCard() {
		Card c = cardService.createCard("card1", col1.getId(), new Date(), user);

		cardRepository.updateCard(c.getId(), "new-name");

		Card updated = cardRepository.findBy(c.getId());

		Assert.assertEquals(c.getId(), updated.getId());
		Assert.assertEquals(c.getOrder(), updated.getOrder());
		Assert.assertEquals(c.getSequence(), updated.getSequence());
		Assert.assertEquals(c.getOrder(), updated.getOrder());
		Assert.assertEquals("new-name", updated.getName());
	}

	@Test
	public void testFindCards() {
		cardService.createCard("card1", col1.getId(), new Date(), user);
		cardService.createCard("card2", col1.getId(), new Date(), user);
		cardService.createCard("card3", col1.getId(), new Date(), user);

		Assert.assertEquals(0, cardRepository.findCards(board.getId(), "z").size());
		Assert.assertEquals(3, cardRepository.findCards(board.getId(), "card").size());
	}

	@Test
	public void testFindCardsId() {
		Card c1 = cardService.createCard("card1", col1.getId(), new Date(), user);
		Card c2 = cardService.createCard("card2", col1.getId(), new Date(), user);
		Card c3 = cardService.createCard("card3", col1.getId(), new Date(), user);

		Map<String, Integer> res = cardRepository.findCardsIds(Arrays.asList("TESTBRD-" + c1.getSequence(),
				"TESTBRD-" + c2.getSequence(), "TESTBRD-" + c3.getSequence(), "TESTBRD-abcd"));
		Assert.assertEquals(res.get("TESTBRD-" + c1.getSequence()).intValue(), c1.getId());
		Assert.assertEquals(res.get("TESTBRD-" + c2.getSequence()).intValue(), c2.getId());
		Assert.assertEquals(res.get("TESTBRD-" + c3.getSequence()).intValue(), c3.getId());
	}

	@Test
	public void testFindCardBy() {
		Card c1 = cardService.createCard("card1", col1.getId(), new Date(), user);

		// find by card "title"
		Assert.assertEquals(c1.getId(), cardRepository.findCardBy("card1", null).get(0).getId());

		// find by card sequenceNr
		Assert.assertEquals(c1.getId(), cardRepository.findCardBy(Integer.toString(c1.getSequence()), null).get(0).getId());

		// find by board short name
		Assert.assertEquals(c1.getId(), cardRepository.findCardBy("TESTBRD", null).get(0).getId());
		Assert.assertEquals(c1.getId(), cardRepository.findCardBy("TESTBRD-", null).get(0).getId());

		// find by board short name + seq nr
		Assert.assertEquals(c1.getId(), cardRepository.findCardBy("TESTBRD-" + c1.getSequence(), null).get(0).getId());

		Assert.assertTrue(cardRepository.findCardBy(null, null).isEmpty());


	}

	@Test
	public void testFindCardByInProject() {
		Card c1 = cardService.createCard("card1", col1.getId(), new Date(), user);

		Set<Integer> projects = Collections.singleton(board.getProjectId());

		// find by card "title"
		Assert.assertEquals(c1.getId(), cardRepository.findCardBy("card1", projects).get(0).getId());

		// find by card sequenceNr
		Assert.assertEquals(c1.getId(), cardRepository.findCardBy(Integer.toString(c1.getSequence()), projects).get(0).getId());

		// find by board short name
		Assert.assertEquals(c1.getId(), cardRepository.findCardBy("TESTBRD", projects).get(0).getId());
		Assert.assertEquals(c1.getId(), cardRepository.findCardBy("TESTBRD-", projects).get(0).getId());

		// find by board short name + seq nr
		Assert.assertEquals(c1.getId(), cardRepository.findCardBy("TESTBRD-" + c1.getSequence(), projects).get(0).getId());

		//
		Assert.assertTrue(cardRepository.findCardBy(null, Collections.<Integer>emptySet()).isEmpty());
	}


	@Test
	public void testFetchAllActivityByCardId() {
	    Card c1 = cardService.createCard("card1", col1.getId(), new Date(), user);
	    //card creation activity
	    Assert.assertEquals(1, cardRepository.fetchAllActivityByCardId(c1.getId()).size());

	    cardService.updateCardName(c1.getId(), "new name", user, new Date());

	    //card update activity
	    Assert.assertEquals(2, cardRepository.fetchAllActivityByCardId(c1.getId()).size());
	}

	@Test
	public void testFetchPaginatedByBoardIdAndLocation() {
	    for(int i = 0; i<11;i++) {
	        cardService.createCard("card1", col1.getId(), new Date(), user);
	    }

	    Assert.assertEquals(11, cardRepository.fetchPaginatedByBoardIdAndLocation(board.getId(), BoardColumnLocation.BOARD, 0).size());
	    Assert.assertEquals(1, cardRepository.fetchPaginatedByBoardIdAndLocation(board.getId(), BoardColumnLocation.BOARD, 1).size());
	}
}
