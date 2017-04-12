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
import io.lavagna.model.BoardColumn.BoardColumnLocation;
import io.lavagna.model.CardLabelValue.LabelValue;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class EventEmitter {

	private final SimpMessageSendingOperations messagingTemplate;
	private final ApiHooksService apiHookService;

	public EventEmitter(SimpMessageSendingOperations messageSendingOperations, ApiHooksService apiHooksService) {
		this.messagingTemplate = messageSendingOperations;
		this.apiHookService = apiHooksService;
	}

	private static Event event(LavagnaEvent type) {
		return new Event(type, null);
	}

	private static ImportEvent importEvent(int currentBoard, int boards, String boardName) {
		return new ImportEvent(currentBoard, boards, boardName);
	}

	private static Event event(LavagnaEvent type, Object payload) {
		return new Event(type, payload);
	}

	private static String columnDestination(String boardShortName, BoardColumnLocation location) {
		return "/event/board/" + boardShortName + "/location/" + location + "/column";
	}

	private static String column(int columnId) {
		return "/event/column/" + columnId + "/card";
	}

	private static String board(String projectShortName, String boardShortName) {
		return "/event/" + projectShortName + "/" + boardShortName + "/card";
	}

	private static String cardData(int cardId) {
		return "/event/card/" + cardId + "/card-data";
	}

	// ------------ project

	public void emitCreateProject(String projectShortName, User user) {
		messagingTemplate.convertAndSend("/event/project", event(LavagnaEvent.CREATE_PROJECT, projectShortName));
		apiHookService.createdProject(projectShortName, user, LavagnaEvent.CREATE_PROJECT);
	}

	public void emitUpdateProject(String projectShortName, User user) {
		messagingTemplate.convertAndSend("/event/project", event(LavagnaEvent.UPDATE_PROJECT, projectShortName));
		apiHookService.updatedProject(projectShortName, user, LavagnaEvent.UPDATE_PROJECT);
	}

	public void emitImportProject(String importId, int currentBoard, int boards, String boardName, User user) {
		messagingTemplate.convertAndSend("/event/import/" + importId, importEvent(currentBoard, boards, boardName));
	}


	public void emitUpdateColumnDefinition(String shortName, User user) {
	    emitUpdateProject(shortName, user);
	    emitProjectMetadataHasChanged(shortName);
    }

	// ------------ board

	public void emitCreateBoard(String projectShortName, String boardShortName, User user) {
		messagingTemplate.convertAndSend("/event/project/" + projectShortName + "/board",
				event(LavagnaEvent.CREATE_BOARD));
		apiHookService.createdBoard(boardShortName, user, LavagnaEvent.CREATE_BOARD);
	}

	public void emitUpdateBoard(String boardShortName, User user) {
		messagingTemplate.convertAndSend("/event/board/" + boardShortName, event(LavagnaEvent.UPDATE_BOARD));
		apiHookService.updatedBoard(boardShortName, user, LavagnaEvent.UPDATE_BOARD);
	}

	// ------------ column

	public void emitCreateColumn(String boardShortName, BoardColumnLocation location, String columnName, User user) {
		messagingTemplate
				.convertAndSend(columnDestination(boardShortName, location), event(LavagnaEvent.CREATE_COLUMN));
		apiHookService.createdColumn(boardShortName, columnName, user, LavagnaEvent.CREATE_COLUMN);
	}

	public void emitUpdateColumn(String boardShortName, BoardColumnLocation location, int columnId,
			BoardColumn column, BoardColumn updatedColumn, User user) {
		messagingTemplate.convertAndSend(columnDestination(boardShortName, location), event(LavagnaEvent.UPDATE_COLUMN));
		messagingTemplate.convertAndSend("/event/column/" + columnId, event(LavagnaEvent.UPDATE_COLUMN));
		apiHookService.updateColumn(boardShortName, column, updatedColumn, user, LavagnaEvent.UPDATE_COLUMN);
	}

	public void emitUpdateColumnPosition(String boardShortName, BoardColumnLocation location) {
		messagingTemplate.convertAndSend(columnDestination(boardShortName, location),
				event(LavagnaEvent.UPDATE_COLUMN_POSITION));
		// updated position will not be exposed to the api hooks
	}

	// ------------ card

	public void emitCreateCard(String projectShortName, String boardShortName, int columnId, Card card, User user) {
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.CREATE_CARD));
		messagingTemplate.convertAndSend(board(projectShortName, boardShortName),
				event(LavagnaEvent.CREATE_CARD, card.getId()));
		apiHookService.createdCard(boardShortName, card, user, LavagnaEvent.CREATE_CARD);
	}

	public void emitUpdateCardName(String projectShortName, String boardShortName, int columnId, Card beforeUpdate, Card newCard, User user) {
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.UPDATE_CARD));
		messagingTemplate.convertAndSend(board(projectShortName, boardShortName),
				event(LavagnaEvent.UPDATE_CARD, beforeUpdate.getId()));
		apiHookService.updatedCardName(boardShortName, beforeUpdate, newCard, user, LavagnaEvent.UPDATE_CARD);
	}

	public void emitUpdateCardPosition(int columnId) {
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.UPDATE_CARD_POSITION));
		// updated position will not be exposed to the api hooks
	}

	public void emitMoveCardOutsideOfBoard(String boardShortName, BoardColumnLocation location) {
		messagingTemplate.convertAndSend("/event/board/" + boardShortName + "/location/" + location + "/card",
				event(LavagnaEvent.UPDATE_CARD_POSITION));
        // apihook: this will be handled by emitCardHasMoved
	}

	public void emitMoveCardFromOutsideOfBoard(String boardShortName, BoardColumnLocation location) {
		messagingTemplate.convertAndSend("/event/board/" + boardShortName + "/location/" + location + "/card",
				event(LavagnaEvent.UPDATE_CARD_POSITION));
        // apihook: this will be handled by emitCardHasMoved
	}

	public void emitCardHasMoved(String projectShortName, String boardShortName, Collection<Integer> affected, BoardColumn from, BoardColumn to, User user) {
		for (Integer a : affected) {
			messagingTemplate.convertAndSend(board(projectShortName, boardShortName),
					event(LavagnaEvent.UPDATE_CARD_POSITION, a));
		}
		apiHookService.moveCards(from, to, affected, user, LavagnaEvent.UPDATE_CARD_POSITION);
	}

	public void emitCreateRole() {
		messagingTemplate.convertAndSend("/event/permission", event(LavagnaEvent.CREATE_ROLE));
	}

	// ------------ permission

	public void emitCreateRole(String projectShortName) {
		messagingTemplate.convertAndSend("/event/permission/project/" + projectShortName,
				event(LavagnaEvent.CREATE_ROLE));
	}

	public void emitDeleteRole() {
		messagingTemplate.convertAndSend("/event/permission", event(LavagnaEvent.DELETE_ROLE));
	}

	public void emitDeleteRole(String projectShortName) {
		messagingTemplate.convertAndSend("/event/permission/project/" + projectShortName,
				event(LavagnaEvent.DELETE_ROLE));
	}

	public void emitUpdatePermissionsToRole() {
		messagingTemplate.convertAndSend("/event/permission", event(LavagnaEvent.UPDATE_PERMISSION_TO_ROLE));
	}

	public void emitUpdatePermissionsToRole(String projectShortName) {
		messagingTemplate.convertAndSend("/event/permission/project/" + projectShortName,
				event(LavagnaEvent.UPDATE_PERMISSION_TO_ROLE));
	}

	public void emitAssignRoleToUsers(String role) {
		messagingTemplate.convertAndSend("/event/permission", event(LavagnaEvent.ASSIGN_ROLE_TO_USERS, role));
	}

	public void emitAssignRoleToUsers(String role, String projectShortName) {
		messagingTemplate.convertAndSend("/event/permission/project/" + projectShortName,
				event(LavagnaEvent.ASSIGN_ROLE_TO_USERS, role));
	}

	public void emitRemoveRoleToUsers(String role) {
		messagingTemplate.convertAndSend("/event/permission", event(LavagnaEvent.REMOVE_ROLE_TO_USERS, role));
	}

	public void emitRemoveRoleToUsers(String role, String projectShortName) {
		messagingTemplate.convertAndSend("/event/permission/project/" + projectShortName,
				event(LavagnaEvent.REMOVE_ROLE_TO_USERS, role));
	}

	// ------------ card description
	public void emitUpdateDescription(int columnId, int cardId, CardDataHistory previousDescription, CardDataHistory newDescription, User user) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.UPDATE_DESCRIPTION));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.UPDATE_DESCRIPTION));
		apiHookService.updateCardDescription(cardId, previousDescription, newDescription, user, LavagnaEvent.UPDATE_DESCRIPTION);
	}

	// ------------ comment
	public void emitCreateComment(int columnId, int cardId, CardData comment, User user) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.CREATE_COMMENT));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.CREATE_COMMENT));
		apiHookService.createdComment(cardId, comment, user, LavagnaEvent.CREATE_COMMENT);
	}

	public void emitUpdateComment(int cardId, CardData previousComment, String newComment, User user) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.UPDATE_COMMENT));
		apiHookService.updatedComment(cardId, previousComment, newComment, user, LavagnaEvent.UPDATE_COMMENT);
	}

	public void emitDeleteComment(int columnId, int cardId, CardData deletedComment, User user) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.DELETE_COMMENT));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.DELETE_COMMENT));
		apiHookService.deletedComment(cardId, deletedComment, user, LavagnaEvent.DELETE_COMMENT);
	}

	public void emitUndoDeleteComment(int columnId, int cardId, CardData undeletedComment, User user) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.UNDO_DELETE_COMMENT));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.UNDO_DELETE_COMMENT));
		apiHookService.undeletedComment(cardId, undeletedComment, user, LavagnaEvent.UNDO_DELETE_COMMENT);
	}

	// ------------ action list handling

	public void emitCreateActionList(int cardId, String name, User user) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.CREATE_ACTION_LIST));
		apiHookService.createActionList(cardId, name, user, LavagnaEvent.CREATE_ACTION_LIST);
	}

	public void emitDeleteActionList(int columnId, int cardId, String name, User user) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.DELETE_ACTION_LIST));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.DELETE_ACTION_LIST));
		apiHookService.deleteActionList(cardId, name, user, LavagnaEvent.DELETE_ACTION_LIST);
	}

	public void emitUpdateActionList(int cardId, String oldName, String newName, User user) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.UPDATE_ACTION_LIST));
		apiHookService.updatedNameActionList(cardId, oldName, newName, user, LavagnaEvent.UPDATE_ACTION_LIST);
	}

	public void emitReorderActionLists(int cardId) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.REORDER_ACTION_LIST));
	}

	public void emitCreateActionItem(int columnId, int cardId, String actionItemListName, String actionItem, User user) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.CREATE_ACTION_ITEM));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.REORDER_ACTION_LIST));
		apiHookService.createActionItem(cardId, actionItemListName, actionItem, user, LavagnaEvent.CREATE_ACTION_ITEM);
	}

	public void emitDeleteActionItem(int columnId, int cardId, String actionItemListName, String actionItem, User user) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.DELETE_ACTION_ITEM));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.DELETE_ACTION_ITEM));
		apiHookService.deletedActionItem(cardId, actionItemListName, actionItem, user, LavagnaEvent.DELETE_ACTION_ITEM);
	}

	public void emitToggleActionItem(int columnId, int cardId, String actionItemListName, String actionItem, boolean toggle, User user) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.TOGGLE_ACTION_ITEM));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.TOGGLE_ACTION_ITEM));
		apiHookService.toggledActionItem(cardId, actionItemListName, actionItem, toggle, user, LavagnaEvent.TOGGLE_ACTION_ITEM);
	}

	public void emitUpdateUpdateActionItem(int cardId, String actionItemListName, String oldActionItem, String newActionItem, User user) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.UPDATE_ACTION_ITEM));
		apiHookService.updatedActionItem(cardId, actionItemListName, oldActionItem, newActionItem, user, LavagnaEvent.UPDATE_ACTION_ITEM);
	}

	public void emitMoveActionItem(int cardId, String fromActionItemListName, String toActionItemListName, String actionItem, User user) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.MOVE_ACTION_ITEM));
		apiHookService.movedActionItem(cardId, fromActionItemListName, toActionItemListName, actionItem, user, LavagnaEvent.MOVE_ACTION_ITEM);
	}

	public void emitReorderActionItems(int cardId) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.REORDER_ACTION_ITEM));
	}

	public void emiteUndoDeleteActionItem(int columnId, int cardId, String actionItemListName, String actionItem, User user) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.UNDO_DELETE_ACTION_ITEM));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.UNDO_DELETE_ACTION_ITEM));
		apiHookService.undoDeleteActionItem(cardId, actionItemListName, actionItem, user, LavagnaEvent.UNDO_DELETE_ACTION_ITEM);
	}

	public void emitUndoDeleteActionList(int columnId, int cardId, String name, User user) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.UNDO_DELETE_ACTION_LIST));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.UNDO_DELETE_ACTION_LIST));
		apiHookService.undeletedActionList(cardId, name, user, LavagnaEvent.UNDO_DELETE_ACTION_LIST);
	}

	// ------------
	public void emitUploadFile(int columnId, int cardId, List<String> fileNames, User user) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.CREATE_FILE));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.CREATE_FILE));
		apiHookService.uploadedFile(cardId, fileNames, user, LavagnaEvent.CREATE_FILE);
	}

	public void emitDeleteFile(int columnId, int cardId, String fileName, User user) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.DELETE_FILE));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.DELETE_FILE));
		apiHookService.deletedFile(cardId, fileName, user, LavagnaEvent.DELETE_FILE);
	}

	public void emiteUndoDeleteFile(int columnId, int cardId, String fileName, User user) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.UNDO_DELETE_FILE));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.UNDO_DELETE_FILE));
		apiHookService.undoDeletedFile(cardId, fileName, user, LavagnaEvent.UNDO_DELETE_FILE);
	}

	// ------------

	private static Triple<Set<Integer>, Set<Integer>, Set<String>> extractFrom(List<CardFull> l) {
		Set<Integer> cardIds = new HashSet<>();
		Set<Integer> columnIds = new HashSet<>();
		Set<String> projectShortNames = new HashSet<>();
		for (CardFull cf : l) {
			cardIds.add(cf.getId());
			columnIds.add(cf.getColumnId());
			projectShortNames.add(cf.getProjectShortName());
		}
		return Triple.of(cardIds, columnIds, projectShortNames);
	}

	public void emitRemoveLabelValueToCards(List<CardFull> affectedCards, int labelId, LabelValue labelValue, User user) {
		sendEventForLabel(affectedCards, LavagnaEvent.REMOVE_LABEL_VALUE);
		apiHookService.removedLabelValueToCards(affectedCards, labelId, labelValue, user, LavagnaEvent.REMOVE_LABEL_VALUE);
	}

	private void sendEventForLabel(List<CardFull> affectedCards, LavagnaEvent ev) {
		Triple<Set<Integer>, Set<Integer>, Set<String>> a = extractFrom(affectedCards);
		for (int cardId : a.getLeft()) {
			messagingTemplate.convertAndSend(cardData(cardId), event(ev));
		}
		for (int columnId : a.getMiddle()) {
			messagingTemplate.convertAndSend(column(columnId), event(ev));
		}
		for (String projectShortName : a.getRight()) {
			messagingTemplate.convertAndSend("/event/project/" + projectShortName + "/label-value", event(ev));
		}
	}

	public void emitAddLabelValueToCards(List<CardFull> affectedCards, int labelId, LabelValue labelValue, User user) {
		sendEventForLabel(affectedCards, LavagnaEvent.ADD_LABEL_VALUE_TO_CARD);
		apiHookService.addLabelValueToCards(affectedCards, labelId, labelValue, user, LavagnaEvent.ADD_LABEL_VALUE_TO_CARD);
	}

	public void emitUpdateOrAddValueToCards(List<CardFull> updated, List<CardFull> added, int labelId, LabelValue labelValue, User user) {
		sendEventForLabel(updated, LavagnaEvent.UPDATE_LABEL_VALUE);
		sendEventForLabel(added, LavagnaEvent.ADD_LABEL_VALUE_TO_CARD);

		apiHookService.updateLabelValueToCards(updated, labelId, labelValue, user, LavagnaEvent.UPDATE_LABEL_VALUE);
		apiHookService.addLabelValueToCards(added, labelId, labelValue, user, LavagnaEvent.ADD_LABEL_VALUE_TO_CARD);

	}

	public void emitAddLabel(String projectShortName) {
		messagingTemplate.convertAndSend("/event/project/" + projectShortName + "/label", event(LavagnaEvent.ADD_LABEL));
		emitProjectMetadataHasChanged(projectShortName);
	}

	public void emitUpdateLabel(String projectShortName, int labelId) {
		messagingTemplate.convertAndSend("/event/project/" + projectShortName + "/label", event(LavagnaEvent.UPDATE_LABEL, labelId));
		emitProjectMetadataHasChanged(projectShortName);
	}

	public void emitDeleteLabel(String projectShortName, int labelId) {
		messagingTemplate.convertAndSend("/event/project/" + projectShortName + "/label", event(LavagnaEvent.DELETE_LABEL, labelId));
		emitProjectMetadataHasChanged(projectShortName);
	}


	public void emitUpdateLabeListValueId(int labelListValueId) {
		messagingTemplate.convertAndSend("/event/label-list-values/" + labelListValueId, event(LavagnaEvent.UPDATE_LABEL_LIST_VALUE, labelListValueId));
	}

	// user profile update
	public void emitUpdateUserProfile(int userId) {
		messagingTemplate.convertAndSend("/event/user", event(LavagnaEvent.UPDATE_USER, userId));
	}


	private void emitProjectMetadataHasChanged(String projectShortName) {
        messagingTemplate.convertAndSend("/event/project/" + projectShortName, LavagnaEvent.PROJECT_METADATA_HAS_CHANGED);
    }

	public enum LavagnaEvent {
		CREATE_PROJECT, CREATE_BOARD, //
		UPDATE_PROJECT, UPDATE_BOARD, //
		CREATE_COLUMN, UPDATE_COLUMN, UPDATE_COLUMN_POSITION, //
		CREATE_CARD, UPDATE_CARD, //
		UPDATE_CARD_POSITION, //

		//
		PROJECT_METADATA_HAS_CHANGED,
		//

		// role and permission
		CREATE_ROLE, DELETE_ROLE, UPDATE_PERMISSION_TO_ROLE, ASSIGN_ROLE_TO_USERS, REMOVE_ROLE_TO_USERS,
		//
		UPDATE_DESCRIPTION,
		//
		CREATE_COMMENT, UPDATE_COMMENT, DELETE_COMMENT, UNDO_DELETE_COMMENT,
		//
		CREATE_ACTION_LIST, DELETE_ACTION_LIST, UPDATE_ACTION_LIST, REORDER_ACTION_LIST, UNDO_DELETE_ACTION_LIST,
		//
		CREATE_ACTION_ITEM, DELETE_ACTION_ITEM, TOGGLE_ACTION_ITEM, UPDATE_ACTION_ITEM, MOVE_ACTION_ITEM, REORDER_ACTION_ITEM, UNDO_DELETE_ACTION_ITEM,
		//
		CREATE_FILE, DELETE_FILE, UNDO_DELETE_FILE,
		//
		ADD_LABEL_VALUE_TO_CARD, UPDATE_LABEL_VALUE, REMOVE_LABEL_VALUE, UNDO_REMOVE_LABEL_VALUE, ADD_LABEL, UPDATE_LABEL, DELETE_LABEL,
		//
		UPDATE_USER, UPDATE_LABEL_LIST_VALUE;
	}

	// ------------

	public static class Event {
		private final LavagnaEvent type;
		private final Object payload;

        @java.beans.ConstructorProperties({ "type", "payload" }) private Event(LavagnaEvent type,
            Object payload) {
            this.type = type;
            this.payload = payload;
        }

        public LavagnaEvent getType() {
            return this.type;
        }

        public Object getPayload() {
            return this.payload;
        }
    }

	public static class ImportEvent {
		private final int currentBoard;
		private final int boards;
		private final String boardName;

        @java.beans.ConstructorProperties({ "currentBoard", "boards", "boardName" }) private ImportEvent(
            int currentBoard,
            int boards, String boardName) {
            this.currentBoard = currentBoard;
            this.boards = boards;
            this.boardName = boardName;
        }

        public int getCurrentBoard() {
            return this.currentBoard;
        }

        public int getBoards() {
            return this.boards;
        }

        public String getBoardName() {
            return this.boardName;
        }
    }
}
