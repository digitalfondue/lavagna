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
import io.lavagna.model.Board;
import io.lavagna.model.BoardColumn;
import io.lavagna.model.BoardColumn.BoardColumnLocation;
import io.lavagna.model.BoardColumnDefinition;
import io.lavagna.model.Card;
import io.lavagna.model.CardData;
import io.lavagna.model.CardFullWithCounts;
import io.lavagna.model.CardLabel;
import io.lavagna.model.CardLabel.LabelDomain;
import io.lavagna.model.CardLabelValue.LabelValue;
import io.lavagna.model.ColumnDefinition;
import io.lavagna.model.Project;
import io.lavagna.model.User;
import io.lavagna.service.config.TestServiceConfig;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
public class CardServiceTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private BoardColumnRepository boardColumnRepository;
    @Autowired
    private LabelService labelService;
    @Autowired
    private CardLabelRepository cardLabelRepository;
    @Autowired
    private CardService cardService;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private CardDataService cardDataService;

    private User user;
    private Project project;
    private Board board;
    private BoardColumn col;
    private BoardColumn col2;
    private CardLabel assigned;
    private LabelValue labelValueToUser;

    @Before
    public void prepare() {
        userRepository.createUser("test", "test", null, null, true);
        user = userRepository.findUserByName("test", "test");
        project = projectService.create("UNITTEST", "UNITTEST", null);
        board = boardRepository.createNewBoard("test", "TEST", null, project.getId());
        List<BoardColumnDefinition> colDef = projectService.findColumnDefinitionsByProjectId(project.getId());

        BoardColumnDefinition openCol = null;
        for (BoardColumnDefinition bcd : colDef) {
            if (bcd.getValue() == ColumnDefinition.OPEN) {
                openCol = bcd;
            }
        }

        Assert.assertNotNull(openCol);

        col = boardColumnRepository.addColumnToBoard("col1", openCol.getId(), BoardColumnLocation.BOARD, board.getId());
        col2 = boardColumnRepository
            .addColumnToBoard("col2", openCol.getId(), BoardColumnLocation.BOARD, board.getId());

        assigned = cardLabelRepository.findLabelByName(project.getId(), "ASSIGNED", LabelDomain.SYSTEM);

        labelValueToUser = new LabelValue(null, null, null, null, user.getId(), null);
    }

    @Test
    public void testFetchAllInColumn() {

        Assert.assertTrue(cardService.fetchAllInColumn(col.getId()).isEmpty());

        Card c2 = cardService.createCard("2", col.getId(), new Date(), user);
        Card c3 = cardService.createCard("3", col.getId(), new Date(), user);
        Card c1 = cardService.createCardFromTop("1", col.getId(), new Date(), user);
        List<CardFullWithCounts> res = cardService.fetchAllInColumn(col.getId());

        Assert.assertEquals(c1.getId(), res.get(0).getId());
        Assert.assertEquals(c2.getId(), res.get(1).getId());
        Assert.assertEquals(c3.getId(), res.get(2).getId());
    }

    @Test
    public void testMoveCardToColumnAndReorder() {
        Card c1 = cardService.createCard("1", col2.getId(), new Date(), user);
        Card c2 = cardService.createCard("2", col.getId(), new Date(), user);
        Card c3 = cardService.createCard("3", col.getId(), new Date(), user);

        cardService.moveCardToColumnAndReorder(c1.getId(), col2.getId(), col.getId(),
            Arrays.asList(c3.getId(), c2.getId(), c1.getId()), user);

        List<CardFullWithCounts> res = cardService.fetchAllInColumn(col.getId());

        Assert.assertEquals(c1.getId(), res.get(2).getId());
        Assert.assertEquals(c2.getId(), res.get(1).getId());
        Assert.assertEquals(c3.getId(), res.get(0).getId());

    }

    @Test
    public void testUpdateCard() {
        Card c1 = cardService.createCardFromTop("1", col.getId(), new Date(), user);
        cardService.updateCard(c1.getId(), "1-new", user, new Date());
        Assert.assertEquals("1-new", cardRepository.findBy(c1.getId()).getName());
    }

    @Test
    public void testGetAllOpenCards() {
        Assert.assertEquals(0, cardService.getAllOpenCards(user, 0, 50).getTotalCards());

        Card c2 = cardService.createCard("2", col.getId(), new Date(), user);
        Card c3 = cardService.createCard("3", col.getId(), new Date(), user);

        labelService.addLabelValueToCard(assigned.getId(), c2.getId(), labelValueToUser, user, new Date());
        labelService.addLabelValueToCard(assigned.getId(), c3.getId(), labelValueToUser, user, new Date());

        Assert.assertEquals(2, cardService.getAllOpenCards(user, 0, 50).getTotalCards());

        // test pagination
        for (int i = 0; i < 10; i++) {
            Card card = cardService.createCard("2", col.getId(), new Date(), user);
            labelService.addLabelValueToCard(assigned.getId(), card.getId(), labelValueToUser, user, new Date());
        }

        Assert.assertEquals(12, cardService.getAllOpenCards(user, 0, 10).getTotalCards());
        //1 more than max page -> we know there is another page after this one
        Assert.assertEquals(11, cardService.getAllOpenCards(user, 0, 10).getCards().size());

        Assert.assertEquals(12, cardService.getAllOpenCards(user, 1, 10).getTotalCards());
        Assert.assertEquals(2, cardService.getAllOpenCards(user, 1, 10).getCards().size());
    }

    @Test
    public void testGetAllOpenCardsByProject() {
        Assert.assertEquals(0, cardService.getAllOpenCardsByProject(project.getShortName(), user, 0, 50)
            .getTotalCards());

        Card c2 = cardService.createCard("2", col.getId(), new Date(), user);
        Card c3 = cardService.createCard("3", col.getId(), new Date(), user);

        labelService.addLabelValueToCard(assigned.getId(), c2.getId(), labelValueToUser, user, new Date());
        labelService.addLabelValueToCard(assigned.getId(), c3.getId(), labelValueToUser, user, new Date());

        Assert.assertEquals(2, cardService.getAllOpenCardsByProject(project.getShortName(), user, 0, 50)
            .getTotalCards());

        // test pagination
        for (int i = 0; i < 10; i++) {
            Card card = cardService.createCard("2", col.getId(), new Date(), user);
            labelService.addLabelValueToCard(assigned.getId(), card.getId(), labelValueToUser, user, new Date());
        }

        Assert.assertEquals(12,
            cardService.getAllOpenCardsByProject(project.getShortName(), user, 0, 10).getTotalCards());
        //1 more than max page -> we know there is another page after this one
        Assert.assertEquals(11,
            cardService.getAllOpenCardsByProject(project.getShortName(), user, 0, 10).getCards().size());

        Assert.assertEquals(12,
            cardService.getAllOpenCardsByProject(project.getShortName(), user, 1, 10).getTotalCards());
        Assert.assertEquals(2,
            cardService.getAllOpenCardsByProject(project.getShortName(), user, 1, 10).getCards().size());

    }

    @Test
    public void testCloneCard() {
        Card c1 = cardService.createCardFromTop("1", col.getId(), new Date(), user);
        CardData comment = cardDataService.createComment(c1.getId(), "Comment", new Date(), user.getId());
        cardDataService.updateComment(comment.getId(), "Comment updated", new Date(), user);
        cardDataService.updateDescription(c1.getId(), "Desc", new Date(), user.getId());
        CardData list = cardDataService.createActionList(c1.getId(), "List", user.getId(), new Date());
        cardDataService.createActionItem(c1.getId(), list.getId(), "Chk1", user.getId(), new Date());
        cardDataService.createActionItem(c1.getId(), list.getId(), "Chk2", user.getId(), new Date());

        Card clone = cardService.cloneCard(c1.getId(), col.getId(), user);

        Assert.assertEquals(c1.getName(), clone.getName());
        Assert.assertNotEquals(c1.getId(), clone.getId());

        Assert.assertEquals(
            cardDataService.findLatestDescriptionByCardId(c1.getId()).getContent(),
            cardDataService.findLatestDescriptionByCardId(clone.getId()).getContent());
        Assert.assertEquals(1, cardDataService.findAllCommentsByCardId(clone.getId()).size());
    }
}
