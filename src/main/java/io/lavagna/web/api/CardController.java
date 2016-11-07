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
import io.lavagna.model.Board;
import io.lavagna.model.BoardColumn;
import io.lavagna.model.BoardColumn.BoardColumnLocation;
import io.lavagna.model.Card;
import io.lavagna.model.CardFullWithCounts;
import io.lavagna.model.Event;
import io.lavagna.model.Permission;
import io.lavagna.model.ProjectAndBoard;
import io.lavagna.model.SearchResults;
import io.lavagna.model.User;
import io.lavagna.model.UserWithPermission;
import io.lavagna.service.BoardColumnRepository;
import io.lavagna.service.BoardRepository;
import io.lavagna.service.CardRepository;
import io.lavagna.service.CardService;
import io.lavagna.service.EventEmitter;
import io.lavagna.service.ProjectService;
import io.lavagna.service.SearchFilter;
import io.lavagna.service.SearchFilter.SearchFilterValue;
import io.lavagna.service.SearchService;
import io.lavagna.web.helper.ExpectPermission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.Validate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CardController {

	private final CardRepository cardRepository;
	private final CardService cardService;
	private final BoardRepository boardRepository;
	private final ProjectService projectService;
	private final BoardColumnRepository boardColumnRepository;
	private final SearchService searchService;
	private final EventEmitter eventEmitter;


	public CardController(CardRepository cardRepository, CardService cardService,
			BoardRepository boardRepository, ProjectService projectService,
			BoardColumnRepository boardColumnRepository,
			SearchService searchService, EventEmitter eventEmitter) {
		this.cardRepository = cardRepository;
		this.cardService = cardService;
		this.boardRepository = boardRepository;
		this.projectService = projectService;
		this.boardColumnRepository = boardColumnRepository;
		this.searchService = searchService;
		this.eventEmitter = eventEmitter;
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/column/{columnId}/card", method = RequestMethod.GET)
	public List<CardFullWithCounts> fetchAllInColumn(@PathVariable("columnId") int columnId) {
		return cardService.fetchAllInColumn(columnId);
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
	public List<CardFullWithCounts> fetchPaginatedIn(@PathVariable("shortName") String shortName,
			@PathVariable("location") BoardColumnLocation location, @PathVariable("page") int page) {
		int boardId = boardRepository.findBoardIdByShortName(shortName);
		return cardService.fetchPaginatedByBoardIdAndLocation(boardId, location, page);
	}

	private void emitCreateCard(int columnId, Card createdCard, User user) {
		ProjectAndBoard projectAndBoard = boardRepository.findProjectAndBoardByColumnId(columnId);
		eventEmitter.emitCreateCard(projectAndBoard.getProject().getShortName(), projectAndBoard.getBoard()
				.getShortName(), columnId, createdCard, user);
	}

	// TODO: check that columnId is effectively inside the board named shortName
	@ExpectPermission(Permission.CREATE_CARD)
	@RequestMapping(value = "/api/column/{columnId}/card", method = RequestMethod.POST)
	public void create(@PathVariable("columnId") int columnId, @RequestBody CardData card, User user) {
		Card createdCard = cardService.createCard(card.name, columnId, new Date(), user);
		emitCreateCard(columnId, createdCard, user);
	}

    @ExpectPermission(Permission.CREATE_CARD)
    @RequestMapping(value = "/api/card/{cardId}/clone-to/column/{columnId}", method = RequestMethod.POST)
    public void clone(@PathVariable("cardId") int cardId, @PathVariable("columnId") int columnId, User user) {
        Card clonedCard = cardService.cloneCard(cardId, columnId, user);
        emitCreateCard(columnId, clonedCard, user);
    }

	@ExpectPermission(Permission.CREATE_CARD)
	@RequestMapping(value = "/api/column/{columnId}/card-top", method = RequestMethod.POST)
	public void createCardFromTop(@PathVariable("columnId") int columnId, @RequestBody CardData card, User user) {
		Card createdCard = cardService.createCardFromTop(card.name, columnId, new Date(), user);
		emitCreateCard(columnId, createdCard, user);
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/card/{cardId}", method = RequestMethod.GET)
	public CardFullWithCounts findCardById(@PathVariable("cardId") int id) {
		return cardService.findFullBy(id);
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/card-by-seq/{boardShortName:[A-Z0-9_]+}-{seqNr:[0-9]+}", method = RequestMethod.GET)
	public CardFullWithCounts findCardIdByBoardNameAndSeq(@PathVariable("boardShortName") String boardShortName,
			@PathVariable("seqNr") int seqNr) {
		return cardService.findFullBy(boardShortName, seqNr);
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/card/{cardId}/activity", method = RequestMethod.GET)
	public List<Event> getCardActivity(@PathVariable("cardId") int id) {
		return cardRepository.fetchAllActivityByCardId(id);
	}

	@ExpectPermission(Permission.UPDATE_CARD)
	@RequestMapping(value = "/api/card/{cardId}", method = RequestMethod.POST)
	public void updateCard(@PathVariable("cardId") int id, @RequestBody CardData updateCard, User user) {
		
		Card beforeUpdate = cardRepository.findBy(id);
		
		cardService.updateCard(id, updateCard.name, user, new Date());

		Card c = cardRepository.findBy(id);
		ProjectAndBoard projectAndBoard = boardRepository.findProjectAndBoardByColumnId(c.getColumnId());
		eventEmitter.emitUpdateCard(projectAndBoard.getProject().getShortName(), projectAndBoard.getBoard()
				.getShortName(), c.getColumnId(), beforeUpdate, c, user);
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
				board.getShortName(), Collections.singletonList(id), prevCol, newCol, user);

		return event;
	}

    @ExpectPermission(Permission.MOVE_CARD)
    @RequestMapping(value = "/api/card/{cardId}/from-column/{previousColumnId}/to-column/{newColumnId}/end", method = RequestMethod.POST)
    public Event moveCardToColumn(@PathVariable("cardId") int id,
                                  @PathVariable("previousColumnId") int previousColumnId, @PathVariable("newColumnId") int newColumnId,
                                  User user) {
        List<CardFullWithCounts> cards = cardService.fetchAllInColumn(newColumnId);
        List<Integer> order = new ArrayList<>();
        for(CardFullWithCounts card: cards) {
            order.add(card.getId());
        }
        order.add(id);
        ColumnOrders orders = new ColumnOrders();
        orders.setNewContainer(order);

        return moveCardToColumn(id, previousColumnId, newColumnId, orders, user);
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
				boardShortName, cardIds.cardIds, col, destination, user);
	}

	private static final List<SearchFilter> TO_ME_STATUS_OPEN = Arrays.asList(new SearchFilter(FilterType.ASSIGNED, "to", new SearchFilterValue(ValueType.CURRENT_USER, "me")), SearchFilter.filter(FilterType.STATUS, ValueType.STRING, "OPEN"));

	@ExpectPermission(Permission.SEARCH)
	@RequestMapping(value = "/api/self/cards/{page}", method = RequestMethod.GET)
	public SearchResults getOpenCards(@PathVariable(value = "page") int page, UserWithPermission user) {
	    return searchService.find(TO_ME_STATUS_OPEN, null, null, user, page);
	}

	@ExpectPermission(Permission.SEARCH)
	@RequestMapping(value = "/api/self/project/{projectShortName}/cards/{page}", method = RequestMethod.GET)
	public SearchResults getOpenCardsByProjectShortName(
			@PathVariable(value = "projectShortName") String shortName, @PathVariable(value = "page") int page,
			UserWithPermission user) {
	    return searchService.find(TO_ME_STATUS_OPEN, projectService.findIdByShortName(shortName), null, user, page);
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
