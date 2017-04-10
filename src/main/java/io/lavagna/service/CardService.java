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

import io.lavagna.model.BoardColumn.BoardColumnLocation;
import io.lavagna.model.*;
import io.lavagna.model.Event.EventType;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class CardService {

    private final EventRepository eventRepository;
    private final CardRepository cardRepository;
    private final CardDataRepository cardDataRepository;
    private final CardDataService cardDataService;
    private final CardLabelRepository cardLabelRepository;

    public CardService(CardRepository cardRepository, CardDataRepository cardDataRepository,
        EventRepository eventRepository, CardDataService cardDataService, CardLabelRepository cardLabelRepository) {
        this.cardRepository = cardRepository;
        this.eventRepository = eventRepository;
        this.cardDataRepository = cardDataRepository;
        this.cardDataService = cardDataService;
        this.cardLabelRepository = cardLabelRepository;
    }

    private static List<Integer> fetchIds(List<CardFull> cards) {
        List<Integer> r = new ArrayList<>(cards.size());
        for (CardFull c : cards) {
            r.add(c.getId());
        }
        return r;
    }

    public boolean existCardWith(String boardShortName, int seqNumber) {
        return cardRepository.existCardWith(boardShortName, seqNumber);
    }

    public CardFullWithCounts findFullBy(int cardId) {
        CardFull card = cardRepository.findFullBy(cardId);
        return fetchCardFull(Collections.singletonList(card)).get(0);
    }

    public CardFullWithCounts findFullBy(String boardShortName, int seqNumber) {
        CardFull card = cardRepository.findFullBy(boardShortName, seqNumber);
        return fetchCardFull(Collections.singletonList(card)).get(0);
    }

    public List<CardFullWithCounts> fetchAllInColumn(int columnId) {

        List<CardFull> cards = cardRepository.findAllByColumnId(columnId);
        if (cards.isEmpty()) {
            return Collections.emptyList();
        }
        List<CardFullWithCounts> res = fetchCardFull(cards);

        Collections.sort(res, new Comparator<CardFullWithCounts>() {
            @Override
            public int compare(CardFullWithCounts o1, CardFullWithCounts o2) {
                return new CompareToBuilder().append(o1.getOrder(), o2.getOrder()).toComparison();
            }
        });
        //
        return res;
    }

    List<CardFull> findFullBy(Collection<Integer> ids) {
        return cardRepository.findFullBy(ids);
    }



    List<CardFullWithCounts> fetchCardFull(List<CardFull> cards) {
        List<Integer> ids = fetchIds(cards);
        Map<Integer, Map<String, CardDataCount>> counts = aggregateByCardId(
            cardDataRepository.findCountsByCardIds(ids));
        Map<Integer, List<LabelAndValue>> labels = cardLabelRepository.findCardLabelValuesByCardIds(ids);
        List<CardFullWithCounts> res = new ArrayList<>();
        for (CardFull card : cards) {
            res.add(new CardFullWithCounts(card, counts.get(card.getId()), labels.get(card.getId())));
        }
        return res;
    }

    public List<CardFullWithCounts> fetchPaginatedByBoardIdAndLocation(int boardId, BoardColumnLocation location, int page) {
    	List<Integer> ids = cardRepository.fetchPaginatedByBoardIdAndLocation(boardId, location, page);
		if (ids.isEmpty()) {
			return Collections.emptyList();
		}
    	Map<Integer, CardFull> cardFullById = aggregateCardFullByCardId(cardRepository.findAllByIds(ids));
    	Map<Integer, Map<String, CardDataCount>> counts = aggregateByCardId(cardDataRepository.findCountsByCardIds(ids));
    	Map<Integer, List<LabelAndValue>> labels = cardLabelRepository.findCardLabelValuesByCardIds(ids);
    	List<CardFullWithCounts> res = new ArrayList<>(ids.size());
		for (int id : ids) {
			CardFull card = cardFullById.get(id);
			res.add(new CardFullWithCounts(card, counts.get(card.getId()), labels.get(card.getId())));
		}
    	return res;
	}



	@Transactional(readOnly = false)
    public void moveCardsToColumn(List<Integer> cardIds, int previousColumnId, int columnId, int userId,
        EventType boardEventType, Date time) {
        List<Integer> updated = cardRepository.moveCardsToColumn(cardIds, previousColumnId, columnId, userId);
        eventRepository.insertCardEvents(updated, previousColumnId, columnId, userId, boardEventType, time, null);
    }

    @Transactional(readOnly = false)
    public Event updateCardName(int cardId, String name, User user, Date date) {
        Card card = cardRepository.updateCard(cardId, name);
        return eventRepository.insertCardEvent(cardId, card.getColumnId(), user.getId(), EventType.CARD_UPDATE, date,
            name);
    }

    @Transactional(readOnly = false)
    public Card cloneCard(int cardToCopyId, int columnId, User user) {
        Card cardToCopy = cardRepository.findBy(cardToCopyId);

        Card newCard = createCard(cardToCopy.getName(), columnId, new Date(), user);

        Map<CardLabel, List<CardLabelValue>> labels = cardLabelRepository.findCardLabelValuesByCardId(cardToCopyId);
        for (CardLabel label : labels.keySet()) {
            for (CardLabelValue labelValue : labels.get(label)) {
                cardLabelRepository.addLabelValueToCard(label, newCard.getId(), labelValue.getValue());
            }
        }

        // Copy the description
        CardDataHistory desc = cardDataService.findLatestDescriptionByCardId(cardToCopyId);
        if (desc != null) {
            cardDataService.updateDescription(newCard.getId(), desc.getContent(), desc.getTime(), desc.getUserId());
        }

        // Copy comments
        for (CardDataFull cData : cardDataService.findAllCommentsByCardId(cardToCopyId)) {
            if(cData.getEventType() == EventType.COMMENT_CREATE) {
                cardDataService.createComment(newCard.getId(), cData.getContent(), cData.getTime(), cData.getUserId());
            }
        }

        // Copy action lists
        Map<Integer, Integer> actionListsNewIds = new HashMap<>();
        for (CardData iData : cardDataService.findAllActionListsAndItemsByCardId(cardToCopyId)) {

            if (iData.getType().equals(CardType.ACTION_LIST)) {
                CardData actionList = cardDataService
                    .createActionList(newCard.getId(), iData.getContent(), user.getId(), new Date());

                actionListsNewIds.put(iData.getId(), actionList.getId());
            } else {
                CardData actionItem = cardDataService.createActionItem(newCard.getId(),
                    actionListsNewIds.get(iData.getReferenceId()), iData.getContent(), user.getId(), new Date());

                if (iData.getType().equals(CardType.ACTION_CHECKED)) {
                    cardDataService.toggleActionItem(actionItem.getId(), true, user.getId(), new Date());
                }
            }
        }

        return newCard;
    }

    @Transactional(readOnly = false)
    public Card createCard(String name, int columnId, Date creationTime, User user) {
        Card card = cardRepository.createCard(name, columnId, user);
        eventRepository.insertCardEvent(card.getId(), columnId, user.getId(), EventType.CARD_CREATE, creationTime,
            card.getName());
        return card;
    }

    @Transactional(readOnly = false)
    public Card createCardFromTop(String name, int columnId, Date creationTime, User user) {
        Card card = cardRepository.createCardFromTop(name, columnId, user);
        eventRepository.insertCardEvent(card.getId(), columnId, user.getId(), EventType.CARD_CREATE, creationTime,
            card.getName());
        return card;
    }

    @Transactional(readOnly = false)
    public Event moveCardToColumn(int cardId, int previousColumnId, int columnId, int userId, Date date) {
        cardRepository.moveCardToColumn(cardId, previousColumnId, columnId);
        return eventRepository.insertCardEvent(cardId, previousColumnId, columnId, userId, EventType.CARD_MOVE, date,
            null);
    }

    @Transactional(readOnly = false)
    public Event moveCardToColumnAndReorder(int cardId, int prevColumnId, int newColumnId,
        List<Integer> newOrderForNewColumn, User user) {
        cardRepository.moveCardToColumnAndReorder(cardId, prevColumnId, newColumnId, newOrderForNewColumn);
        return eventRepository.insertCardEvent(cardId, prevColumnId, newColumnId, user.getId(), EventType.CARD_MOVE,
            new Date(), null);
    }

    private static Map<Integer, Map<String, CardDataCount>> aggregateByCardId(List<CardDataCount> counts) {
        Map<Integer, Map<String, CardDataCount>> r = new TreeMap<>();

        for (CardDataCount c : counts) {
            if (!r.containsKey(c.getCardId())) {
                r.put(c.getCardId(), new TreeMap<String, CardDataCount>());
            }
            r.get(c.getCardId()).put(c.getType(), c);
        }

        return r;
    }

    private static Map<Integer, CardFull> aggregateCardFullByCardId(List<CardFull> cards) {
		Map<Integer, CardFull> res = new HashMap<>();
		for(CardFull card : cards) {
			res.put(card.getId(), card);
		}
		return res;
	}



}
