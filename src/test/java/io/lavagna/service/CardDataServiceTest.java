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
import io.lavagna.service.config.TestServiceConfig;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.*;

import static java.util.EnumSet.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

//TODO, FIXME: wall of copy paste, what will your oshi say about that?
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestServiceConfig.class, PersistenceAndServiceConfig.class })
@Transactional
public class CardDataServiceTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardColumnRepository boardColumnRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CardService cardService;

    @Autowired
    private CardDataService cardDataService;

    @Autowired
    private CardDataRepository cardDataRepo;

    private Board board;

    private BoardColumn col1;

    private Card card1;

    private User user;

    private static InputStream from(String s) throws UnsupportedEncodingException {
        return new ByteArrayInputStream(s.getBytes("UTF-8"));
    }

    @Before
    public void prepare() {
        Helper.createUser(userRepository, "test", "test-user");
        user = userRepository.findUserByName("test", "test-user");

        Project project = projectService.create("test", "TEST", "desc");
        board = boardRepository.createNewBoard("test-board", "TEST-BRD", null, project.getId());

        List<BoardColumnDefinition> definitions = projectService.findColumnDefinitionsByProjectId(project.getId());
        boardColumnRepository.addColumnToBoard("col1", definitions.get(0).getId(), BoardColumnLocation.BOARD,
            board.getId());
        List<BoardColumn> cols = boardColumnRepository.findAllColumnsFor(board.getId(), BoardColumnLocation.BOARD);
        col1 = cols.get(0);
        cardService.createCard("card1", col1.getId(), new Date(), user);
        List<CardFull> cards = cardRepository.findAllByColumnId(col1.getId());
        card1 = cards.get(0);
    }

    @Test
    public void findByIds() {
        cardDataService.createComment(card1.getId(), "hello world1", new Date(), user.getId());
        cardDataService.createComment(card1.getId(), "hello world2", new Date(), user.getId());
        List<CardData> created = cardDataRepo.findAllDataLightByCardIdAndType(card1.getId(), CardType.COMMENT);
        assertTrue(created.size() == 2);
        Map<Integer, String> resById = cardDataRepo.findDataByIds(Arrays.asList(created.get(0).getId(), created.get(1)
            .getId()));
        assertEquals(created.get(0).getContent(), resById.get(created.get(0).getId()));
        assertEquals(created.get(1).getContent(), resById.get(created.get(1).getId()));
    }

    @Test
    public void TestUpdateDescription() {
        assertTrue(cardDataService.findAllCommentsByCardId(card1.getId()).isEmpty());
        // when updating a card that has no description, a fresh one is created
        String newCardDescription = "test-update-description";
        cardDataService.updateDescription(card1.getId(), newCardDescription, DateUtils.addHours(new Date(), -3),
            user.getId());
        assertEquals(cardDataService.findDescriptionByCardId(card1.getId()).size(), 1);
        assertEquals(newCardDescription, cardDataService.findLatestDescriptionByCardId(card1.getId()).getContent());

        List<CardData> cardData = cardDataRepo.findAllDataLightByCardIdAndType(card1.getId(), CardType.DESCRIPTION);
        assertEquals(1, cardData.size());
        CardData cardData1 = cardDataRepo.getUndeletedDataLightById(cardData.get(0).getId());
        assertEquals(CardType.DESCRIPTION, cardData1.getType());

        cardDataService.updateDescription(card1.getId(), "test-update-description-after-update", new Date(),
            user.getId());
        List<CardData> updatedCardData = cardDataRepo.findAllDataLightByCardId(card1.getId());
        assertEquals(2, updatedCardData.size());
        assertEquals(1, cardDataRepo.findAllDataLightByCardIdAndType(card1.getId(), CardType.DESCRIPTION_HISTORY)
            .size());
        assertEquals(1, cardDataRepo.findAllDataLightByCardIdAndType(card1.getId(), CardType.DESCRIPTION).size());

        assertEquals(cardData1.getId(), updatedCardData.get(0).getId());
        assertEquals("test-update-description-after-update", updatedCardData.get(0).getContent());

        assertEquals("test-update-description-after-update", cardDataService.findLatestDescriptionByCardId(card1.getId()).getContent());
    }

    @Test
    public void TestCreateCommentAndFetch() {
        assertTrue(cardDataService.findAllCommentsByCardId(card1.getId()).isEmpty());

        assertTrue(cardDataRepo.findCountsByBoardIdAndLocation(board.getId(), BoardColumnLocation.BOARD).isEmpty());

        assertEquals("test-comment", cardDataService.createComment(card1.getId(), "test-comment", new Date(),
            user.getId())
            .getContent());
        assertEquals(cardDataService.findAllCommentsByCardId(card1.getId()).size(), 1);

        assertEquals(1, cardDataRepo.findCountsByBoardIdAndLocation(board.getId(), BoardColumnLocation.BOARD).size());

        List<CardData> cardData = cardDataRepo.findAllDataLightByCardId(card1.getId());
        assertEquals(1, cardData.size());
        CardData cardData1 = cardDataRepo.getUndeletedDataLightById(cardData.get(0).getId());
        assertEquals(cardData1.getId(), cardData.get(0).getId());
        assertEquals(cardData1.getContent(), cardData.get(0).getContent());
        assertEquals(cardData1.getType(), cardData.get(0).getType());
    }

    @Test
    public void TestUpdateComment() {
        assertTrue(cardDataService.findAllCommentsByCardId(card1.getId()).isEmpty());
        assertEquals("test-update-comment",
            cardDataService.createComment(card1.getId(), "test-update-comment", new Date(), user.getId()).getContent());
        assertEquals(cardDataService.findAllCommentsByCardId(card1.getId()).size(), 1);

        List<CardData> cardData = cardDataRepo.findAllDataLightByCardId(card1.getId());
        assertEquals(1, cardData.size());
        CardData cardData1 = cardDataRepo.getUndeletedDataLightById(cardData.get(0).getId());

        cardDataService.updateComment(cardData1.getId(), "test-update-comment-after-update", new Date(), user);
        List<CardData> updatedCardData = cardDataRepo.findAllDataLightByCardId(card1.getId());
        assertEquals(2, updatedCardData.size());
        assertEquals(1, cardDataRepo.findAllDataLightByCardIdAndType(card1.getId(), CardType.COMMENT_HISTORY).size());
        assertEquals(1, cardDataRepo.findAllDataLightByCardIdAndType(card1.getId(), CardType.COMMENT).size());

        assertEquals(cardData1.getId(), updatedCardData.get(0).getId());
        assertEquals("test-update-comment-after-update", updatedCardData.get(0).getContent());
    }

    @Test
    public void TestDeleteCommentAndUndo() {
        assertTrue(cardDataService.findAllCommentsByCardId(card1.getId()).isEmpty());
        assertTrue(cardDataRepo.findCountsByBoardIdAndLocation(board.getId(), BoardColumnLocation.BOARD).isEmpty());

        assertEquals("test-update-comment",
            cardDataService.createComment(card1.getId(), "test-update-comment", new Date(), user.getId()).getContent());
        assertEquals(cardDataService.findAllCommentsByCardId(card1.getId()).size(), 1);
        assertEquals(1, cardDataRepo.findCountsByBoardIdAndLocation(board.getId(), BoardColumnLocation.BOARD).size());

        List<CardData> cardData = cardDataRepo.findAllDataLightByCardId(card1.getId());
        assertEquals(1, cardData.size());
        CardData cardData1 = cardDataRepo.getUndeletedDataLightById(cardData.get(0).getId());

        Event deleteCommentEvent = cardDataService.deleteComment(cardData1.getId(), user, new Date());
        assertTrue(cardDataService.findAllCommentsByCardId(card1.getId()).isEmpty());
        assertTrue(cardDataRepo.findCountsByBoardIdAndLocation(board.getId(), BoardColumnLocation.BOARD).isEmpty());

        cardDataService.undoDeleteComment(deleteCommentEvent);

        assertEquals(1, cardDataService.findAllCommentsByCardId(card1.getId()).size());
        assertEquals(1, cardDataRepo.findCountsByBoardIdAndLocation(board.getId(), BoardColumnLocation.BOARD).size());
    }

    @Test
    public void TestActionListAndItems() {
        assertTrue(cardDataService.findAllActionListsAndItemsByCardId(card1.getId()).isEmpty());

        assertEquals("test-list", cardDataService.createActionList(card1.getId(), "test-list", user.getId(), new Date())
            .getContent());
        assertEquals(1, cardDataRepo.findAllDataByCardIdAndType(card1.getId(), CardType.ACTION_LIST).size());

        List<CardData> cardData = cardDataRepo.findAllDataLightByCardIdAndType(card1.getId(), CardType.ACTION_LIST);
        assertEquals(1, cardData.size());
        CardData list1 = cardDataRepo.getUndeletedDataLightById(cardData.get(0).getId());
        assertEquals(list1.getId(), cardData.get(0).getId());
        assertEquals(list1.getContent(), cardData.get(0).getContent());
        assertEquals(list1.getType(), cardData.get(0).getType());

        // add action item
        assertEquals("test-unchecked",
            cardDataService.createActionItem(card1.getId(), list1.getId(), "test-unchecked", user.getId(), new Date())
                .getContent());

        cardData = cardDataRepo.findAllDataLightByCardIdAndTypes(card1.getId(),
            of(CardType.ACTION_CHECKED, CardType.ACTION_UNCHECKED));
        assertEquals(1, cardData.size());
        CardData item1 = cardDataRepo.getUndeletedDataLightById(cardData.get(0).getId());
        assertEquals(1, item1.getOrder());
        assertEquals(CardType.ACTION_UNCHECKED, item1.getType());

        assertEquals("test-order",
            cardDataService.createActionItem(card1.getId(), list1.getId(), "test-order", user.getId(), new Date())
                .getContent());

        cardData = cardDataRepo.findAllDataLightByCardIdAndTypes(card1.getId(),
            of(CardType.ACTION_CHECKED, CardType.ACTION_UNCHECKED));
        assertEquals(2, cardData.size());
        CardData item2 = cardDataRepo.getUndeletedDataLightById(cardData.get(1).getId());
        assertEquals(2, item2.getOrder());
    }

    @Test
    public void TestUpdateActionList() {
        assertTrue(cardDataService.findAllActionListsAndItemsByCardId(card1.getId()).isEmpty());

        assertEquals("test-list-update",
            cardDataService.createActionList(card1.getId(), "test-list-update", user.getId(), new Date()).getContent());
        assertEquals(1, cardDataRepo.findAllDataByCardIdAndType(card1.getId(), CardType.ACTION_LIST).size());

        List<CardData> cardData = cardDataRepo.findAllDataLightByCardIdAndType(card1.getId(), CardType.ACTION_LIST);
        assertEquals(1, cardData.size());
        CardData list1 = cardDataRepo.getUndeletedDataLightById(cardData.get(0).getId());
        assertEquals(list1.getId(), cardData.get(0).getId());
        assertEquals(list1.getContent(), cardData.get(0).getContent());
        assertEquals(list1.getType(), cardData.get(0).getType());

        cardDataService.updateActionList(list1.getId(), "test-list-update-new");

        CardData list1updated = cardDataRepo.getUndeletedDataLightById(cardData.get(0).getId());
        assertEquals("test-list-update-new", list1updated.getContent());
    }

    @Test
    public void TestToggleActionItem() {
        assertTrue(cardDataService.findAllActionListsAndItemsByCardId(card1.getId()).isEmpty());

        assertEquals("test-list-toggle",
            cardDataService.createActionList(card1.getId(), "test-list-toggle", user.getId(), new Date()).getContent());
        assertEquals(1, cardDataRepo.findAllDataByCardIdAndType(card1.getId(), CardType.ACTION_LIST).size());

        List<CardData> cardData = cardDataRepo.findAllDataLightByCardIdAndType(card1.getId(), CardType.ACTION_LIST);
        assertEquals(1, cardData.size());
        CardData list1 = cardDataRepo.getUndeletedDataLightById(cardData.get(0).getId());
        assertEquals(list1.getId(), cardData.get(0).getId());
        assertEquals(list1.getContent(), cardData.get(0).getContent());
        assertEquals(list1.getType(), cardData.get(0).getType());

        // add action item
        assertEquals(
            "test-toggle-unchecked",
            cardDataService.createActionItem(card1.getId(), list1.getId(), "test-toggle-unchecked", user.getId(),
                new Date()).getContent());

        cardData = cardDataRepo.findAllDataLightByCardIdAndTypes(card1.getId(),
            of(CardType.ACTION_CHECKED, CardType.ACTION_UNCHECKED));
        assertEquals(1, cardData.size());
        CardData item1 = cardDataRepo.getUndeletedDataLightById(cardData.get(0).getId());

        cardDataService.toggleActionItem(item1.getId(), true, user.getId(), new Date());
        item1 = cardDataRepo.getUndeletedDataLightById(item1.getId());
        assertEquals(CardType.ACTION_CHECKED, item1.getType());
        cardData = cardDataRepo.findAllDataLightByCardIdAndTypes(card1.getId(),
            of(CardType.ACTION_CHECKED, CardType.ACTION_UNCHECKED));
        assertEquals(1, cardData.size());

        cardDataService.toggleActionItem(item1.getId(), false, user.getId(), new Date());
        item1 = cardDataRepo.getUndeletedDataLightById(item1.getId());
        assertEquals(CardType.ACTION_UNCHECKED, item1.getType());
    }

    @Test
    public void TestUpdateActionItem() {
        assertTrue(cardDataService.findAllActionListsAndItemsByCardId(card1.getId()).isEmpty());

        assertEquals("test-list-update",
            cardDataService.createActionList(card1.getId(), "test-list-update", user.getId(), new Date()).getContent());
        assertEquals(1, cardDataRepo.findAllDataLightByCardIdAndType(card1.getId(), CardType.ACTION_LIST).size());

        List<CardData> cardData = cardDataRepo.findAllDataLightByCardIdAndType(card1.getId(), CardType.ACTION_LIST);
        assertEquals(1, cardData.size());
        CardData list1 = cardDataRepo.getUndeletedDataLightById(cardData.get(0).getId());

        // add action item
        assertEquals("test-update",
            cardDataService.createActionItem(card1.getId(), list1.getId(), "test-update", user.getId(), new Date())
                .getContent());

        cardData = cardDataRepo.findAllDataLightByCardIdAndTypes(card1.getId(),
            of(CardType.ACTION_CHECKED, CardType.ACTION_UNCHECKED));
        assertEquals(1, cardData.size());
        CardData item1 = cardDataRepo.getUndeletedDataLightById(cardData.get(0).getId());

        cardDataService.updateActionItem(item1.getId(), "test-update-after");
        cardData = cardDataRepo.findAllDataLightByCardIdAndTypes(card1.getId(),
            of(CardType.ACTION_CHECKED, CardType.ACTION_UNCHECKED));
        assertEquals(1, cardData.size());
        assertEquals(item1.getId(), cardData.get(0).getId());
        assertEquals("test-update-after", cardData.get(0).getContent());

    }

    @Test
    public void TestDeleteActionItemAndUndo() {
        assertTrue(cardDataService.findAllActionListsAndItemsByCardId(card1.getId()).isEmpty());
        assertEquals("test-list-delete",
            cardDataService.createActionList(card1.getId(), "test-list-delete", user.getId(), new Date()).getContent());
        assertEquals(1, cardDataRepo.findAllDataLightByCardIdAndType(card1.getId(), CardType.ACTION_LIST).size());

        List<CardData> cardData = cardDataRepo.findAllDataLightByCardIdAndType(card1.getId(), CardType.ACTION_LIST);
        assertEquals(1, cardData.size());
        CardData list1 = cardDataRepo.getUndeletedDataLightById(cardData.get(0).getId());

        // add action item
        assertEquals("test-delete",
            cardDataService.createActionItem(card1.getId(), list1.getId(), "test-delete", user.getId(), new Date())
                .getContent());

        cardData = cardDataRepo.findAllDataLightByCardIdAndTypes(card1.getId(),
            of(CardType.ACTION_CHECKED, CardType.ACTION_UNCHECKED));
        assertEquals(1, cardData.size());
        CardData item1 = cardDataRepo.getUndeletedDataLightById(cardData.get(0).getId());

        Event deleteActionItemEvent = cardDataService.deleteActionItem(item1.getId(), user, new Date());
        cardData = cardDataRepo.findAllDataLightByCardIdAndTypes(card1.getId(),
            of(CardType.ACTION_CHECKED, CardType.ACTION_UNCHECKED));
        assertEquals(0, cardData.size());

        cardDataService.undoDeleteActionItem(deleteActionItemEvent);

        cardData = cardDataRepo.findAllDataLightByCardIdAndTypes(card1.getId(),
            of(CardType.ACTION_CHECKED, CardType.ACTION_UNCHECKED));
        assertEquals(1, cardData.size());
    }

    @Test
    public void TestReorderActionLists() {
        assertTrue(cardDataService.findAllActionListsAndItemsByCardId(card1.getId()).isEmpty());

        assertEquals("test-list-reorder1",
            cardDataService.createActionList(card1.getId(), "test-list-reorder1", user.getId(),
                new Date()).getContent());
        assertEquals(1, cardDataRepo.findAllDataLightByCardIdAndType(card1.getId(), CardType.ACTION_LIST).size());
        assertEquals("test-list-reorder2",
            cardDataService.createActionList(card1.getId(), "test-list-reorder2", user.getId(),
                new Date()).getContent());
        assertEquals(2, cardDataRepo.findAllDataLightByCardIdAndType(card1.getId(), CardType.ACTION_LIST).size());

        List<CardData> cardData = cardDataRepo.findAllDataLightByCardIdAndType(card1.getId(), CardType.ACTION_LIST);
        assertEquals(2, cardData.size());
        CardData list1 = cardDataRepo.getUndeletedDataLightById(cardData.get(0).getId());
        assertEquals(1, list1.getOrder());

        CardData list2 = cardDataRepo.getUndeletedDataLightById(cardData.get(1).getId());
        assertEquals(2, list2.getOrder());

        List<Integer> newOrder = Arrays.asList(list2.getId(), list1.getId());
        cardDataRepo.updateActionListOrder(card1.getId(), newOrder);

        CardData list1reorder = cardDataRepo.getUndeletedDataLightById(cardData.get(0).getId());
        assertEquals("test-list-reorder1", list1reorder.getContent());
        assertEquals(2, list1reorder.getOrder());

        CardData list2reorder = cardDataRepo.getUndeletedDataLightById(cardData.get(1).getId());
        assertEquals("test-list-reorder2", list2reorder.getContent());
        assertEquals(1, list2reorder.getOrder());
    }

    @Test
    public void TestReorderActionList() {
        int numberOfActionItems = 3;

        assertTrue(numberOfActionItems > 0);

        assertTrue(cardDataService.findAllActionListsAndItemsByCardId(card1.getId()).isEmpty());
        assertEquals("test-list-reorder1",
            cardDataService.createActionList(card1.getId(), "test-list-reorder1", user.getId(),
                new Date()).getContent());

        List<CardData> actionLists = cardDataRepo.findAllDataLightByCardIdAndType(card1.getId(), CardType.ACTION_LIST);
        assertEquals(1, actionLists.size());
        CardData list1 = cardDataRepo.getUndeletedDataLightById(actionLists.get(0).getId());

        // add action item
        for (int i = 1; i < numberOfActionItems + 1; i++) {
            assertEquals(i,
                cardDataService.createActionItem(card1.getId(), list1.getId(), "test-order-" + i, user.getId(), new Date())
                    .getOrder());
        }

        List<CardData> actionItems = cardDataRepo.findAllDataLightByCardIdAndTypes(card1.getId(),
            of(CardType.ACTION_CHECKED, CardType.ACTION_UNCHECKED));
        assertEquals(3, actionItems.size());

        List<Integer> newOrder = new ArrayList<>();
        for (int i = numberOfActionItems; i > 0; i--) {
            assertEquals(i, actionItems.get(i - 1).getOrder());
            newOrder.add(actionItems.get(i - 1).getId());
        }

        cardDataRepo.updateOrderByCardAndReferenceId(card1.getId(), list1.getId(), newOrder);

        for (int i = 0; i < numberOfActionItems; i++) {
            CardData actionItem = cardDataRepo.getUndeletedDataLightById(actionItems.get(i).getId());
            assertEquals(numberOfActionItems - i, actionItem.getOrder());
        }

    }

    @Test
    public void TestMoveActionItem() {
        int numberOfActionItems = 3;

        assertTrue(numberOfActionItems > 0);

        assertTrue(cardDataService.findAllActionListsAndItemsByCardId(card1.getId()).isEmpty());
        assertEquals("test-list-reorder1",
            cardDataService.createActionList(card1.getId(), "test-list-reorder1", user.getId(),
                new Date()).getContent());
        assertEquals("test-list-reorder2",
            cardDataService.createActionList(card1.getId(), "test-list-reorder2", user.getId(),
                new Date()).getContent());

        List<CardData> actionLists = cardDataRepo.findAllDataLightByCardIdAndType(card1.getId(), CardType.ACTION_LIST);
        assertEquals(2, actionLists.size());
        CardData list1 = cardDataRepo.getUndeletedDataLightById(actionLists.get(0).getId());
        assertEquals("test-list-reorder1", list1.getContent());
        CardData list2 = cardDataRepo.getUndeletedDataLightById(actionLists.get(1).getId());
        assertEquals("test-list-reorder2", list2.getContent());

        // add action item
        for (int l = 0; l < actionLists.size(); l++) {
            for (int i = 1; i < numberOfActionItems + 1; i++) {
                assertEquals(
                    i,
                    cardDataService.createActionItem(card1.getId(), actionLists.get(l).getId(), "test-move-" + i,
                        user.getId(), new Date()).getOrder());
            }
        }

        List<CardData> actionItems = cardDataRepo.findAllDataLightByCardIdAndTypes(card1.getId(),
            of(CardType.ACTION_CHECKED, CardType.ACTION_UNCHECKED));
        assertEquals(numberOfActionItems * actionLists.size(), actionItems.size());

        List<CardData> actionList1Items = cardDataRepo.findAllDataLightByReferenceId(list1.getId());
        assertEquals(3, actionList1Items.size());
        List<CardData> actionList2Items = cardDataRepo.findAllDataLightByReferenceId(list2.getId());
        assertEquals(3, actionList2Items.size());

        // move middle item of list 2 to the beginning of list 1
        int indexToMove = Math.round(numberOfActionItems / 2);

        CardData itemToMove = actionList2Items.get(indexToMove);

        // create destination list IDs
        List<Integer> newActionListOrder = new ArrayList<>();
        newActionListOrder.add(itemToMove.getId());
        for (int i = 0; i < actionList1Items.size(); i++) {
            newActionListOrder.add(actionList1Items.get(i).getId());
        }

        cardDataService.moveActionItem(card1.getId(), itemToMove.getId(), list1.getId(), newActionListOrder, user,
            new Date());

        List<CardData> actionList1ItemsReOrdered = cardDataRepo.findAllDataLightByReferenceId(list1.getId());
        assertEquals(4, actionList1ItemsReOrdered.size());
        assertEquals(itemToMove.getContent(), actionList1ItemsReOrdered.get(0).getContent());

        List<CardData> actionList2ItemsReOrdered = cardDataRepo.findAllDataLightByReferenceId(list2.getId());
        assertEquals(2, actionList2ItemsReOrdered.size());
    }

    @Test
    public void TestDeleteActionListWithContentAndUndo() {
        int numberOfActionItems = 3;

        assertTrue(numberOfActionItems > 0);

        assertTrue(cardDataService.findAllActionListsAndItemsByCardId(card1.getId()).isEmpty());
        assertEquals("test-list-reorder1",
            cardDataService.createActionList(card1.getId(), "test-list-reorder1", user.getId(),
                new Date()).getContent());
        assertEquals("test-list-reorder2",
            cardDataService.createActionList(card1.getId(), "test-list-reorder2", user.getId(),
                new Date()).getContent());

        List<CardData> actionLists = cardDataRepo.findAllDataLightByCardIdAndType(card1.getId(), CardType.ACTION_LIST);
        assertEquals(2, actionLists.size());
        CardData list1 = cardDataRepo.getUndeletedDataLightById(actionLists.get(0).getId());
        assertEquals("test-list-reorder1", list1.getContent());
        CardData list2 = cardDataRepo.getUndeletedDataLightById(actionLists.get(1).getId());
        assertEquals("test-list-reorder2", list2.getContent());

        // add action item
        for (int l = 0; l < actionLists.size(); l++) {
            for (int i = 1; i < numberOfActionItems + 1; i++) {
                assertEquals(
                    i,
                    cardDataService.createActionItem(card1.getId(), actionLists.get(l).getId(), "test-move-" + i,
                        user.getId(), new Date()).getOrder());
            }
        }

        List<CardData> actionItems = cardDataRepo.findAllDataLightByCardIdAndTypes(card1.getId(),
            of(CardType.ACTION_CHECKED, CardType.ACTION_UNCHECKED));
        assertEquals(numberOfActionItems * actionLists.size(), actionItems.size());

        Event deleteActionListEvent = cardDataService.deleteActionList(list1.getId(), user, new Date());

        List<CardData> remainingActionLists = cardDataRepo.findAllDataLightByCardIdAndType(card1.getId(),
            CardType.ACTION_LIST);

        assertEquals(1, remainingActionLists.size());
        assertEquals(list2.getContent(), remainingActionLists.get(0).getContent());

        List<CardData> remainingActionItems = cardDataRepo.findAllDataLightByCardIdAndTypes(card1.getId(),
            of(CardType.ACTION_CHECKED, CardType.ACTION_UNCHECKED));

        assertEquals(3, remainingActionItems.size());

        cardDataService.undoDeleteActionList(deleteActionListEvent);

        actionItems = cardDataRepo.findAllDataLightByCardIdAndTypes(card1.getId(),
            of(CardType.ACTION_CHECKED, CardType.ACTION_UNCHECKED));
        assertEquals(numberOfActionItems * actionLists.size(), actionItems.size());
    }

    @Test
    public void TestUploadFiles() throws IOException {
        String testData = "derp";
        // InputStream input = new
        // ByteArrayInputStream(testData.getBytes("UTF-8"));
        String fileDigest = DigestUtils.sha256Hex(from(testData));
        cardDataService.createFile("test.txt", fileDigest, 4, card1.getId(), from(testData), "text", user, new Date());

        List<FileDataLight> files = cardDataRepo.findAllFilesByCardId(card1.getId());
        assertEquals(1, files.size());
        FileDataLight file = files.get(0);
        assertEquals(fileDigest, file.getDigest());
        assertEquals("test.txt", file.getName());
        assertEquals("text", file.getContentType());
        Assert.assertNull(file.getReferenceId());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        cardDataRepo.outputFileContent(fileDigest, outputStream);

        assertEquals(testData, outputStream.toString("UTF-8"));

        String testData2 = "herp";
        String fileDigest2 = DigestUtils.sha256Hex(from(testData2));
        cardDataService.createFile("test2.txt", fileDigest2, 4, card1.getId(), from(testData2), "text", user,
            new Date());

        files = cardDataRepo.findAllFilesByCardId(card1.getId());
        assertEquals(2, files.size());
        FileDataLight file2 = files.get(1);
        assertEquals(fileDigest2, file2.getDigest());
        assertEquals("test2.txt", file2.getName());
        assertEquals("text", file2.getContentType());
        Assert.assertNull(file2.getReferenceId());

        ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();

        cardDataRepo.outputFileContent(fileDigest2, outputStream2);

        assertEquals(testData2, outputStream2.toString("UTF-8"));

        FileDataLight fileData = cardDataRepo.getUndeletedFileByCardDataId(file.getCardDataId());
        assertEquals(file.getContentType(), fileData.getContentType());
        assertEquals(file.getDigest(), fileData.getDigest());
        assertEquals(file.getName(), fileData.getName());
    }

    @Test
    public void TestUploadSameFileToCard() throws IOException {
        String testData = "derp";
        String fileDigest = DigestUtils.sha256Hex(from(testData));
        cardDataService.createFile("test.txt", fileDigest, 4, card1.getId(), from(testData), "text", user, new Date());

        List<FileDataLight> files = cardDataRepo.findAllFilesByCardId(card1.getId());
        assertEquals(1, files.size());
        FileDataLight file = files.get(0);
        assertEquals(fileDigest, file.getDigest());
        assertEquals("test.txt", file.getName());
        assertEquals("text", file.getContentType());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        cardDataRepo.outputFileContent(fileDigest, outputStream);
        assertEquals(testData, outputStream.toString("UTF-8"));

        // without reference
        String fileDigest2 = DigestUtils.sha256Hex(from(testData));
        assertEquals(fileDigest, fileDigest2);
        cardDataService
            .createFile("test2.txt", fileDigest2, 4, card1.getId(), from(testData), "text", user, new Date());

        // with reference
        String fileDigest3 = DigestUtils.sha256Hex(from(testData));
        assertEquals(fileDigest, fileDigest3);
        cardDataService
            .createFile("test3.txt", fileDigest3, 4, card1.getId(), from(testData), "text", user, new Date());

        files = cardDataRepo.findAllFilesByCardId(card1.getId());
        assertEquals(1, files.size());
    }

    @Test
    public void TestDeleteFilesAndUndo() throws IOException {
        String testData = "derp";
        String fileDigest = DigestUtils.sha256Hex(from(testData));
        cardDataService.createFile("test.txt", fileDigest, 4, card1.getId(), from(testData), "text", user, new Date());

        List<FileDataLight> files = cardDataRepo.findAllFilesByCardId(card1.getId());
        assertEquals(1, files.size());
        FileDataLight file = files.get(0);
        assertEquals(fileDigest, file.getDigest());
        assertEquals("test.txt", file.getName());
        assertEquals("text", file.getContentType());
        Assert.assertNull(file.getReferenceId());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        cardDataRepo.outputFileContent(fileDigest, outputStream);
        assertEquals(testData, outputStream.toString("UTF-8"));

        Event deleteFileEvent = cardDataService.deleteFile(file.getCardDataId(), user, new Date());

        List<FileDataLight> files2 = cardDataRepo.findAllFilesByCardId(card1.getId());
        assertEquals(0, files2.size());

        cardDataService.undoDeleteFile(deleteFileEvent);

        files2 = cardDataRepo.findAllFilesByCardId(card1.getId());
        assertEquals(1, files2.size());
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testCheckEnsureReferenceIdConstraints() {
        CardData list = cardDataService.createActionList(card1.getId(), "name", user.getId(), new Date());
        cardDataRepo.createDataWithReferenceOrder(card1.getId(), list.getId(), CardType.COMMENT, "body");
    }

    @Test
    public void createFileWithoutCard() throws IOException {
        String testData = "derp";
        String fileDigest = DigestUtils.sha256Hex(from(testData));

        cardDataService.createFile(fileDigest, 4, from(testData), "text");

        assertTrue(cardDataRepo.fileExists(fileDigest));
    }

    @Test
    public void assignFileToCard() throws IOException {
        String testData = "derp";
        String fileDigest = DigestUtils.sha256Hex(from(testData));

        cardDataService.createFile(fileDigest, 4, from(testData), "text");

        assertTrue(cardDataRepo.fileExists(fileDigest));

        ImmutablePair<Boolean, CardData> result = cardDataService.assignFileToCard("test.txt", fileDigest, card1.getId(), user, new Date());

        assertTrue(result.getLeft());

        List<FileDataLight> files = cardDataRepo.findAllFilesByCardId(card1.getId());
        assertEquals(1, files.size());
        FileDataLight file = files.get(0);
        assertEquals(fileDigest, file.getDigest());
        assertEquals("test.txt", file.getName());
        assertEquals("text", file.getContentType());
        Assert.assertNull(file.getReferenceId());
    }

    @Test
    public void assignSameFileToCard() throws IOException {
        String testData = "derp";
        String fileDigest = DigestUtils.sha256Hex(from(testData));

        cardDataService.createFile(fileDigest, 4, from(testData), "text");

        assertTrue(cardDataRepo.fileExists(fileDigest));

        ImmutablePair<Boolean, CardData> result = cardDataService.assignFileToCard("test.txt", fileDigest, card1.getId(), user, new Date());

        assertTrue(result.getLeft());

        List<FileDataLight> files = cardDataRepo.findAllFilesByCardId(card1.getId());
        assertEquals(1, files.size());
        FileDataLight file = files.get(0);
        assertEquals(fileDigest, file.getDigest());
        assertEquals("test.txt", file.getName());
        assertEquals("text", file.getContentType());
        Assert.assertNull(file.getReferenceId());

        ImmutablePair<Boolean, CardData> result2 = cardDataService.assignFileToCard("test.txt", fileDigest, card1.getId(), user, new Date());

        assertEquals(false, result2.getLeft());

        List<FileDataLight> files2 = cardDataRepo.findAllFilesByCardId(card1.getId());
        assertEquals(1, files2.size());
        FileDataLight file2 = files.get(0);
        assertEquals(fileDigest, file2.getDigest());
        assertEquals("test.txt", file2.getName());
        assertEquals("text", file2.getContentType());
        Assert.assertNull(file2.getReferenceId());
    }
}
