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

import static io.lavagna.service.SearchFilter.FilterType;
import static io.lavagna.service.SearchFilter.ValueType;
import static io.lavagna.service.SearchFilter.filter;
import io.lavagna.model.Board;
import io.lavagna.model.BoardColumn;
import io.lavagna.model.BoardColumn.BoardColumnLocation;
import io.lavagna.model.BoardColumnDefinition;
import io.lavagna.model.Card;
import io.lavagna.model.CardFull;
import io.lavagna.model.CardFullWithCounts;
import io.lavagna.model.CardFullWithCountsHolder;
import io.lavagna.model.CardLabel;
import io.lavagna.model.ColumnDefinition;
import io.lavagna.model.Event;
import io.lavagna.model.LabelListValue;
import io.lavagna.model.LabelListValueWithMetadata;
import io.lavagna.model.MilestoneCount;
import io.lavagna.model.Pair;
import io.lavagna.model.Permission;
import io.lavagna.model.Project;
import io.lavagna.model.ProjectAndBoard;
import io.lavagna.model.SearchResults;
import io.lavagna.model.User;
import io.lavagna.model.UserWithPermission;
import io.lavagna.service.BoardColumnRepository;
import io.lavagna.service.BoardRepository;
import io.lavagna.service.CardLabelRepository;
import io.lavagna.service.CardRepository;
import io.lavagna.service.CardService;
import io.lavagna.service.EventEmitter;
import io.lavagna.service.ProjectService;
import io.lavagna.service.SearchFilter;
import io.lavagna.service.SearchService;
import io.lavagna.service.StatisticsService;
import io.lavagna.web.api.model.MilestoneDetail;
import io.lavagna.web.api.model.MilestoneInfo;
import io.lavagna.web.api.model.Milestones;
import io.lavagna.web.helper.ExpectPermission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CardController {

	private static final int CARDS_PER_PAGE = 20;
	private final CardRepository cardRepository;
	private final CardService cardService;
	private final CardLabelRepository cardLabelRepository;
	private final BoardRepository boardRepository;
	private final ProjectService projectService;
	private final BoardColumnRepository boardColumnRepository;
	private final StatisticsService statisticsService;
	private final SearchService searchService;
	private final EventEmitter eventEmitter;

	@Autowired
	public CardController(CardRepository cardRepository, CardService cardService,
			CardLabelRepository cardLabelRepository, BoardRepository boardRepository, ProjectService projectService,
			BoardColumnRepository boardColumnRepository, StatisticsService statisticsService,
			SearchService searchService, EventEmitter eventEmitter) {
		this.cardRepository = cardRepository;
		this.cardService = cardService;
		this.cardLabelRepository = cardLabelRepository;
		this.boardRepository = boardRepository;
		this.projectService = projectService;
		this.boardColumnRepository = boardColumnRepository;
		this.statisticsService = statisticsService;
		this.searchService = searchService;
		this.eventEmitter = eventEmitter;
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/column/{columnId}/card", method = RequestMethod.GET)
	public List<CardFullWithCounts> fetchAllInColumn(@PathVariable("columnId") int columnId) {
		return cardService.fetchAllInColumn(columnId);
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/project/{projectShortName}/cards-by-milestone", method = RequestMethod.GET)
	public Milestones findCardsByMilestone(@PathVariable("projectShortName") String projectShortName) {
		Project project = projectService.findByShortName(projectShortName);
		Map<Integer, Integer> milestoneToIndex = new HashMap<>();
		List<MilestoneInfo> milestones = new ArrayList<>();
		getMilestones(project.getId(), milestoneToIndex, milestones);

		for (MilestoneCount count : statisticsService.findCardsCountByMilestone(project.getId())) {
			MilestoneInfo md = milestones.get(milestoneToIndex.get(count.getMilestoneId()));
			md.getCardsCountByStatus().put(count.getColumnDefinition(), count.getCount());
		}

		Map<ColumnDefinition, Integer> statusColors = new EnumMap<>(ColumnDefinition.class);
		for (BoardColumnDefinition cd : projectService.findColumnDefinitionsByProjectId(project.getId())) {
			statusColors.put(cd.getValue(), cd.getColor());
		}

		return new Milestones(milestones, statusColors);
	}

	private void getMilestones(int projectId, Map<Integer, Integer> milestoneToIndex, List<MilestoneInfo> milestones) {
		CardLabel label = cardLabelRepository.findLabelByName(projectId, "MILESTONE", CardLabel.LabelDomain.SYSTEM);
		List<LabelListValueWithMetadata> listValues = cardLabelRepository.findListValuesByLabelId(label.getId());
		int foundUnassignedIndex = -1;
		int mIndex = 0;
		for (LabelListValue milestone : listValues) {
			milestones.add(new MilestoneInfo(milestone, new EnumMap<ColumnDefinition, Long>(ColumnDefinition.class)));
			milestoneToIndex.put(milestone.getId(), mIndex);
			if ("Unassigned".equals(milestone.getValue())) {
				foundUnassignedIndex = mIndex;
			}
			mIndex++;
		}
		if (foundUnassignedIndex < 0) {
			LabelListValue unassigned = new LabelListValue(-1, 0, Integer.MAX_VALUE, "Unassigned");
			milestones.add(new MilestoneInfo(unassigned, new EnumMap<ColumnDefinition, Long>(ColumnDefinition.class)));
			milestoneToIndex.put(null, milestoneToIndex.size());
		} else {
			milestoneToIndex.put(null, foundUnassignedIndex);
		}
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/project/{projectShortName}/cards-by-milestone-detail/{milestone}", method = RequestMethod.GET)
	public MilestoneDetail findCardsByMilestoneDetail(@PathVariable("projectShortName") String projectShortName,
			@PathVariable("milestone") String milestone, UserWithPermission user) {

		int projectId = projectService.findByShortName(projectShortName).getId();
		CardLabel label = cardLabelRepository.findLabelByName(projectId, "MILESTONE", CardLabel.LabelDomain.SYSTEM);
		List<LabelListValueWithMetadata> listValues = cardLabelRepository.findListValuesByLabelIdAndValue(label.getId(), milestone);

		SearchFilter filter;
		Map<Long, Pair<Long, Long>> assignedAndClosedCards;

		if (listValues.size() > 0) {
			filter = filter(FilterType.MILESTONE, ValueType.STRING, milestone);
			assignedAndClosedCards = statisticsService.getAssignedAndClosedCardsByMilestone(listValues.get(0),
					DateUtils.addWeeks(DateUtils.truncate(new Date(), Calendar.DATE), -2));
		} else {
			filter = filter(FilterType.MILESTONE, ValueType.UNASSIGNED, null);
			assignedAndClosedCards = null;
		}

        SearchFilter notTrashFilter = filter(SearchFilter.FilterType.NOTLOCATION, SearchFilter.ValueType.STRING,
            BoardColumnLocation.TRASH.toString());

		SearchResults cards = searchService.find(Arrays.asList(filter, notTrashFilter), projectId, null, user);
		return new MilestoneDetail(cards, assignedAndClosedCards);
	}

	/**
	 * Return the latest 11 card in a given location, ordered by mutation time. (11 so the user can paginate 10 at the
	 * time and know if there are more).
	 *
	 * @param shortName
	 * @param location
	 * @param page
	 * @return
	 */
	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/board/{shortName}/cards-in/{location}/{page}", method = RequestMethod.GET)
	public List<Card> fetchPaginatedIn(@PathVariable("shortName") String shortName,
			@PathVariable("location") BoardColumnLocation location, @PathVariable("page") int page) {
		int boardId = boardRepository.findBoardIdByShortName(shortName);
		return cardRepository.fetchPaginatedByBoardIdAndLocation(boardId, location, page);
	}

	private void emitCreateCard(int columnId, Card createdCard) {
		ProjectAndBoard projectAndBoard = boardRepository.findProjectAndBoardByColumnId(columnId);
		eventEmitter.emitCreateCard(projectAndBoard.getProject().getShortName(), projectAndBoard.getBoard()
				.getShortName(), columnId, createdCard.getId());
	}

	// TODO: check that columnId is effectively inside the board named shortName
	@ExpectPermission(Permission.CREATE_CARD)
	@RequestMapping(value = "/api/column/{columnId}/card", method = RequestMethod.POST)
	public void create(@PathVariable("columnId") int columnId, @RequestBody CardData card, User user) {
		Card createdCard = cardService.createCard(card.name, columnId, new Date(), user);
		emitCreateCard(columnId, createdCard);
	}

	@ExpectPermission(Permission.CREATE_CARD)
	@RequestMapping(value = "/api/column/{columnId}/card-top", method = RequestMethod.POST)
	public void createCardFromTop(@PathVariable("columnId") int columnId, @RequestBody CardData card, User user) {
		Card createdCard = cardService.createCardFromTop(card.name, columnId, new Date(), user);
		emitCreateCard(columnId, createdCard);
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/card/{cardId}", method = RequestMethod.GET)
	public CardFull findCardById(@PathVariable("cardId") int id) {
		return cardRepository.findFullBy(id);
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/card-by-seq/{boardShortName:[A-Z0-9_]+}-{seqNr:[0-9]+}", method = RequestMethod.GET)
	public CardFull findCardIdByBoardNameAndSeq(@PathVariable("boardShortName") String boardShortName,
			@PathVariable("seqNr") int seqNr) {
		return cardRepository.findFullBy(boardShortName, seqNr);
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/card/{cardId}/activity", method = RequestMethod.GET)
	public List<Event> getCardActivity(@PathVariable("cardId") int id) {
		return cardRepository.fetchAllActivityByCardId(id);
	}

	@ExpectPermission(Permission.UPDATE_CARD)
	@RequestMapping(value = "/api/card/{cardId}", method = RequestMethod.POST)
	public void updateCard(@PathVariable("cardId") int id, @RequestBody CardData updateCard, User user) {
		cardService.updateCard(id, updateCard.name, user, new Date());

		Card c = cardRepository.findBy(id);
		ProjectAndBoard projectAndBoard = boardRepository.findProjectAndBoardByColumnId(c.getColumnId());
		eventEmitter.emitUpdateCard(projectAndBoard.getProject().getShortName(), projectAndBoard.getBoard()
				.getShortName(), c.getColumnId(), id);
	}

	@ExpectPermission(Permission.MOVE_CARD)
	@RequestMapping(value = "/api/card/{cardId}/from-column/{previousColumnId}/to-column/{newColumnId}", method = RequestMethod.POST)
	public Event moveCardToColumn(@PathVariable("cardId") int id,
			@PathVariable("previousColumnId") int previousColumnId, @PathVariable("newColumnId") int newColumnId,
			@RequestBody ColumnOrders columnOrders, User user) {

		//
		BoardColumn prevCol = boardColumnRepository.findById(previousColumnId);
		BoardColumn newCol = boardColumnRepository.findById(newColumnId);
		Card c = cardRepository.findBy(id);
		Validate.isTrue(c.getColumnId() == prevCol.getId(), "card must be inside previous column");
		Validate.isTrue(prevCol.getBoardId() == newCol.getBoardId(), "can only move inside the same board");
		//

		Event event = cardService.moveCardToColumnAndReorder(id,//
				previousColumnId, newColumnId, columnOrders.newContainer, user);

		//
		eventEmitter.emitUpdateCardPosition(previousColumnId);
		eventEmitter.emitUpdateCardPosition(newColumnId);
		//

		Board board = boardRepository.findBoardById(prevCol.getBoardId());

		if (prevCol.getLocation() != BoardColumnLocation.BOARD) {
			eventEmitter.emitMoveCardFromOutsideOfBoard(board.getShortName(), prevCol.getLocation());
		}
		eventEmitter.emitCardHasMoved(projectService.findRelatedProjectShortNameByBoardShortname(board.getShortName()),
				board.getShortName(), Collections.singletonList(id));

		return event;
	}

	@ExpectPermission(Permission.MOVE_CARD)
	@RequestMapping(value = "/api/column/{columnId}/order", method = RequestMethod.POST)
	public boolean updateCardOrder(@PathVariable("columnId") int columnId, @RequestBody List<Number> cardIds) {
		cardRepository.updateCardOrder(Utils.from(cardIds), columnId);
		eventEmitter.emitUpdateCardPosition(columnId);
		return true;
	}

	@ExpectPermission(Permission.MOVE_CARD)
	@RequestMapping(value = "/api/card/from-column/{previousColumnId}/to-location/{location}", method = RequestMethod.POST)
	public void moveCardsToLocation(@PathVariable("previousColumnId") int previousColumnId,
			@PathVariable("location") BoardColumnLocation location, @RequestBody CardIds cardIds, User user) {
		Validate.isTrue(location != BoardColumnLocation.BOARD);
		Validate.isTrue(!cardIds.cardIds.isEmpty());
		BoardColumn col = boardColumnRepository.findById(cardRepository.findBy(cardIds.cardIds.get(0)).getColumnId());

		// Validate.isTrue(col.getLocation() == BoardColumnLocation.BOARD);

		BoardColumn destination = boardColumnRepository.findDefaultColumnFor(col.getBoardId(), location);

		Validate.isTrue(col.getLocation() != destination.getLocation());

		cardService.moveCardsToColumn(cardIds.cardIds, previousColumnId, destination.getId(), user.getId(),
				BoardColumnLocation.MAPPING.get(location), new Date());

		eventEmitter.emitUpdateCardPosition(previousColumnId);

		String boardShortName = boardRepository.findBoardById(destination.getBoardId()).getShortName();

		if (col.getLocation() == BoardColumnLocation.BOARD) {
			eventEmitter.emitMoveCardOutsideOfBoard(boardShortName, location);
		} else {
			eventEmitter.emitMoveCardFromOutsideOfBoard(boardShortName, col.getLocation());
			eventEmitter.emitMoveCardFromOutsideOfBoard(boardShortName, location);
		}

		eventEmitter.emitCardHasMoved(projectService.findRelatedProjectShortNameByBoardShortname(boardShortName),
				boardShortName, cardIds.cardIds);
	}

	@ExpectPermission(Permission.SEARCH)
	@RequestMapping(value = "/api/self/cards/{page}", method = RequestMethod.GET)
	public CardFullWithCountsHolder getOpenCards(@PathVariable(value = "page") int page, User user) {
		return cardService.getAllOpenCards(user, page, CARDS_PER_PAGE);
	}

	@ExpectPermission(Permission.SEARCH)
	@RequestMapping(value = "/api/self/project/{projectShortName}/cards/{page}", method = RequestMethod.GET)
	public CardFullWithCountsHolder getOpenCardsByProjectShortName(
			@PathVariable(value = "projectShortName") String shortName, @PathVariable(value = "page") int page,
			User user) {
		return cardService.getAllOpenCardsByProject(shortName, user, page, CARDS_PER_PAGE);
	}

	@Getter
	@Setter
	public static class CardData {
		private String name;
	}

	@Getter
	@Setter
	public static class ColumnOrders {
		private List<Integer> newContainer;
	}

	@Getter
	@Setter
	public static class CardIds {
		private List<Integer> cardIds;
	}
}
