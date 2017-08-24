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
package io.lavagna.web.api;

import io.lavagna.model.*;
import io.lavagna.model.BoardColumn.BoardColumnLocation;
import io.lavagna.service.*;
import io.lavagna.web.api.CardController.CardData;
import io.lavagna.web.api.CardController.ColumnOrders;
import io.lavagna.web.api.model.BulkOperation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

//TODO complete with verify
@RunWith(MockitoJUnitRunner.class)
public class CardControllerTest {

	private final int columnId = 0;
	@Mock
	private CardService cardService;
	@Mock
	private CardRepository cardRepository;
	@Mock
	private CardDataRepository cardDataRepository;
	@Mock
    private CardDataService cardDataService;
	@Mock
	private BoardRepository boardRepository;
	@Mock
    private BulkOperationService bulkOperationService;
	@Mock
	private ProjectService projectService;
	@Mock
	private BoardColumnRepository boardColumnRepository;
	@Mock
	private SearchService searchService;
	@Mock
	private EventEmitter eventEmitter;
	@Mock
	private Card card;
	@Mock
	private BoardColumn boardColumn;
	@Mock
	private Project project;
	@Mock
	private Board board;

	private CardController cardController;
	@Mock
	private UserWithPermission user;

	private ProjectAndBoard pab;
    private int userId;
    private int cardId;
    private String projectShortName;
    private List<Integer> cardIds;

	@Before
	public void prepare() {
		cardController = new CardController(cardRepository, cardService, cardDataService, boardRepository,
				bulkOperationService, projectService, boardColumnRepository, searchService, eventEmitter);

		pab = new ProjectAndBoard(project.getId(), project.getShortName(), project.getName(),
				project.getDescription(), project.getArchived(), board.getId(), board.getShortName(), board.getName(),
				board.getDescription(), board.getArchived());
		when(boardRepository.findProjectAndBoardByColumnId(boardColumn.getId())).thenReturn(pab);

		userId = user.getId();
		cardId = card.getId();
		projectShortName = project.getShortName();
        cardIds = Collections.singletonList(card.getId());
	}

	@Test
	public void create() {
		CardData cardData = new CardData();
		cardData.setName("name");

		when(cardService.createCard(eq("name"), eq(columnId), any(Date.class), eq(user))).thenReturn(card);

		cardController.create(columnId, cardData, user);

		verify(cardService).createCard(eq("name"), eq(columnId), any(Date.class), eq(user));
		verify(eventEmitter).emitCreateCard(project.getShortName(), board.getShortName(), boardColumn.getId(),
				card, user);
        verify(cardDataService, never()).updateDescription(eq(cardId),
            anyString(),
            any(Date.class),
            eq(userId));
        verify(bulkOperationService, never()).addUserLabel(eq(projectShortName),
            anyInt(),
            any(CardLabelValue.LabelValue.class),
            ArgumentMatchers.<Integer>anyList(),
            eq(user));
        verify(bulkOperationService, never()).setDueDate(eq(projectShortName),
            ArgumentMatchers.<Integer>anyList(),
            any(CardLabelValue.LabelValue.class),
            eq(user));
        verify(bulkOperationService, never()).setMilestone(eq(projectShortName),
            ArgumentMatchers.<Integer>anyList(),
            any(CardLabelValue.LabelValue.class),
            eq(user));
        verify(bulkOperationService, never()).assign(eq(projectShortName),
            ArgumentMatchers.<Integer>anyList(),
            any(CardLabelValue.LabelValue.class),
            eq(user));
        verify(cardDataService, never()).assignFileToCard(anyString(),
            anyString(),
            anyInt(),
            eq(user),
            any(Date.class));
	}

	@Test
    public void createWithDescription() {
        CardData cardData = new CardData();
        cardData.setName("name");
        cardData.setDescription("description");

        when(cardService.createCard(eq("name"), eq(columnId), any(Date.class), eq(user))).thenReturn(card);
        when(cardDataService.updateDescription(anyInt(), anyString(), any(Date.class), anyInt())).thenReturn(1);

        cardController.create(columnId, cardData, user);

        verify(cardService).createCard(eq("name"), eq(columnId), any(Date.class), eq(user));
        verify(cardDataService).updateDescription(eq(cardId), eq("description"), any(Date.class), eq(userId));
        verify(eventEmitter).emitCreateCard(project.getShortName(), board.getShortName(), boardColumn.getId(),
            card, user);

        verify(bulkOperationService, never()).addUserLabel(eq(projectShortName),
            anyInt(),
            any(CardLabelValue.LabelValue.class),
            ArgumentMatchers.<Integer>anyList(),
            eq(user));
        verify(bulkOperationService, never()).setDueDate(eq(projectShortName),
            ArgumentMatchers.<Integer>anyList(),
            any(CardLabelValue.LabelValue.class),
            eq(user));
        verify(bulkOperationService, never()).setMilestone(eq(projectShortName),
            ArgumentMatchers.<Integer>anyList(),
            any(CardLabelValue.LabelValue.class),
            eq(user));
        verify(bulkOperationService, never()).assign(eq(projectShortName),
            ArgumentMatchers.<Integer>anyList(),
            any(CardLabelValue.LabelValue.class),
            eq(user));
        verify(cardDataService, never()).assignFileToCard(anyString(),
            anyString(),
            anyInt(),
            eq(user),
            any(Date.class));
    }

    @Test
    public void createWithLabels() {
        CardData cardData = new CardData();
        cardData.setName("name");

        List<BulkOperation> labels = new ArrayList<>();
        BulkOperation op1 = new BulkOperation(1, new CardLabelValue.LabelValue("test"), Collections.<Integer>emptyList());
        BulkOperation op2 = new BulkOperation(2, new CardLabelValue.LabelValue("test2"), Collections.<Integer>emptyList());

        labels.add(op1);
        labels.add(op2);

        cardData.setLabels(labels);

        Map<Permission, Permission> permissions = new HashMap<>();
        permissions.put(Permission.MANAGE_LABEL_VALUE, Permission.MANAGE_LABEL_VALUE);

        when(user.getBasePermissions()).thenReturn(permissions);
        when(cardService.createCard(eq("name"), eq(columnId), any(Date.class), eq(user))).thenReturn(card);
        when(bulkOperationService.addUserLabel(eq(projectShortName),
            any(Integer.class),
            any(CardLabelValue.LabelValue.class),
            eq(cardIds),
            eq(user))).thenReturn(cardIds);

        cardController.create(columnId, cardData, user);

        verify(cardService).createCard(eq("name"), eq(columnId), any(Date.class), eq(user));
        verify(bulkOperationService).addUserLabel(eq(projectShortName),
            eq(1),
            any(CardLabelValue.LabelValue.class),
            eq(cardIds),
            eq(user));
        verify(bulkOperationService).addUserLabel(eq(projectShortName),
            eq(2),
            any(CardLabelValue.LabelValue.class),
            eq(cardIds),
            eq(user));
        verify(eventEmitter).emitCreateCard(project.getShortName(), board.getShortName(), boardColumn.getId(),
            card, user);
        verify(cardDataService, never()).updateDescription(eq(cardId),
            anyString(),
            any(Date.class),
            eq(userId));
        verify(bulkOperationService, never()).setDueDate(eq(projectShortName),
            ArgumentMatchers.<Integer>anyList(),
            any(CardLabelValue.LabelValue.class),
            eq(user));
        verify(bulkOperationService, never()).setMilestone(eq(projectShortName),
            ArgumentMatchers.<Integer>anyList(),
            any(CardLabelValue.LabelValue.class),
            eq(user));
        verify(bulkOperationService, never()).assign(eq(projectShortName),
            ArgumentMatchers.<Integer>anyList(),
            any(CardLabelValue.LabelValue.class),
            eq(user));
        verify(cardDataService, never()).assignFileToCard(anyString(),
            anyString(),
            anyInt(),
            eq(user),
            any(Date.class));
    }

    @Test
    public void createWithDueDate() {
        CardData cardData = new CardData();
        cardData.setName("name");

        BulkOperation dueDate = new BulkOperation(1, new CardLabelValue.LabelValue(new Date()), Collections.<Integer>emptyList());

        cardData.setDueDate(dueDate);

        ImmutablePair<List<Integer>, List<Integer>> result = new ImmutablePair<>(cardIds, cardIds);

        when(cardService.createCard(eq("name"), eq(columnId), any(Date.class), eq(user))).thenReturn(card);
        when(bulkOperationService.setDueDate(eq(projectShortName),
            eq(cardIds),
            any(CardLabelValue.LabelValue.class),
            eq(user))).thenReturn(result);

        cardController.create(columnId, cardData, user);

        verify(cardService).createCard(eq("name"), eq(columnId), any(Date.class), eq(user));
        verify(eventEmitter).emitCreateCard(project.getShortName(), board.getShortName(), boardColumn.getId(),
            card, user);
        verify(bulkOperationService).setDueDate(eq(projectShortName),
            eq(cardIds),
            any(CardLabelValue.LabelValue.class),
            eq(user));

        verify(cardDataService, never()).updateDescription(eq(cardId),
            anyString(),
            any(Date.class),
            eq(userId));
        verify(bulkOperationService, never()).addUserLabel(eq(projectShortName),
            anyInt(),
            any(CardLabelValue.LabelValue.class),
            ArgumentMatchers.<Integer>anyList(),
            eq(user));
        verify(bulkOperationService, never()).setMilestone(eq(projectShortName),
            ArgumentMatchers.<Integer>anyList(),
            any(CardLabelValue.LabelValue.class),
            eq(user));
        verify(bulkOperationService, never()).assign(eq(projectShortName),
            ArgumentMatchers.<Integer>anyList(),
            any(CardLabelValue.LabelValue.class),
            eq(user));
        verify(cardDataService, never()).assignFileToCard(anyString(),
            anyString(),
            anyInt(),
            eq(user),
            any(Date.class));
    }

    @Test
    public void createWithMilestone() {
        CardData cardData = new CardData();
        cardData.setName("name");

        BulkOperation milestone = new BulkOperation(1, new CardLabelValue.LabelValue(1), Collections.<Integer>emptyList());

        cardData.setMilestone(milestone);

        ImmutablePair<List<Integer>, List<Integer>> result = new ImmutablePair<>(cardIds, cardIds);

        when(cardService.createCard(eq("name"), eq(columnId), any(Date.class), eq(user))).thenReturn(card);
        when(bulkOperationService.setMilestone(eq(projectShortName),
            eq(cardIds),
            any(CardLabelValue.LabelValue.class),
            eq(user))).thenReturn(result);

        cardController.create(columnId, cardData, user);

        verify(cardService).createCard(eq("name"), eq(columnId), any(Date.class), eq(user));
        verify(eventEmitter).emitCreateCard(project.getShortName(), board.getShortName(), boardColumn.getId(),
            card, user);
        verify(bulkOperationService).setMilestone(eq(projectShortName),
            eq(cardIds),
            any(CardLabelValue.LabelValue.class),
            eq(user));

        verify(cardDataService, never()).updateDescription(eq(cardId),
            anyString(),
            any(Date.class),
            eq(userId));
        verify(bulkOperationService, never()).addUserLabel(eq(projectShortName),
            anyInt(),
            any(CardLabelValue.LabelValue.class),
            ArgumentMatchers.<Integer>anyList(),
            eq(user));
        verify(bulkOperationService, never()).setDueDate(eq(projectShortName),
            ArgumentMatchers.<Integer>anyList(),
            any(CardLabelValue.LabelValue.class),
            eq(user));
        verify(bulkOperationService, never()).assign(eq(projectShortName),
            ArgumentMatchers.<Integer>anyList(),
            any(CardLabelValue.LabelValue.class),
            eq(user));
        verify(cardDataService, never()).assignFileToCard(anyString(),
            anyString(),
            anyInt(),
            eq(user),
            any(Date.class));
    }

    @Test
    public void createWithAssignedUsers() {
        CardData cardData = new CardData();
        cardData.setName("name");

        List<BulkOperation> users = new ArrayList<>();
        BulkOperation op1 = new BulkOperation(1, new CardLabelValue.LabelValue(1), Collections.<Integer>emptyList());
        BulkOperation op2 = new BulkOperation(1, new CardLabelValue.LabelValue(2), Collections.<Integer>emptyList());

        users.add(op1);
        users.add(op2);

        cardData.setAssignedUsers(users);

        when(cardService.createCard(eq("name"), eq(columnId), any(Date.class), eq(user))).thenReturn(card);
        when(bulkOperationService.assign(eq(projectShortName),
            eq(cardIds),
            any(CardLabelValue.LabelValue.class),
            eq(user))).thenReturn(cardIds);

        cardController.create(columnId, cardData, user);

        verify(cardService).createCard(eq("name"), eq(columnId), any(Date.class), eq(user));
        verify(eventEmitter).emitCreateCard(project.getShortName(), board.getShortName(), boardColumn.getId(),
            card, user);
        verify(bulkOperationService, times(2)).assign(eq(projectShortName),
            eq(cardIds),
            any(CardLabelValue.LabelValue.class),
            eq(user));

        verify(cardDataService, never()).updateDescription(eq(cardId),
            anyString(),
            any(Date.class),
            eq(userId));
        verify(bulkOperationService, never()).addUserLabel(eq(projectShortName),
            anyInt(),
            any(CardLabelValue.LabelValue.class),
            ArgumentMatchers.<Integer>anyList(),
            eq(user));
        verify(bulkOperationService, never()).setDueDate(eq(projectShortName),
            ArgumentMatchers.<Integer>anyList(),
            any(CardLabelValue.LabelValue.class),
            eq(user));
        verify(bulkOperationService, never()).setMilestone(eq(projectShortName),
            ArgumentMatchers.<Integer>anyList(),
            any(CardLabelValue.LabelValue.class),
            eq(user));
        verify(cardDataService, never()).assignFileToCard(anyString(),
            anyString(),
            anyInt(),
            eq(user),
            any(Date.class));
    }

    @Test
    public void createWithFiles() {
        CardData cardData = new CardData();
        cardData.setName("name");

        List<CardController.NewCardFile> files = new ArrayList<>();

        CardController.NewCardFile file1 = new CardController.NewCardFile();
        file1.setName("file.txt");
        file1.setDigest("1234");

        files.add(file1);

        cardData.setFiles(files);

        ImmutablePair<Boolean, io.lavagna.model.CardData> result = new ImmutablePair<>(true,
            new io.lavagna.model.CardData(1, cardId, null, CardType.FILE, null, 0));

        Map<Permission, Permission> permissions = new HashMap<>();
        permissions.put(Permission.CREATE_FILE, Permission.CREATE_FILE);

        when(user.getBasePermissions()).thenReturn(permissions);
        when(cardService.createCard(eq("name"), eq(columnId), any(Date.class), eq(user))).thenReturn(card);
        when(cardDataService.assignFileToCard(eq("file.txt"),
            eq("1234"),
            eq(cardId),
            eq(user),
            any(Date.class))).thenReturn(result);

        cardController.create(columnId, cardData, user);

        verify(cardService).createCard(eq("name"), eq(columnId), any(Date.class), eq(user));
        verify(eventEmitter).emitCreateCard(project.getShortName(), board.getShortName(), boardColumn.getId(),
            card, user);
        verify(cardDataService).assignFileToCard(eq("file.txt"),
            eq("1234"),
            eq(cardId),
            eq(user),
            any(Date.class));

        verify(cardDataService, never()).updateDescription(eq(cardId),
            anyString(),
            any(Date.class),
            eq(userId));
        verify(bulkOperationService, never()).addUserLabel(eq(projectShortName),
            anyInt(),
            any(CardLabelValue.LabelValue.class),
            ArgumentMatchers.<Integer>anyList(),
            eq(user));
        verify(bulkOperationService, never()).setDueDate(eq(projectShortName),
            ArgumentMatchers.<Integer>anyList(),
            any(CardLabelValue.LabelValue.class),
            eq(user));
        verify(bulkOperationService, never()).setMilestone(eq(projectShortName),
            ArgumentMatchers.<Integer>anyList(),
            any(CardLabelValue.LabelValue.class),
            eq(user));
        verify(bulkOperationService, never()).assign(eq(projectShortName),
            ArgumentMatchers.<Integer>anyList(),
            any(CardLabelValue.LabelValue.class),
            eq(user));
    }

    @Test
    public void createWithoutLabelsPermission() {
        CardData cardData = new CardData();
        cardData.setName("name");

        List<BulkOperation> labels = new ArrayList<>();
        BulkOperation op1 = new BulkOperation(1, new CardLabelValue.LabelValue("test"), Collections.<Integer>emptyList());
        BulkOperation op2 = new BulkOperation(2, new CardLabelValue.LabelValue("test2"), Collections.<Integer>emptyList());

        labels.add(op1);
        labels.add(op2);

        cardData.setLabels(labels);

        Map<Permission, Permission> permissions = new HashMap<>();

        when(user.getBasePermissions()).thenReturn(permissions);
        when(cardService.createCard(eq("name"), eq(columnId), any(Date.class), eq(user))).thenReturn(card);

        cardController.create(columnId, cardData, user);

        verify(cardService).createCard(eq("name"), eq(columnId), any(Date.class), eq(user));
        verify(eventEmitter).emitCreateCard(project.getShortName(), board.getShortName(), boardColumn.getId(),
            card, user);

        verify(cardDataService, never()).updateDescription(eq(cardId),
            anyString(),
            any(Date.class),
            eq(userId));
        verify(bulkOperationService, never()).addUserLabel(eq(projectShortName),
            anyInt(),
            any(CardLabelValue.LabelValue.class),
            ArgumentMatchers.<Integer>anyList(),
            eq(user));
        verify(bulkOperationService, never()).setDueDate(eq(projectShortName),
            ArgumentMatchers.<Integer>anyList(),
            any(CardLabelValue.LabelValue.class),
            eq(user));
        verify(bulkOperationService, never()).setMilestone(eq(projectShortName),
            ArgumentMatchers.<Integer>anyList(),
            any(CardLabelValue.LabelValue.class),
            eq(user));
        verify(bulkOperationService, never()).assign(eq(projectShortName),
            ArgumentMatchers.<Integer>anyList(),
            any(CardLabelValue.LabelValue.class),
            eq(user));
        verify(cardDataService, never()).assignFileToCard(anyString(),
            anyString(),
            anyInt(),
            eq(user),
            any(Date.class));
    }

    @Test
    public void createWithoutFilesPermission() {
        CardData cardData = new CardData();
        cardData.setName("name");

        List<CardController.NewCardFile> files = new ArrayList<>();

        CardController.NewCardFile file1 = new CardController.NewCardFile();
        file1.setName("file.txt");
        file1.setDigest("1234");

        files.add(file1);

        cardData.setFiles(files);

        Map<Permission, Permission> permissions = new HashMap<>();

        when(user.getBasePermissions()).thenReturn(permissions);
        when(cardService.createCard(eq("name"), eq(columnId), any(Date.class), eq(user))).thenReturn(card);

        cardController.create(columnId, cardData, user);

        verify(cardService).createCard(eq("name"), eq(columnId), any(Date.class), eq(user));
        verify(eventEmitter).emitCreateCard(project.getShortName(), board.getShortName(), boardColumn.getId(),
            card, user);

        verify(cardDataService, never()).updateDescription(eq(cardId),
            anyString(),
            any(Date.class),
            eq(userId));
        verify(bulkOperationService, never()).addUserLabel(eq(projectShortName),
            anyInt(),
            any(CardLabelValue.LabelValue.class),
            ArgumentMatchers.<Integer>anyList(),
            eq(user));
        verify(bulkOperationService, never()).setDueDate(eq(projectShortName),
            ArgumentMatchers.<Integer>anyList(),
            any(CardLabelValue.LabelValue.class),
            eq(user));
        verify(bulkOperationService, never()).setMilestone(eq(projectShortName),
            ArgumentMatchers.<Integer>anyList(),
            any(CardLabelValue.LabelValue.class),
            eq(user));
        verify(bulkOperationService, never()).assign(eq(projectShortName),
            ArgumentMatchers.<Integer>anyList(),
            any(CardLabelValue.LabelValue.class),
            eq(user));
        verify(cardDataService, never()).assignFileToCard(anyString(),
            anyString(),
            anyInt(),
            eq(user),
            any(Date.class));
    }

	@Test
	public void createFromTop() {
		CardData cardData = new CardData();
		cardData.setName("name");

		when(cardService.createCardFromTop(eq("name"), eq(columnId), any(Date.class), eq(user))).thenReturn(card);

		cardController.createCardFromTop(columnId, cardData, user);

		verify(cardService).createCardFromTop(eq("name"), eq(columnId), any(Date.class), eq(user));
		verify(eventEmitter).emitCreateCard(project.getShortName(), board.getShortName(), boardColumn.getId(),
				card, user);

	}

	@Test
	public void findCardById() {
		cardController.findCardById(0);
	}

	@Test
	public void findCardIdByBoardNameAndSeq() {
		cardController.findCardIdByBoardNameAndSeq("", 0);
	}

	@Test
	public void testFindCardIdByBoardNameAndSeq() {
		cardController.findCardIdByBoardNameAndSeq("TEST", 0);
	}

	@Test
	public void moveCardToColumn() {
		BoardColumn col2 = new BoardColumn(1, "name", 2, 0, BoardColumnLocation.BOARD, 0, ColumnDefinition.OPEN,
				ColumnDefinition.OPEN.getDefaultColor());
		when(boardColumnRepository.findById(0)).thenReturn(boardColumn);
		when(boardColumnRepository.findById(1)).thenReturn(col2);
		when(boardRepository.findBoardById(0)).thenReturn(board);
		when(cardRepository.findBy(0)).thenReturn(new Card(0, "name", 42, 42, 0, 42));
		ColumnOrders columnOrders = new ColumnOrders();
		cardController.moveCardToColumn(0, 0, 1, columnOrders, user);
	}

	@Test(expected = IllegalArgumentException.class)
	public void moveCardToColumnWithWrongPreviousColumnId() {
		BoardColumn col2 = new BoardColumn(1, "name", 2, 0, BoardColumnLocation.BOARD, 0, ColumnDefinition.OPEN,
				ColumnDefinition.OPEN.getDefaultColor());
		when(boardColumnRepository.findById(0)).thenReturn(boardColumn);
		when(boardColumnRepository.findById(1)).thenReturn(col2);
		when(cardRepository.findBy(0)).thenReturn(new Card(0, "name", 42, 42, 42, 42));
		ColumnOrders columnOrders = new ColumnOrders();
		cardController.moveCardToColumn(0, 0, 1, columnOrders, user);
	}

	@Test
	public void updateCard() {
		int cardId = 0;
		when(cardRepository.findBy(cardId)).thenReturn(card);

		CardData card = new CardData();
		cardController.updateCardName(cardId, card, user);
	}

	@Test
	public void updateCardOrder() {
		cardController.updateCardOrder(columnId, Arrays.<Number>asList(1, 2, 3));
	}
}
