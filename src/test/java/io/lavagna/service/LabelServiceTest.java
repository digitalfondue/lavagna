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
import io.lavagna.model.CardLabel.LabelDomain;
import io.lavagna.model.CardLabel.LabelType;
import io.lavagna.service.config.TestServiceConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestServiceConfig.class, PersistenceAndServiceConfig.class })
@Transactional
public class LabelServiceTest {

	private final int SYSTEM_LABELS = 4;

	@Autowired
	private BoardRepository boardRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BoardColumnRepository boardColumnRepository;

	@Autowired
	private CardService cardService;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private LabelService labelService;

	@Autowired
	private CardLabelRepository cardLabelRepository;

	private Project project;

	private Board board;

	private BoardColumn column;

	private User user;

	private Card card;

	@Before
	public void setUpBoard() {
		Helper.createUser(userRepository, "test", "label");
		user = userRepository.findUserByName("test", "label");
		project = projectService.create("test", "TEST", "desc");
		board = boardRepository.createNewBoard("test-label", "LABEL", "label", projectService.findByShortName("TEST")
				.getId());
		List<BoardColumnDefinition> definitions = projectService.findColumnDefinitionsByProjectId(project.getId());
		column = boardColumnRepository.addColumnToBoard("label-column", definitions.get(0).getId(),
				BoardColumnLocation.BOARD, board.getId());
		card = cardService.createCard("card", column.getId(), new Date(), user);
	}

	@Test
	public void testAddLabel() {
		Assert.assertEquals(SYSTEM_LABELS, cardLabelRepository.findLabelsByProject(project.getId()).size());
		CardLabel inserted = cardLabelRepository.addLabel(project.getId(), false, LabelType.STRING, LabelDomain.USER,
				"label1", 0);
		Assert.assertEquals(SYSTEM_LABELS + 1, cardLabelRepository.findLabelsByProject(project.getId()).size());

		CardLabel cl = cardLabelRepository.findLabelById(inserted.getId());

		Assert.assertEquals(project.getId(), cl.getProjectId());
		Assert.assertEquals(LabelType.STRING, cl.getType());
		Assert.assertEquals(LabelDomain.USER, cl.getDomain());
		Assert.assertEquals("label1", cl.getName());
		Assert.assertEquals(0, cl.getColor());
	}

	private void checkCardContainsLabelValue(Card card, CardLabel cl, int expectedLabelValues) {
		Map<CardLabel, List<CardLabelValue>> res2 = cardLabelRepository.findCardLabelValuesByCardId(card.getId());
		Map<Integer, Map<CardLabel, List<CardLabelValue>>> byBoard2 = cardLabelRepository.findCardLabelValuesByBoardId(
				board.getId(), BoardColumnLocation.BOARD);

		Assert.assertTrue(res2.containsKey(cl));
		Assert.assertEquals(expectedLabelValues, res2.get(cl).size());
		Assert.assertEquals(expectedLabelValues, byBoard2.get(card.getId()).get(cl).size());
	}

	@Test
	public void testAddLabelValueToCard() {

		Assert.assertTrue(cardLabelRepository.findCardLabelValuesByCardId(card.getId()).isEmpty());
		Assert.assertTrue(cardLabelRepository.findCardLabelValuesByBoardId(board.getId(), BoardColumnLocation.BOARD)
				.isEmpty());

		CardLabel cl = cardLabelRepository.addLabel(project.getId(), false, LabelType.STRING, LabelDomain.USER,
				"label1", 0);
		labelService.addLabelValueToCard(cl.getId(), card.getId(), new CardLabelValue.LabelValue("string"), user,
				new Date());

		checkCardContainsLabelValue(card, cl, 1);

		labelService.addLabelValueToCard(cl.getId(), card.getId(), new CardLabelValue.LabelValue("str"), user,
				new Date());

		checkCardContainsLabelValue(card, cl, 2);
	}

	@Test
	public void testAddLabelValueToCards() {
		CardLabel cl = cardLabelRepository.addLabel(project.getId(), false, LabelType.STRING, LabelDomain.USER,
				"label1", 0);

		Card card2 = cardService.createCard("card2", column.getId(), new Date(), user);

		labelService.addLabelValueToCards(cl.getId(), Arrays.asList(card.getId(), card2.getId()),
				new CardLabelValue.LabelValue("str"), user, new Date());

		checkCardContainsLabelValue(card, cl, 1);
		checkCardContainsLabelValue(card2, cl, 1);
	}

	@Test
	public void testUpdateLabelValue() {
		CardLabel cl = cardLabelRepository.addLabel(project.getId(), false, LabelType.STRING, LabelDomain.USER,
				"label1", 0);
		labelService.addLabelValueToCard(cl.getId(), card.getId(), new CardLabelValue.LabelValue("str1"), user,
				new Date());

		Map<CardLabel, List<CardLabelValue>> res1 = cardLabelRepository.findCardLabelValuesByCardId(card.getId());

		CardLabelValue clv1 = res1.get(cl).get(0);

		CardLabelValue clv1FoundById = cardLabelRepository.findLabelValueById(clv1.getCardLabelValueId());

		Assert.assertEquals("str1", clv1FoundById.getValue().getValueString());

		labelService.updateLabelValue(clv1.newValue("str2"), user, new Date());

		Map<CardLabel, List<CardLabelValue>> res2 = cardLabelRepository.findCardLabelValuesByCardId(card.getId());

		CardLabelValue clv2 = res2.get(cl).get(0);

		Assert.assertEquals("str2", clv2.getValue().getValueString());
	}

	@Test
	public void testRemoveLabelValue() {
		CardLabel cl = cardLabelRepository.addLabel(project.getId(), false, LabelType.NULL, LabelDomain.USER, "label1",
				0);
		labelService.addLabelValueToCard(cl.getId(), card.getId(), new CardLabelValue.LabelValue(), user, new Date());
		Map<CardLabel, List<CardLabelValue>> res1 = cardLabelRepository.findCardLabelValuesByCardId(card.getId());
		Assert.assertEquals(1, res1.get(cl).size());

		labelService.removeLabelValue(res1.get(cl).get(0), user, new Date());

		Map<CardLabel, List<CardLabelValue>> res2 = cardLabelRepository.findCardLabelValuesByCardId(card.getId());
		Assert.assertTrue(res2.isEmpty());
	}

	@Test(expected = DataIntegrityViolationException.class)
	public void testCardLabelEnsureTypeConstraint() {
		// mismatch between card label type and value (string, value is null)
		CardLabel cl = cardLabelRepository.addLabel(project.getId(), false, LabelType.STRING, LabelDomain.USER,
				"label1", 0);
		labelService.addLabelValueToCard(cl.getId(), card.getId(), new CardLabelValue.LabelValue(), user, new Date());
	}

	@Test
	public void testAddNonUniqueLabelValue() {
		CardLabel cl = cardLabelRepository.addLabel(project.getId(), false, LabelType.NULL, LabelDomain.USER, "label1",
				0);
		labelService.addLabelValueToCard(cl.getId(), card.getId(), new CardLabelValue.LabelValue(), user, new Date());
		labelService.addLabelValueToCard(cl.getId(), card.getId(), new CardLabelValue.LabelValue(), user, new Date());

		Map<CardLabel, List<CardLabelValue>> res2 = cardLabelRepository.findCardLabelValuesByCardId(card.getId());
		Assert.assertTrue(res2.containsKey(cl));
		Assert.assertEquals(2, res2.get(cl).size());
	}

	@Test(expected = DuplicateKeyException.class)
	public void testAddUniqueLabelValue() {
		CardLabel cl = cardLabelRepository.addLabel(project.getId(), true, LabelType.NULL, LabelDomain.USER, "label1",
				0);
		labelService.addLabelValueToCard(cl.getId(), card.getId(), new CardLabelValue.LabelValue(), user, new Date());
		Map<CardLabel, List<CardLabelValue>> res2 = cardLabelRepository.findCardLabelValuesByCardId(card.getId());
		Assert.assertTrue(res2.containsKey(cl));
		Assert.assertEquals(1, res2.get(cl).size());
		labelService.addLabelValueToCard(cl.getId(), card.getId(), new CardLabelValue.LabelValue(), user, new Date());
	}
}
