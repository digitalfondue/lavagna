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

import io.lavagna.model.*;
import io.lavagna.model.Event.EventType;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import static java.util.EnumSet.of;

@Service
@Transactional(readOnly = true)
public class CardDataService {

    private final EventRepository eventRepository;
    private final CardDataRepository cardDataRepository;

    public CardDataService(EventRepository eventRepository, CardDataRepository cardDataRepository) {
        this.eventRepository = eventRepository;
        this.cardDataRepository = cardDataRepository;
    }

    public List<CardDataFull> findDescriptionByCardId(int cardId) {
        return cardDataRepository.findAllDataByCardIdAndType(cardId, CardType.DESCRIPTION);
    }

    public CardDataHistory findLatestDescriptionByCardId(int cardId) {
        List<CardDataFull> descriptions = findDescriptionByCardId(cardId);
        // look for duplicates, the event model will keep the entire history of
        // the description
        CardDataHistory description = null;
        for (CardDataFull des : descriptions) {
            if (description == null) {
                description = new CardDataHistory(des.getId(), des.getContent(), des.getOrder(), des.getUserId(),
                    des.getTime(), des.getUserId(), des.getTime());
            }

            if (des.getEventType() == EventType.DESCRIPTION_CREATE) {
                description.setUserId(des.getUserId());
                description.setTime(des.getTime());
            }
            if (des.getEventType() == EventType.DESCRIPTION_UPDATE) {
                description.setUpdatedCount(description.getUpdatedCount() + 1);
                // never null because the object's fields are always initialized
                if (des.getTime().getTime() > description.getUpdateDate().getTime()) {
                    description.setUpdateUser(des.getUserId());
                    description.setUpdateDate(des.getTime());
                }
            }
        }
        return description;
    }

    public List<CardDataFull> findAllCommentsByCardId(int cardId) {
        return cardDataRepository.findAllDataByCardIdAndType(cardId, CardType.COMMENT);
    }

    public List<CardData> findAllActionListsAndItemsByCardId(int cardId) {
        return cardDataRepository.findAllDataLightByCardIdAndTypes(cardId,
            of(CardType.ACTION_CHECKED, CardType.ACTION_UNCHECKED, CardType.ACTION_LIST));
    }

    @Transactional(readOnly = false)
    private CardData createDescription(int cardId, String content, Date time, int userId) {
        CardData description = cardDataRepository.createData(cardId, CardType.DESCRIPTION, content);
        eventRepository.insertCardDataEvent(description.getId(), cardId, EventType.DESCRIPTION_CREATE, userId,
            description.getId(), time);
        return description;
    }

    @Transactional(readOnly = false)
    public CardData createComment(int cardId, String content, Date time, int userId) {
        CardData comment = cardDataRepository.createData(cardId, CardType.COMMENT, content);
        eventRepository.insertCardDataEvent(comment.getId(), cardId, EventType.COMMENT_CREATE, userId,
            comment.getId(), time);
        return comment;
    }

    @Transactional(readOnly = false)
    public CardData createActionList(int cardId, String name, int userId, Date time) {
        CardData actionList = cardDataRepository.createData(cardId, CardType.ACTION_LIST, name);
        eventRepository.insertCardDataEvent(actionList.getId(), cardId, EventType.ACTION_LIST_CREATE, userId,
            actionList.getId(), time);
        return actionList;
    }

    @Transactional(readOnly = false)
    public CardData createActionItem(int cardId, int actionListId, String name, int userId, Date time) {
        CardData actionItem = cardDataRepository.createDataWithReferenceOrder(cardId, actionListId,
            CardType.ACTION_UNCHECKED, name);
        eventRepository.insertCardDataEvent(actionItem.getId(), cardId, EventType.ACTION_ITEM_CREATE, userId,
            actionItem.getReferenceId(), time);
        return actionItem;
    }

    @Transactional(readOnly = false)
    public void createFile(final String digest,
                           final long fileSize,
                           final InputStream content,
                           final String contentType) {
        if (!cardDataRepository.fileExists(digest)) {
            cardDataRepository.addUploadContent(digest, fileSize, content, contentType);
        }
    }

    @Transactional(readOnly = false)
    public ImmutablePair<Boolean, CardData> assignFileToCard(String name, String digest, int cardId, User user, Date time) {
        return assignFile(name, digest, cardId, user, time);
    }

    @Transactional(readOnly = false)
    public ImmutablePair<Boolean, CardData> createFile(String name, String digest, long fileSize, int cardId,
        InputStream content, String contentType, User user, Date time) {
        if (!cardDataRepository.fileExists(digest)) {
            cardDataRepository.addUploadContent(digest, fileSize, content, contentType);
        }

        return assignFile(name, digest, cardId, user, time);
    }

    private ImmutablePair<Boolean, CardData> assignFile(String name, String digest, int cardId, User user, Date time) {
        if (!cardDataRepository.isFileAvailableByCard(digest, cardId)) {
            CardData file = cardDataRepository.createData(cardId, CardType.FILE, digest);
            cardDataRepository.createUploadInfo(digest, name, name, file.getId());
            eventRepository.insertFileEvent(file.getId(), cardId, EventType.FILE_UPLOAD, user.getId(), file.getId(),
                name, time);
            return ImmutablePair.of(true, file);
        }
        return ImmutablePair.<Boolean, CardData>of(false, null);
    }

    /**
     * Checked and filtered.
     */
    @Transactional(readOnly = false)
    public void moveActionItem(int cardId, int dataId, Integer newReferenceId, List<Integer> newDataOrder, User user,
        Date time) {
        CardData actionItem = cardDataRepository.getUndeletedDataLightById(dataId);

        CardData newActionList = cardDataRepository.getDataLightById(newReferenceId);

        // checks
        Validate.isTrue(actionItem.getCardId() == cardId
            && (actionItem.getType() == CardType.ACTION_CHECKED || actionItem.getType() == CardType.ACTION_UNCHECKED));
        Validate.isTrue(newActionList.getCardId() == cardId && newActionList.getType() == CardType.ACTION_LIST);
        //

        cardDataRepository.updateReferenceId(cardId, dataId, newReferenceId);

        cardDataRepository.updateOrderByCardAndReferenceId(cardId, newReferenceId, newDataOrder);
        eventRepository.insertCardDataEvent(dataId, cardId, EventType.ACTION_ITEM_MOVE, user.getId(),
            actionItem.getReferenceId(), newReferenceId, time);
    }

    @Transactional(readOnly = false)
    public int toggleActionItem(int actionitemId, boolean status, int userId, Date time) {
        CardData actionItem = cardDataRepository.getUndeletedDataLightById(actionitemId);
        eventRepository.insertCardDataEvent(actionitemId, actionItem.getCardId(), status ? EventType.ACTION_ITEM_CHECK
            : EventType.ACTION_ITEM_UNCHECK, userId, actionItem.getReferenceId(), time);
        return cardDataRepository.updateType(actionitemId, of(CardType.ACTION_CHECKED, CardType.ACTION_UNCHECKED),
            status ? CardType.ACTION_CHECKED : CardType.ACTION_UNCHECKED);
    }

    @Transactional(readOnly = false)
    public int updateActionItem(int actionItemId, String content) {
        return cardDataRepository.updateContent(actionItemId, of(CardType.ACTION_CHECKED, CardType.ACTION_UNCHECKED),
            content);
    }

    @Transactional(readOnly = false)
    public int updateActionList(int actionListId, String content) {
        return cardDataRepository.updateContent(actionListId, of(CardType.ACTION_LIST), content);
    }

    @Transactional(readOnly = false)
    public int updateComment(int commentId, String content, Date time, User user) {
        CardData comment = cardDataRepository.getUndeletedDataLightById(commentId);
        // save old comment as history and update the reference id of the event
        CardData historyComment = cardDataRepository.createDataWithReferenceOrder(comment.getCardId(), comment.getId(),
            CardType.COMMENT_HISTORY, comment.getContent());
        // insert new data
        eventRepository.insertCardDataEvent(commentId, comment.getCardId(), EventType.COMMENT_UPDATE, user.getId(),
            historyComment.getId(), time);
        return cardDataRepository.updateContent(commentId, of(CardType.COMMENT), content);
    }

    @Transactional(readOnly = false)
    public int updateDescription(int cardId, String content, Date time, int userId) {
        // if no data is returned, then create the description
        List<CardData> descriptions = cardDataRepository.findAllDataLightByCardIdAndType(cardId, CardType.DESCRIPTION);
        if (descriptions.isEmpty()) {
            return createDescription(cardId, content, time, userId).getId();
        }

        // there can (should) be only one
        Validate.isTrue(1 == descriptions.size());
        CardData description = descriptions.get(0);
        // save old description as history and update the reference id of the
        // event
        CardData historyDescription = cardDataRepository.createDataWithReferenceOrder(description.getCardId(),
            description.getId(), CardType.DESCRIPTION_HISTORY, description.getContent());
        // insert new data
        eventRepository.insertCardDataEvent(description.getId(), description.getCardId(), EventType.DESCRIPTION_UPDATE,
            userId, historyDescription.getId(), time);
        return cardDataRepository.updateContent(description.getId(), of(CardType.DESCRIPTION), content);
    }

    @Transactional(readOnly = false)
    public Event deleteActionItem(int actionItemId, User user, Date time) {
        CardData actionItem = cardDataRepository.getUndeletedDataLightById(actionItemId);
        Event event = eventRepository.insertCardDataEvent(actionItemId, actionItem.getCardId(),
            EventType.ACTION_ITEM_DELETE, user.getId(), actionItem.getReferenceId(), time);
        cardDataRepository.softDelete(actionItemId, of(CardType.ACTION_CHECKED, CardType.ACTION_UNCHECKED));
        return event;
    }

    @Transactional(readOnly = false)
    public Event deleteActionList(int actionListId, User user, Date time) {
        CardData actionList = cardDataRepository.getUndeletedDataLightById(actionListId);
        Validate.isTrue(actionList.getType() == CardType.ACTION_LIST);
        Event event = eventRepository.insertCardDataEvent(actionListId, actionList.getCardId(),
            EventType.ACTION_LIST_DELETE, user.getId(), actionListId, time);
        cardDataRepository.softDeleteOnCascade(actionListId, of(CardType.ACTION_LIST));
        return event;
    }

    @Transactional(readOnly = false)
    public Event deleteComment(int commentId, User user, Date time) {
        CardData comment = cardDataRepository.getUndeletedDataLightById(commentId);
        Event event = eventRepository.insertCardDataEvent(commentId, comment.getCardId(), EventType.COMMENT_DELETE,
            user.getId(), commentId, time);
        cardDataRepository.softDelete(commentId, of(CardType.COMMENT));
        return event;
    }

    @Transactional(readOnly = false)
    public Event deleteFile(int cardDataId, User user, Date time) {
        FileDataLight file = cardDataRepository.getUndeletedFileByCardDataId(cardDataId);
        Event event = eventRepository.insertFileEvent(file.getCardDataId(), file.getCardId(), EventType.FILE_DELETE,
            user.getId(), file.getCardDataId(), file.getName(), time);
        cardDataRepository.softDelete(file.getCardDataId(), of(CardType.FILE));
        return event;
    }

    @Transactional(readOnly = false)
    public void undoDeleteActionItem(Event event) {
        eventRepository.remove(event.getId(), event.getCardId(), event.getEvent());
        cardDataRepository.undoSoftDelete(event.getDataId(), of(CardType.ACTION_CHECKED, CardType.ACTION_UNCHECKED));
    }

    @Transactional(readOnly = false)
    public void undoDeleteComment(Event event) {
        eventRepository.remove(event.getId(), event.getCardId(), event.getEvent());
        cardDataRepository.undoSoftDelete(event.getDataId(), of(CardType.COMMENT));
    }

    @Transactional(readOnly = false)
    public void undoDeleteActionList(Event event) {
        eventRepository.remove(event.getId(), event.getCardId(), event.getEvent());
        cardDataRepository.undoSoftDeleteOnCascade(event.getDataId(), of(CardType.ACTION_LIST),
            of(EventType.ACTION_ITEM_DELETE));
    }

    @Transactional(readOnly = false)
    public void undoDeleteFile(Event event) {
        eventRepository.remove(event.getId(), event.getCardId(), event.getEvent());
        cardDataRepository.undoSoftDelete(event.getDataId(), of(CardType.FILE));
    }
}
