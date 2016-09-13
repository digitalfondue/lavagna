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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.lavagna.model.Board;
import io.lavagna.model.BoardColumn;
import io.lavagna.model.BoardColumn.BoardColumnLocation;
import io.lavagna.model.Card;
import io.lavagna.model.CardLabel;
import io.lavagna.model.ColumnDefinition;
import io.lavagna.model.LabelListValueWithMetadata;
import io.lavagna.model.MilestoneCount;
import io.lavagna.model.Project;
import io.lavagna.model.ProjectAndBoard;
import io.lavagna.model.User;
import io.lavagna.service.BoardColumnRepository;
import io.lavagna.service.BoardRepository;
import io.lavagna.service.CardDataRepository;
import io.lavagna.service.CardLabelRepository;
import io.lavagna.service.CardRepository;
import io.lavagna.service.CardService;
import io.lavagna.service.EventEmitter;
import io.lavagna.service.ProjectService;
import io.lavagna.service.SearchService;
import io.lavagna.service.StatisticsService;
import io.lavagna.web.api.CardController.CardData;
import io.lavagna.web.api.CardController.ColumnOrders;
import io.lavagna.web.api.model.MilestoneInfo;
import io.lavagna.web.api.model.Milestones;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
	private CardLabelRepository cardLabelRepository;
	@Mock
	private BoardRepository boardRepository;
	@Mock
	private ProjectService projectService;
	@Mock
	private BoardColumnRepository boardColumnRepository;
	@Mock
	private StatisticsService statisticsService;
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
	private User user;

	@Before
	public void prepare() {
		cardController = new CardController(cardRepository, cardService, cardLabelRepository, boardRepository,
				projectService, boardColumnRepository, statisticsService, searchService, eventEmitter);

		ProjectAndBoard pab = new ProjectAndBoard(project.getId(), project.getShortName(), project.getName(),
				project.getDescription(), project.isArchived(), board.getId(), board.getShortName(), board.getName(),
				board.getDescription(), board.isArchived());
		when(boardRepository.findProjectAndBoardByColumnId(boardColumn.getId())).thenReturn(pab);
	}

	@Test
	public void create() {
		CardData cardData = new CardData();
		cardData.setName("name");

		when(cardService.createCard(eq("name"), eq(columnId), any(Date.class), eq(user))).thenReturn(card);

		cardController.create(columnId, cardData, user);

		verify(cardService).createCard(eq("name"), eq(columnId), any(Date.class), eq(user));
		verify(eventEmitter).emitCreateCard(project.getShortName(), board.getShortName(), boardColumn.getId(),
				card.getId());

	}

	@Test
	public void createFromTop() {
		CardData cardData = new CardData();
		cardData.setName("name");

		when(cardService.createCardFromTop(eq("name"), eq(columnId), any(Date.class), eq(user))).thenReturn(card);

		cardController.createCardFromTop(columnId, cardData, user);

		verify(cardService).createCardFromTop(eq("name"), eq(columnId), any(Date.class), eq(user));
		verify(eventEmitter).emitCreateCard(project.getShortName(), board.getShortName(), boardColumn.getId(),
				card.getId());

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
		when(boardRepository.findBoardById(0)).thenReturn(board);
		when(cardRepository.findBy(0)).thenReturn(new Card(0, "name", 42, 42, 42, 42));
		ColumnOrders columnOrders = new ColumnOrders();
		cardController.moveCardToColumn(0, 0, 1, columnOrders, user);
	}

	@Test
	public void updateCard() {
		int cardId = 0;
		when(cardRepository.findBy(cardId)).thenReturn(card);

		CardData card = new CardData();
		cardController.updateCard(cardId, card, user);
	}

	@Test
	public void updateCardOrder() {
		when(boardColumnRepository.findById(columnId)).thenReturn(boardColumn);
		cardController.updateCardOrder(columnId, Arrays.<Number>asList(1, 2, 3));
	}

	@Test
	public void testFindCardsByMilestone() {
		when(cardLabelRepository.findLabelByName(1, "MILESTONE", CardLabel.LabelDomain.SYSTEM)).thenReturn(
				new CardLabel(1, 1, true, CardLabel.LabelType.STRING, CardLabel.LabelDomain.SYSTEM, "MILESTONE", 0));
		when(projectService.findIdByShortName("TEST")).thenReturn(1);

		List<MilestoneCount> counts = new ArrayList<>();
		MilestoneCount count = new MilestoneCount(null, ColumnDefinition.OPEN, 1);
		counts.add(count);
		when(statisticsService.findCardsCountByMilestone(1)).thenReturn(counts);

		List<LabelListValueWithMetadata> listValues = new ArrayList<>();
		when(cardLabelRepository.findListValuesByLabelId(1)).thenReturn(listValues);

		Milestones cardsByMilestone = cardController.findCardsByMilestone("TEST");

		Assert.assertEquals(1, cardsByMilestone.getMilestones().size());
		MilestoneInfo md = cardsByMilestone.getMilestones().get(0);
		Assert.assertEquals("Unassigned", md.getLabelListValue().getValue());
		Assert.assertEquals(0, cardsByMilestone.getStatusColors().size());
	}
}
