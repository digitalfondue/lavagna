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

import com.julienvey.trello.Trello;
import com.julienvey.trello.domain.*;
import com.julienvey.trello.domain.Board;
import com.julienvey.trello.domain.Card;
import com.julienvey.trello.domain.Label;
import com.julienvey.trello.impl.TrelloImpl;
import io.lavagna.model.*;
import io.lavagna.web.api.model.TrelloImportRequest;
import io.lavagna.web.api.model.TrelloImportRequest.BoardIdAndShortName;
import io.lavagna.web.api.model.TrelloRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static io.lavagna.common.Constants.SYSTEM_LABEL_ASSIGNED;
import static io.lavagna.common.Constants.SYSTEM_LABEL_DUE_DATE;

@Service
public class ImportService {

	private static final Logger LOG = LogManager.getLogger();

	private final EventEmitter eventEmitter;
	private final ProjectService projectService;
	private final BoardRepository boardRepository;
	private final BoardColumnRepository boardColumnRepository;
	private final CardDataService cardDataService;
	private final CardService cardService;
	private final LabelService labelService;
	private final CardLabelRepository cardLabelRepository;
	private final UserRepository userRepository;

	public ImportService(EventEmitter eventEmitter, ProjectService projectService, BoardRepository boardRepository,
			BoardColumnRepository boardColumnRepository, CardDataService cardDataService, CardService cardService,
			LabelService labelService, CardLabelRepository cardLabelRepository, UserRepository userRepository) {
		this.eventEmitter = eventEmitter;
		this.projectService = projectService;
		this.boardRepository = boardRepository;
		this.boardColumnRepository = boardColumnRepository;
		this.cardDataService = cardDataService;
		this.cardService = cardService;
		this.labelService = labelService;
		this.cardLabelRepository = cardLabelRepository;
		this.userRepository = userRepository;
	}

	public TrelloBoardsResponse getAvailableTrelloBoards(TrelloRequest request) {
		TrelloBoardsResponse response = new TrelloBoardsResponse();
		Trello trello = new TrelloImpl(request.getApiKey(), request.getSecret());
		Member member = trello.getMemberInformation("me");
		Map<String, TrelloOrganizationInfo> organizations = new HashMap<>();
		for (String boardId : member.getIdBoards()) {
			Board board = trello.getBoard(boardId);
			if (board.isClosed()) {
				continue;
			}
			String orgId = board.getIdOrganization();
			if (!organizations.containsKey(orgId)) {
				TrelloOrganizationInfo tOrg;
				if (orgId != null) {
					Organization organization = board.fetchOrganization();
					tOrg = new TrelloOrganizationInfo(orgId, organization.getDisplayName());
				} else {
					tOrg = new TrelloOrganizationInfo(orgId, "Personal");
				}
				organizations.put(orgId, tOrg);
				response.organizations.add(tOrg);
			}
			TrelloOrganizationInfo tOrg = organizations.get(orgId);
			tOrg.boards.add(new TrelloBoardInfo(board.getId(), board.getName()));
		}
		return response;
	}

	public TrelloImportResponse importFromTrello(TrelloImportRequest importRequest, User user) {
		Trello trello = new TrelloImpl(importRequest.getApiKey(), importRequest.getSecret());

		// Cache the user mappings
		Map<String, User> lavagnaUsers = new HashMap<>();

		TrelloImportResponse tImport = new TrelloImportResponse();
		int boardsToImport = importRequest.getBoards().size() + 1;
		int currentBoard = 0;

		for (BoardIdAndShortName boardIdAndShortName : importRequest.getBoards()) {
			currentBoard++;
			Board board = trello.getBoard(boardIdAndShortName.getId());
			String boardShortName = boardIdAndShortName.getShortName().toUpperCase(Locale.ENGLISH);
			if (board.isClosed() || boardRepository.existsWithShortName(boardShortName)) {
				continue;
			}

			eventEmitter.emitImportProject(importRequest.getImportId(), currentBoard, boardsToImport, board.getName(), user);

			TrelloBoard tBoard = new TrelloBoard(boardShortName, board.getName(), board.getDesc());
			importBoard(trello, tImport, board, tBoard, lavagnaUsers, importRequest.isImportArchived());
			tImport.boards.add(tBoard);
		}
		eventEmitter.emitImportProject(importRequest.getImportId(), boardsToImport, boardsToImport, "", user);
		return tImport;
	}

	private void importBoard(Trello trello, TrelloImportResponse tImport, Board board, TrelloBoard tBoard,
			Map<String, User> lavagnaUsers, boolean importArchived) {

		// Cache the checklists
		Map<String, CheckList> checklists = new HashMap<>();
		for (CheckList checklist : trello.getBoardChecklists(board.getId())) {
			checklists.put(checklist.getId(), checklist);
		}

		// Cache the board members
		Map<String, Member> boardMembers = new HashMap<>();
		for (Member member : trello.getBoardMembers(board.getId())) {
			boardMembers.put(member.getId(), member);
		}

		// Fetch the columns with every card in it
		for (TList list : board.fetchLists(new Argument("cards", importArchived ? "all" : "open"))) {
			TrelloBoardColumn tColumn = new TrelloBoardColumn(list.getName());
			tBoard.columns.put(list.getPos(), tColumn);

			for (Card iCard : list.getCards()) {
				if (StringUtils.isEmpty(iCard.getName())) {
					continue;
				}

				TrelloCard tCard = new TrelloCard(iCard.getName(), iCard.getDesc(), iCard.isClosed(), iCard.getDue());

				// Checklists
				for (String checklistId : iCard.getIdChecklists()) {
					CheckList checklist = checklists.get(checklistId);
					TrelloChecklist tChecklist = new TrelloChecklist(checklist.getName());
					for (CheckItem iItem : checklist.getCheckItems()) {
						TrelloChecklistItem item = new TrelloChecklistItem(iItem.getName(),
								iItem.getState().equals("complete"));
						tChecklist.items.put(iItem.getPos(), item);
					}
					tCard.checklists.add(tChecklist);
				}

				// Comments
				for (Action action : iCard.getActions(new Argument("filter", "commentCard"))) {
					tCard.comments.add(new TrelloComment(action.getDate(),
							getMemberFromId(lavagnaUsers, boardMembers, action.getIdMemberCreator()),
							action.getData().getText()));
				}

				// Assigned users
				for (String memberId : iCard.getIdMembers()) {
					User user = getMemberFromId(lavagnaUsers, boardMembers, memberId);
					if (user != null) {
						tCard.assignedUsers.add(user);
					}
				}

				// Labels
				for (Label label : iCard.getLabels()) {
					addLabelToCard(tCard, tImport, label);
				}

				tColumn.cards.put(iCard.getPos(), tCard);
			}
		}
	}

	private void addLabelToCard(TrelloCard card, TrelloImportResponse tImport, Label label) {
		String name = label.getName();
		if (StringUtils.isBlank(name)) {
			name = label.getColor();
		}
		if (!tImport.labels.containsKey(name)) {
			tImport.labels.put(name, label.getColor());
		}
		card.labels.add(name);
	}

	private User getMemberFromId(Map<String, User> lavagnaUsers, Map<String, Member> boardMembers, String memberId) {
		if (lavagnaUsers.containsKey(memberId)) {
			return lavagnaUsers.get(memberId);
		}
		Member member = boardMembers.get(memberId);
		User user = matchUser(lavagnaUsers, memberId, member.getUsername());
		if (user != null)
			return user;

		user = matchUser(lavagnaUsers, memberId, member.getEmail());
		if (user != null)
			return user;
		return matchUser(lavagnaUsers, memberId, member.getFullName());
	}

	private User matchUser(Map<String, User> lavagnaUsers, String memberId, String criteria) {
		if (StringUtils.isNotBlank(memberId)) {
			List<User> users = userRepository.findUsers(criteria);
			if (users.size() > 0) {
				lavagnaUsers.put(memberId, users.get(0));
				return users.get(0);
			}
		}
		return null;
	}

	@Transactional(readOnly = false)
	public void saveTrelloBoardsToDb(String projectShortName, TrelloImportResponse tImport, User user) {
		int projectId = projectService.findIdByShortName(projectShortName);
		List<BoardColumnDefinition> definitions = projectService.findColumnDefinitionsByProjectId(projectId);
		BoardColumnDefinition openDefinition = null;
		for (BoardColumnDefinition def : definitions) {
			if (openDefinition == null || openDefinition.getValue() != ColumnDefinition.OPEN) {
				openDefinition = def;
			}
		}

		if (openDefinition == null) {
			LOG.warn("no valid column definition has been found for the project {}.", projectShortName);
			return;
		}

		// Labels
		Map<String, CardLabel> lavagnaLabels = new HashMap<>();
		for (CardLabel cl : cardLabelRepository.findLabelsByProject(projectId)) {
			if (cl.getDomain().equals(CardLabel.LabelDomain.USER)) {
				lavagnaLabels.put(cl.getName(), cl);
			}
		}

		for (String labelName : tImport.labels.keySet()) {
			if (!lavagnaLabels.containsKey(labelName)) {
				CardLabel cl = cardLabelRepository.addLabel(projectId, true, CardLabel.LabelType.NULL,
						CardLabel.LabelDomain.USER, labelName, getColorFromTrelloColor(tImport.labels.get(labelName)));
				lavagnaLabels.put(labelName, cl);
			}
		}

		CardLabel dueDateLabel = cardLabelRepository.findLabelByName(projectId, SYSTEM_LABEL_DUE_DATE,
				CardLabel.LabelDomain.SYSTEM);

		CardLabel assignedLabel = cardLabelRepository.findLabelByName(projectId, SYSTEM_LABEL_ASSIGNED,
				CardLabel.LabelDomain.SYSTEM);

		// Import the boards
		for (TrelloBoard tBoard : tImport.boards) {
			int boardId = boardRepository.createNewBoard(tBoard.getName(), tBoard.getShortName(), tBoard.getDesc(), projectId).getId();
			for (Integer trelloColumnPos : tBoard.columns.keySet()) {
				TrelloBoardColumn trelloColumn = tBoard.columns.get(trelloColumnPos);

				int boardColumnId = boardColumnRepository.addColumnToBoard(trelloColumn.name, openDefinition.getId(),
						BoardColumn.BoardColumnLocation.BOARD, boardId).getId();

				for (Integer pos : trelloColumn.cards.keySet()) {
					TrelloCard card = trelloColumn.cards.get(pos);
					importCard(card, boardId, boardColumnId, user, lavagnaLabels, dueDateLabel, assignedLabel);
				}
			}
		}
	}

	private int getColorFromTrelloColor(String color) {
		switch (color) {
		case "yellow":
			return 16763904;
		case "red":
			return 15012864;
		case "orange":
			return 16733986;
		case "green":
			return 2464548;
		case "pink":
			return 15102392;
		case "purple":
			return 10233776;
		case "sky":
			return 48340;
		case "lime":
			return 13491257;
		case "black":
			return 0;
		default: //case "blue":
			return 1810914;
		}
	}

	private void importCard(TrelloCard card, int boardId, int boardColumnId, User user,
			Map<String, CardLabel> lavagnaLabels, CardLabel dueDateLabel, CardLabel assignedLabel) {
		int cardId = cardService.createCard(card.getName(), boardColumnId, new Date(), user).getId();
		if (StringUtils.isNotEmpty(card.getDesc())) {
			cardDataService.updateDescription(cardId, card.getDesc(), new Date(), user.getId());
		}

		// Due date
		if (card.getDueDate() != null) {
			labelService.addLabelValueToCard(dueDateLabel, cardId, new CardLabelValue.LabelValue(card.getDueDate()),
					user, new Date());
		}

		// Checklists
		for (TrelloChecklist checklist : card.checklists) {
			CardData data = cardDataService.createActionList(cardId, checklist.getName(), user.getId(), new Date());
			for (Integer pos : checklist.items.keySet()) {
				TrelloChecklistItem item = checklist.items.get(pos);
				CardData itemData = cardDataService.createActionItem(cardId, data.getId(), item.name, user.getId(), new Date());
				if (item.isChecked) {
					cardDataService.toggleActionItem(itemData.getId(), true, user.getId(), new Date());
				}
			}
		}

		// Comments
		for (TrelloComment comment : card.comments) {
			cardDataService.createComment(cardId, comment.getText(), comment.getDate(),
                (comment.getUser() != null ? comment.getUser() : user).getId());
		}

		// Archive if closed
		if (card.isClosed()) {
			BoardColumn destination = boardColumnRepository.findDefaultColumnFor(boardId,
					BoardColumn.BoardColumnLocation.ARCHIVE);
			List<Integer> idToList = new ArrayList<>();
			idToList.add(cardId);
			cardService.moveCardsToColumn(idToList, boardColumnId, destination.getId(), user.getId(),
					Event.EventType.CARD_ARCHIVE, new Date());
		}

		// Assigned users
		for (User assignedUser : card.assignedUsers) {
			labelService.addLabelValueToCard(assignedLabel, cardId, new CardLabelValue.LabelValue(assignedUser.getId()),
					user, new Date());
		}

		// Labels
		for (String labelName : card.labels) {
			labelService.addLabelValueToCard(lavagnaLabels.get(labelName), cardId, new CardLabelValue.LabelValue(),
					user, new Date());
		}
	}

	static class TrelloChecklistItem {
		private final String name;
		private final boolean isChecked;

        @java.beans.ConstructorProperties({ "name", "isChecked" }) public TrelloChecklistItem(String name,
            boolean isChecked) {
            this.name = name;
            this.isChecked = isChecked;
        }
    }

	static class TrelloChecklist {
		private final String name;

		private final Map<Integer, TrelloChecklistItem> items = new TreeMap<>();

		public TrelloChecklist(String name) {
			this.name = name;
		}

        public String getName() {
            return this.name;
        }

        public Map<Integer, TrelloChecklistItem> getItems() {
            return this.items;
        }
    }

	static class TrelloComment {
		private final Date date;
		private final User user;
		private final String text;

        @java.beans.ConstructorProperties({ "date", "user", "text" }) public TrelloComment(Date date, User user,
            String text) {
            this.date = date;
            this.user = user;
            this.text = text;
        }

        public Date getDate() {
            return this.date;
        }

        public User getUser() {
            return this.user;
        }

        public String getText() {
            return this.text;
        }
    }

	static class TrelloCard {
		private final String name;
		private final String desc;
		private final boolean isClosed;
		private final Date dueDate;

		private final List<TrelloComment> comments = new ArrayList<>();
		private final List<TrelloChecklist> checklists = new ArrayList<>();
		private final List<User> assignedUsers = new ArrayList<>();
		private final List<String> labels = new ArrayList<>();

		public TrelloCard(String name, String desc, boolean isClosed, Date dueDate) {
			this.name = name;
			this.desc = desc;
			this.isClosed = isClosed;
			this.dueDate = dueDate;
		}

        public String getName() {
            return this.name;
        }

        public String getDesc() {
            return this.desc;
        }

        public boolean isClosed() {
            return this.isClosed;
        }

        public Date getDueDate() {
            return this.dueDate;
        }

        public List<TrelloComment> getComments() {
            return this.comments;
        }

        public List<TrelloChecklist> getChecklists() {
            return this.checklists;
        }

        public List<User> getAssignedUsers() {
            return this.assignedUsers;
        }

        public List<String> getLabels() {
            return this.labels;
        }
    }

	static class TrelloBoardColumn {
		private final String name;

		private final Map<Integer, TrelloCard> cards = new TreeMap<>();

		public TrelloBoardColumn(String name) {
			this.name = name;
		}

        public String getName() {
            return this.name;
        }

        public Map<Integer, TrelloCard> getCards() {
            return this.cards;
        }
    }

	public static class TrelloBoard {
		private final String shortName;
		private final String name;
		private final String desc;

		private final Map<Integer, TrelloBoardColumn> columns = new TreeMap<>();

		public TrelloBoard(String shortName, String name, String desc) {
			this.shortName = shortName;
			this.name = name;
			this.desc = desc;
		}

        public String getShortName() {
            return this.shortName;
        }

        public String getName() {
            return this.name;
        }

        public String getDesc() {
            return this.desc;
        }

        public Map<Integer, TrelloBoardColumn> getColumns() {
            return this.columns;
        }
    }

	public static class TrelloImportResponse {
		private final List<TrelloBoard> boards = new ArrayList<>();

		private final Map<String, String> labels = new HashMap<>();

        public List<TrelloBoard> getBoards() {
            return this.boards;
        }

        public Map<String, String> getLabels() {
            return this.labels;
        }
    }

	public static class TrelloBoardsResponse {
		private final List<TrelloOrganizationInfo> organizations = new ArrayList<>();
	}

	public static class TrelloBoardInfo {
		private final String id;
		private final String name;

        @java.beans.ConstructorProperties({ "id", "name" }) public TrelloBoardInfo(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }
    }

	public static class TrelloOrganizationInfo {
		private final String id;
		private final String name;
		private final List<TrelloBoardInfo> boards = new ArrayList<>();

		public TrelloOrganizationInfo(String id, String name) {
			this.id = id;
			this.name = name;
		}

        public String getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public List<TrelloBoardInfo> getBoards() {
            return this.boards;
        }
    }
}
