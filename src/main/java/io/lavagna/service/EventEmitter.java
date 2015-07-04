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
import io.lavagna.model.CardFull;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Component
public class EventEmitter {

	private final SimpMessageSendingOperations messagingTemplate;

	@Autowired
	public EventEmitter(SimpMessageSendingOperations messageSendingOperations) {
		this.messagingTemplate = messageSendingOperations;
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

	public void emitCreateProject(String projectShortName) {
		messagingTemplate.convertAndSend("/event/project", event(LavagnaEvent.CREATE_PROJECT, projectShortName));
	}

	public void emitUpdateProject(String projectShortName) {
		messagingTemplate.convertAndSend("/event/project", event(LavagnaEvent.UPDATE_PROJECT, projectShortName));
	}

	public void emitImportProject(String importId, int currentBoard, int boards, String boardName) {
		messagingTemplate.convertAndSend("/event/import/" + importId, importEvent(currentBoard, boards, boardName));
	}

	// ------------ board

	public void emitCreateBoard(String projectShortName) {
		messagingTemplate.convertAndSend("/event/project/" + projectShortName + "/board",
				event(LavagnaEvent.CREATE_BOARD));
	}

	public void emitUpdateBoard(String boardShortName) {
		messagingTemplate.convertAndSend("/event/board/" + boardShortName, event(LavagnaEvent.UPDATE_BOARD));
	}

	// ------------ column

	public void emitCreateColumn(String boardShortName, BoardColumnLocation location) {
		messagingTemplate
				.convertAndSend(columnDestination(boardShortName, location), event(LavagnaEvent.CREATE_COLUMN));
	}

	public void emitUpdateColumn(String boardShortName, BoardColumnLocation location, int columnId) {
		messagingTemplate
				.convertAndSend(columnDestination(boardShortName, location), event(LavagnaEvent.UPDATE_COLUMN));
		messagingTemplate.convertAndSend("/event/column/" + columnId, event(LavagnaEvent.UPDATE_COLUMN));
	}

	public void emitUpdateColumnPosition(String boardShortName, BoardColumnLocation location) {
		messagingTemplate.convertAndSend(columnDestination(boardShortName, location),
				event(LavagnaEvent.UPDATE_COLUMN_POSITION));
	}

	// ------------ card

	public void emitCreateCard(String projectShortName, String boardShortName, int columnId, int cardId) {
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.CREATE_CARD));
		messagingTemplate.convertAndSend(board(projectShortName, boardShortName),
				event(LavagnaEvent.CREATE_CARD, cardId));
	}

	public void emitUpdateCard(String projectShortName, String boardShortName, int columnId, int cardId) {
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.UPDATE_CARD));
		messagingTemplate.convertAndSend(board(projectShortName, boardShortName),
				event(LavagnaEvent.UPDATE_CARD, cardId));
	}

	public void emitUpdateCardPosition(int columnId) {
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.UPDATE_CARD_POSITION));
	}

	public void emitMoveCardOutsideOfBoard(String boardShortName, BoardColumnLocation location) {
		messagingTemplate.convertAndSend("/event/board/" + boardShortName + "/location/" + location + "/card",
				event(LavagnaEvent.UPDATE_CARD_POSITION));
	}

	public void emitMoveCardFromOutsideOfBoard(String boardShortName, BoardColumnLocation location) {
		messagingTemplate.convertAndSend("/event/board/" + boardShortName + "/location/" + location + "/card",
				event(LavagnaEvent.UPDATE_CARD_POSITION));
	}

	public void emitCardHasMoved(String projectShortName, String boardShortName, Collection<Integer> affected) {
		for (Integer a : affected) {
			messagingTemplate.convertAndSend(board(projectShortName, boardShortName),
					event(LavagnaEvent.UPDATE_CARD_POSITION, a));
		}
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
	public void emitUpdateDescription(int columnId, int cardId) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.UPDATE_DESCRIPTION));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.UPDATE_DESCRIPTION));
	}

	// ------------ comment
	public void emitCreateComment(int columnId, int cardId) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.CREATE_COMMENT));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.CREATE_COMMENT));
	}

	public void emitUpdateComment(int cardId) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.UPDATE_COMMENT));
	}

	public void emitDeleteComment(int columnId, int cardId) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.DELETE_COMMENT));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.DELETE_COMMENT));
	}

	public void emitUndoDeleteComment(int columnId, int cardId) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.UNDO_DELETE_COMMENT));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.UNDO_DELETE_COMMENT));
	}

	// ------------ action list handling

	public void emitCreateActionList(int cardId) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.CREATE_ACTION_LIST));
	}

	public void emitDeleteActionList(int columnId, int cardId) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.DELETE_ACTION_LIST));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.DELETE_ACTION_LIST));
	}

	public void emitUpdateActionList(int cardId) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.UPDATE_ACTION_LIST));
	}

	public void emitReorderActionLists(int cardId) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.REORDER_ACTION_LIST));
	}

	public void emitCreateActionItem(int columnId, int cardId) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.CREATE_ACTION_ITEM));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.REORDER_ACTION_LIST));
	}

	public void emitDeleteActionItem(int columnId, int cardId) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.DELETE_ACTION_ITEM));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.DELETE_ACTION_ITEM));
	}

	public void emitToggleActionItem(int columnId, int cardId) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.TOGGLE_ACTION_ITEM));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.TOGGLE_ACTION_ITEM));
	}

	public void emitUpdateUpdateActionItem(int cardId) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.UPDATE_ACTION_ITEM));
	}

	public void emitMoveActionItem(int cardId) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.MOVE_ACTION_ITEM));
	}

	public void emitReorderActionItems(int cardId) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.REORDER_ACTION_ITEM));
	}

	public void emiteUndoDeleteActionItem(int columnId, int cardId) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.UNDO_DELETE_ACTION_ITEM));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.UNDO_DELETE_ACTION_ITEM));
	}

	public void emitUndoDeleteActionList(int columnId, int cardId) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.UNDO_DELETE_ACTION_LIST));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.UNDO_DELETE_ACTION_LIST));

	}

	// ------------
	public void emitUploadFile(int columnId, int cardId) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.CREATE_FILE));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.CREATE_FILE));
	}

	public void emitDeleteFile(int columnId, int cardId) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.DELETE_FILE));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.DELETE_FILE));
	}

	public void emiteUndoDeleteFile(int columnId, int cardId) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.UNDO_DELETE_FILE));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.UNDO_DELETE_FILE));
	}

	// ------------
	public void emitAddLabelValueToCard(String projectShortName, int columnId, int cardId) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.ADD_LABEL_VALUE_TO_CARD));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.ADD_LABEL_VALUE_TO_CARD));
		messagingTemplate.convertAndSend("/event/project/" + projectShortName + "/label-value",
				event(LavagnaEvent.ADD_LABEL_VALUE_TO_CARD));
	}

	public void emitUpdateLabelValue(String projectShortName, int columnId, int cardId) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.UPDATE_LABEL_VALUE));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.UPDATE_LABEL_VALUE));
		messagingTemplate.convertAndSend("/event/project/" + projectShortName + "/label-value",
				event(LavagnaEvent.UPDATE_LABEL_VALUE));
	}

	public void emitRemoveLabelValue(String projectShortName, int columnId, int cardId) {
		messagingTemplate.convertAndSend(cardData(cardId), event(LavagnaEvent.REMOVE_LABEL_VALUE));
		messagingTemplate.convertAndSend(column(columnId), event(LavagnaEvent.REMOVE_LABEL_VALUE));
		messagingTemplate.convertAndSend("/event/project/" + projectShortName + "/label-value",
				event(LavagnaEvent.REMOVE_LABEL_VALUE));
	}

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

	public void emitRemoveLabelValueToCards(List<CardFull> affectedCards) {
		sendEventForLabel(affectedCards, LavagnaEvent.REMOVE_LABEL_VALUE);
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

	public void emitAddLabelValueToCards(List<CardFull> affectedCards) {
		sendEventForLabel(affectedCards, LavagnaEvent.ADD_LABEL_VALUE_TO_CARD);
	}

	public void emitUpdateOrAddValueToCards(List<CardFull> updated, List<CardFull> added) {
		sendEventForLabel(updated, LavagnaEvent.UPDATE_LABEL_VALUE);
		sendEventForLabel(added, LavagnaEvent.ADD_LABEL_VALUE_TO_CARD);
	}

	public void emitAddLabel(String projectShortName) {
		messagingTemplate
				.convertAndSend("/event/project/" + projectShortName + "/label", event(LavagnaEvent.ADD_LABEL));
	}

	public void emitUpdateLabel(String projectShortName, int labelId) {
		messagingTemplate.convertAndSend("/event/project/" + projectShortName + "/label",
				event(LavagnaEvent.UPDATE_LABEL, labelId));
	}

	public void emitDeleteLabel(String projectShortName, int labelId) {
		messagingTemplate.convertAndSend("/event/project/" + projectShortName + "/label",
				event(LavagnaEvent.DELETE_LABEL, labelId));
	}
	

	public void emitUpdateLabeListValueId(int labelListValueId) {
		messagingTemplate.convertAndSend("/event/label-list-values/" + labelListValueId, event(LavagnaEvent.UPDATE_LABEL_LIST_VALUE, labelListValueId));
	}

	// user profile update
	public void emitUpdateUserProfile(int userId) {
		messagingTemplate.convertAndSend("/event/user", event(LavagnaEvent.UPDATE_USER, userId));
	}

	private enum LavagnaEvent {
		CREATE_PROJECT, CREATE_BOARD, //
		UPDATE_PROJECT, UPDATE_BOARD, //
		CREATE_COLUMN, UPDATE_COLUMN, UPDATE_COLUMN_POSITION, //
		CREATE_CARD, UPDATE_CARD, //
		UPDATE_CARD_POSITION, //

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

	@Getter
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Event {
		private final LavagnaEvent type;
		private final Object payload;
	}

	@Getter
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class ImportEvent {
		private final int currentBoard;
		private final int boards;
		private final String boardName;
	}

}
