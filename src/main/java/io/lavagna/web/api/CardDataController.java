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

import io.lavagna.model.CardData;
import io.lavagna.model.CardDataFull;
import io.lavagna.model.CardType;
import io.lavagna.model.Event;
import io.lavagna.model.Event.EventType;
import io.lavagna.model.FileDataLight;
import io.lavagna.model.Key;
import io.lavagna.model.Permission;
import io.lavagna.model.User;
import io.lavagna.service.CardDataRepository;
import io.lavagna.service.CardDataService;
import io.lavagna.service.CardRepository;
import io.lavagna.service.ConfigurationRepository;
import io.lavagna.service.EventEmitter;
import io.lavagna.service.EventRepository;
import io.lavagna.web.helper.CardCommentOwnershipChecker;
import io.lavagna.web.helper.ExpectPermission;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class CardDataController {

	private static final Logger LOG = LogManager.getLogger();

	private final CardDataService cardDataService;
	private final CardDataRepository cardDataRepository;
	private final CardRepository cardRepository;
	private final EventRepository eventRepository;
	private final EventEmitter eventEmitter;
	private final ConfigurationRepository configurationRepository;

	@Autowired
	public CardDataController(CardDataService cardDataService, CardDataRepository cardDataRepository,
			CardRepository cardRepository, ConfigurationRepository configurationRepository,
			EventRepository eventRepository, EventEmitter eventEmitter) {
		this.cardDataService = cardDataService;
		this.cardDataRepository = cardDataRepository;
		this.cardRepository = cardRepository;
		this.eventRepository = eventRepository;
		this.eventEmitter = eventEmitter;
		this.configurationRepository = configurationRepository;
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/card/{cardId}/data", method = RequestMethod.GET)
	@ResponseBody
	public List<CardData> findAllLightByCardId(@PathVariable("cardId") int cardId) {
		return cardDataRepository.findAllDataLightByCardId(cardId);
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/card/{cardId}/description", method = RequestMethod.GET)
	@ResponseBody
	public CardDataHistory description(@PathVariable("cardId") int cardId) {
		List<CardDataFull> descriptions = cardDataService.findDescriptionByCardId(cardId);
		// look for duplicates, the event model will keep the entire history of
		// the description
		CardDataHistory description = null;
		for (CardDataFull des : descriptions) {
			if (description == null) {
				description = new CardDataHistory(des.getId(), des.getContent(), des.getOrder(), des.getUserId(),
						des.getTime(), des.getUserId(), des.getTime());
			}

			if (des.getEventType() == EventType.DESCRIPTION_CREATE) {
				description.userId = des.getUserId();
				description.time = des.getTime();
			}
			if (des.getEventType() == EventType.DESCRIPTION_UPDATE) {
				description.updatedCount++;
				// never null because the object's fields are always initialized
				if (des.getTime().getTime() > description.getUpdateDate().getTime()) {
					description.updateUser = des.getUserId();
					description.updateDate = des.getTime();
				}
			}
		}
		return description;
	}

	@ExpectPermission(Permission.UPDATE_CARD)
	@RequestMapping(value = "/api/card/{cardId}/description", method = RequestMethod.POST)
	@ResponseBody
	public int updateDescription(@PathVariable("cardId") int cardId, @RequestBody Content content, User user) {
		int result = cardDataService.updateDescription(cardId, content.content, new Date(), user);
		eventEmitter.emitUpdateDescription(cardRepository.findBy(cardId).getColumnId(), cardId);
		return result;
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/card/{cardId}/comments", method = RequestMethod.GET)
	@ResponseBody
	public List<CardDataHistory> findAllComments(@PathVariable("cardId") int cardId) {
		List<CardDataFull> comments = cardDataService.findAllCommentsByCardId(cardId);
		// look for duplicates, the event model will keep the entire history of
		// the comment
		Map<Integer, CardDataHistory> duplicates = new HashMap<>();
		for (CardDataFull comment : comments) {
			if (!duplicates.containsKey(comment.getId())) {
				CardDataHistory newComment = new CardDataHistory(comment.getId(), comment.getContent(),
						comment.getOrder(), comment.getUserId(), comment.getTime(), comment.getUserId(),
						comment.getTime());
				duplicates.put(comment.getId(), newComment);
			}
			CardDataHistory instance = duplicates.get(comment.getId());

			if (comment.getEventType() == EventType.COMMENT_CREATE) {
				instance.userId = comment.getUserId();
				instance.time = comment.getTime();
			}
			if (comment.getEventType() == EventType.COMMENT_UPDATE) {
				instance.updatedCount++;
				// never null because the object's fields are always initialized
				if (comment.getTime().getTime() > instance.updateDate.getTime()) {
					instance.updateUser = comment.getUserId();
					instance.updateDate = comment.getTime();
				}
			}
			duplicates.put(comment.getId(), instance);
		}
		return new ArrayList<CardDataHistory>(duplicates.values());
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/card/{cardId}/actionlists", method = RequestMethod.GET)
	@ResponseBody
	public List<CardData> findAllActionLists(@PathVariable("cardId") int cardId) {
		return cardDataService.findAllActionListsAndItemsByCardId(cardId);
	}

	@ExpectPermission(Permission.CREATE_CARD_COMMENT)
	@RequestMapping(value = "/api/card/{cardId}/comment", method = RequestMethod.POST)
	@ResponseBody
	public CardData createComment(@PathVariable("cardId") int cardId, @RequestBody Content commentData, User user) {
		CardData comment = cardDataService.createComment(cardId, commentData.content, new Date(), user);
		eventEmitter.emitCreateComment(cardRepository.findBy(cardId).getColumnId(), cardId);
		return comment;
	}

	@ExpectPermission(value = Permission.UPDATE_CARD_COMMENT, ownershipChecker = CardCommentOwnershipChecker.class)
	@RequestMapping(value = "/api/card-data/comment/{commentId}", method = RequestMethod.POST)
	@ResponseBody
	public void updateComment(@PathVariable("commentId") int commentId, @RequestBody Content content, User user) {
		cardDataService.updateComment(commentId, content.content, new Date(), user);
		eventEmitter.emitUpdateComment(cardDataRepository.getUndeletedDataLightById(commentId).getCardId());
	}

	@ExpectPermission(value = Permission.DELETE_CARD_COMMENT, ownershipChecker = CardCommentOwnershipChecker.class)
	@RequestMapping(value = "/api/card-data/comment/{commentId}", method = RequestMethod.DELETE)
	@ResponseBody
	public Event deleteComment(@PathVariable("commentId") int commentId, User user) {
		Event res = cardDataService.deleteComment(commentId, user, new Date());
		eventEmitter.emitDeleteComment(cardRepository.findBy(res.getCardId()).getColumnId(), res.getCardId());
		return res;
	}

	@ExpectPermission(value = Permission.DELETE_CARD_COMMENT, ownershipChecker = CardCommentOwnershipChecker.class)
	@RequestMapping(value = "/api/card-data/undo/{eventId}/comment", method = RequestMethod.POST)
	@ResponseBody
	public int undoDeleteComment(@PathVariable("eventId") int eventId, User user) {
		Event event = eventRepository.getEventById(eventId);
		Validate.isTrue(event.getEvent() == EventType.COMMENT_DELETE);

		cardDataService.undoDeleteComment(event);
		eventEmitter.emitUndoDeleteComment(cardRepository.findBy(event.getCardId()).getColumnId(), event.getCardId());

		return event.getDataId();
	}

	@ExpectPermission(Permission.MANAGE_ACTION_LIST)
	@RequestMapping(value = "/api/card/{cardId}/actionlist", method = RequestMethod.POST)
	@ResponseBody
	public CardData createActionList(@PathVariable("cardId") int cardId, @RequestBody Content actionListData, User user) {
		CardData actionList = cardDataService.createActionList(cardId, actionListData.content, user, new Date());
		eventEmitter.emitCreateActionList(cardId);
		return actionList;
	}

	@ExpectPermission(Permission.MANAGE_ACTION_LIST)
	@RequestMapping(value = "/api/card-data/actionlist/{actionListId}", method = RequestMethod.DELETE)
	@ResponseBody
	public Event deleteActionList(@PathVariable("actionListId") int actionListId, User user) {
		Event res = cardDataService.deleteActionList(actionListId, user, new Date());
		eventEmitter.emitDeleteActionList(cardRepository.findBy(res.getCardId()).getColumnId(), res.getCardId());
		return res;
	}

	@ExpectPermission(value = Permission.MANAGE_ACTION_LIST)
	@RequestMapping(value = "/api/card-data/undo/{eventId}/actionlist", method = RequestMethod.POST)
	@ResponseBody
	public int undoDeleteActionList(@PathVariable("eventId") int eventId, User user) {
		Event event = eventRepository.getEventById(eventId);
		Validate.isTrue(event.getEvent() == EventType.ACTION_LIST_DELETE);

		cardDataService.undoDeleteActionList(event);
		eventEmitter
				.emitUndoDeleteActionList(cardRepository.findBy(event.getCardId()).getColumnId(), event.getCardId());

		return event.getDataId();
	}

	@ExpectPermission(Permission.MANAGE_ACTION_LIST)
	@RequestMapping(value = "/api/card-data/actionlist/{actionListId}/update", method = RequestMethod.POST)
	@ResponseBody
	public int updateActionList(@PathVariable("actionListId") int actionListId, @RequestBody Content data, User user) {
		int res = cardDataService.updateActionList(actionListId, data.content);
		eventEmitter.emitUpdateActionList(cardDataRepository.getUndeletedDataLightById(actionListId).getCardId());
		return res;
	}

	@ExpectPermission(Permission.MANAGE_ACTION_LIST)
	@RequestMapping(value = "/api/card-data/actionlist/{actionListId}/item", method = RequestMethod.POST)
	@ResponseBody
	public CardData createActionItem(@PathVariable("actionListId") int actionListId,
			@RequestBody Content actionItemData, User user) {
		int cardId = cardDataRepository.getUndeletedDataLightById(actionListId).getCardId();
		CardData actionItem = cardDataService.createActionItem(cardId, actionListId, actionItemData.content, user,
				new Date());
		eventEmitter.emitCreateActionItem(cardRepository.findBy(cardId).getColumnId(), cardId);
		return actionItem;
	}

	@ExpectPermission(Permission.MANAGE_ACTION_LIST)
	@RequestMapping(value = "/api/card-data/actionitem/{actionItemId}", method = RequestMethod.DELETE)
	@ResponseBody
	public Event deleteActionItem(@PathVariable("actionItemId") int actionItemId, User user) {
		int cardId = cardDataRepository.getUndeletedDataLightById(actionItemId).getCardId();
		Event res = cardDataService.deleteActionItem(actionItemId, user, new Date());
		eventEmitter.emitDeleteActionItem(cardRepository.findBy(cardId).getColumnId(), cardId);
		return res;
	}

	@ExpectPermission(Permission.MANAGE_ACTION_LIST)
	@RequestMapping(value = "/api/card-data/undo/{eventId}/actionitem", method = RequestMethod.POST)
	@ResponseBody
	public int undoDeleteActionItem(@PathVariable("eventId") int eventId, User user) {
		Event event = eventRepository.getEventById(eventId);
		Validate.isTrue(event.getEvent() == EventType.ACTION_ITEM_DELETE);

		cardDataService.undoDeleteActionItem(event);
		eventEmitter.emiteUndoDeleteActionItem(cardRepository.findBy(event.getCardId()).getColumnId(),
				event.getCardId());

		return event.getDataId();
	}

	@ExpectPermission(Permission.MANAGE_ACTION_LIST)
	@RequestMapping(value = "/api/card-data/actionitem/{actionItemId}/toggle/{status}", method = RequestMethod.POST)
	@ResponseBody
	public int toggleActionItem(@PathVariable("actionItemId") int actionItemId, @PathVariable("status") Boolean status,
			User user) {
		int cardId = cardDataRepository.getUndeletedDataLightById(actionItemId).getCardId();
		int res = cardDataService.toggleActionItem(actionItemId, status, user, new Date());
		eventEmitter.emitToggleActionItem(cardRepository.findBy(cardId).getColumnId(), cardId);
		return res;
	}

	@ExpectPermission(Permission.MANAGE_ACTION_LIST)
	@RequestMapping(value = "/api/card-data/actionitem/{actionItemId}/update", method = RequestMethod.POST)
	@ResponseBody
	public int updateActionItem(@PathVariable("actionItemId") int actionItemId, @RequestBody Content data, User user) {
		int cardId = cardDataRepository.getUndeletedDataLightById(actionItemId).getCardId();
		int res = cardDataService.updateActionItem(actionItemId, data.content);
		eventEmitter.emitUpdateUpdateActionItem(cardId);
		return res;
	}

	@ExpectPermission(Permission.MANAGE_ACTION_LIST)
	@RequestMapping(value = "/api/card-data/actionitem/{actionItemId}/move-to-actionlist/{to}", method = RequestMethod.POST)
	@ResponseBody
	public boolean moveActionItem(@PathVariable("actionItemId") int actionItemId,
			@PathVariable("to") Integer newReferenceId, @RequestBody OrderData dataOrder, User user) {
		int cardId = cardDataRepository.getUndeletedDataLightById(actionItemId).getCardId();

		cardDataService.moveActionItem(cardId, actionItemId, newReferenceId, dataOrder.newContainer, user, new Date());
		eventEmitter.emitMoveActionItem(cardId);
		return true;
	}

	@ExpectPermission(Permission.MANAGE_ACTION_LIST)
	@RequestMapping(value = "/api/card/{cardId}/order/actionlist", method = RequestMethod.POST)
	@ResponseBody
	public boolean reorderActionLists(@PathVariable("cardId") int cardId, @RequestBody List<Number> order) {
		cardDataRepository.updateActionListOrder(cardId, Utils.from(order));
		eventEmitter.emitReorderActionLists(cardId);
		return true;
	}

	@ExpectPermission(Permission.MANAGE_ACTION_LIST)
	@RequestMapping(value = "/api/card-data/actionlist/{actionListId}/order", method = RequestMethod.POST)
	@ResponseBody
	public boolean reorderActionItems(@PathVariable("actionListId") int actionListId, @RequestBody List<Number> order) {
		CardData cd = cardDataRepository.getUndeletedDataLightById(actionListId);
		Validate.isTrue(cd.getType() == CardType.ACTION_LIST);
		int cardId = cd.getCardId();
		cardDataRepository.updateOrderByCardAndReferenceId(cardId, actionListId, Utils.from(order));
		eventEmitter.emitReorderActionItems(cardId);
		return true;
	}

	@ExpectPermission(Permission.CREATE_FILE)
	@RequestMapping(value = "/api/card/{cardId}/file", method = RequestMethod.POST)
	@ResponseBody
	public List<String> uploadFiles(@PathVariable("cardId") int cardId,
			@RequestParam("files") List<MultipartFile> files, User user, HttpServletResponse resp) throws IOException {

		LOG.debug("Files uploaded: {}", files.size());

		if (!ensureFileSize(files)) {
			resp.setStatus(422);
			return Collections.emptyList();
		}

		List<String> digests = new ArrayList<>();
		for (MultipartFile file : files) {
			Path p = Files.createTempFile("lavagna", "upload");
			try (InputStream fileIs = file.getInputStream()) {
				Files.copy(fileIs, p, StandardCopyOption.REPLACE_EXISTING);

				String digest = DigestUtils.sha256Hex(Files.newInputStream(p));
				boolean result = cardDataService.createFile(file.getOriginalFilename(), digest, file.getSize(), cardId,
						Files.newInputStream(p), file.getContentType(), user, new Date()).getLeft();
				if (result) {
					LOG.debug("file uploaded! size: {}, original name: {}, content-type: {}", file.getSize(),
							file.getOriginalFilename(), file.getContentType());
					digests.add(digest);
				}
			} finally {
				Files.delete(p);
				LOG.debug("deleted temp file {}", p);
			}
		}
		eventEmitter.emitUploadFile(cardRepository.findBy(cardId).getColumnId(), cardId);
		return digests;
	}

	private boolean ensureFileSize(List<MultipartFile> files) {
		Integer maxSizeInByte = NumberUtils.createInteger(configurationRepository
				.getValueOrNull(Key.MAX_UPLOAD_FILE_SIZE));
		if (maxSizeInByte == null) {
			return true;
		}
		for (MultipartFile file : files) {
			if (file.getSize() > maxSizeInByte) {
				return false;
			}
		}
		return true;
	}

	// TODO: fix exception handling
	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/card-data/file/{fileId}/{ignore:.+}", method = RequestMethod.GET)
	public void getFile(@PathVariable("fileId") int fileId, HttpServletResponse response) {
		FileDataLight fileData = cardDataRepository.getUndeletedFileByCardDataId(fileId);
		try (OutputStream out = response.getOutputStream()) {
			response.setContentType(fileData.getContentType());
			cardDataRepository.outputFileContent(fileData.getDigest(), out);
		} catch (IOException e) {
			LOG.error("error getting file", e);
			response.setStatus(500);
		}
	}

	@ExpectPermission(Permission.DELETE_FILE)
	@RequestMapping(value = "/api/card-data/file/{fileId}", method = RequestMethod.DELETE)
	@ResponseBody
	public Event deleteFile(@PathVariable("fileId") int fileId, User user) {
		Event result = cardDataService.deleteFile(fileId, user, new Date());
		eventEmitter.emitDeleteFile(cardRepository.findBy(result.getCardId()).getColumnId(), result.getCardId());
		return result;
	}

	@ExpectPermission(Permission.DELETE_FILE)
	@RequestMapping(value = "/api/card-data/undo/{eventId}/file", method = RequestMethod.POST)
	@ResponseBody
	public int undoDeleteFile(@PathVariable("eventId") int eventId, User user) {
		Event event = eventRepository.getEventById(eventId);
		Validate.isTrue(event.getEvent() == EventType.FILE_DELETE);

		cardDataService.undoDeleteFile(event);
		eventEmitter.emiteUndoDeleteFile(cardRepository.findBy(event.getCardId()).getColumnId(), event.getCardId());

		return event.getDataId();
	}

	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/card/{cardId}/files", method = RequestMethod.GET)
	@ResponseBody
	public List<FileDataLight> findAllFiles(@PathVariable("cardId") int cardId) {
		return cardDataRepository.findAllFilesByCardId(cardId);
	}

	// -- activity extension
	@ExpectPermission(Permission.READ)
	@RequestMapping(value = "/api/card-data/activity/{id}", method = RequestMethod.GET)
	@ResponseBody
	public CardData getCardDataById(@PathVariable("id") int id) {
		return cardDataRepository.getDataLightById(id);
	}

	@Getter
	@Setter
	public static class Content {
		private String content;
	}

	@Getter
	@Setter
	public static class OrderData {
		private List<Integer> newContainer;
	}

	@Getter
	@Setter
	public static class FileUpload {
		private MultipartFile file;
	}

	@Getter
	@Setter
	public static class CardDataHistory {
		private final int id;
		private int userId;
		private Date time;
		private final String content;
		private int updatedCount = 0;
		private int updateUser;
		private Date updateDate;
		private final int order;

		public CardDataHistory(int id, String content, int order, int createUser, Date createDate, int updateUser,
				Date updatedDate) {
			this.id = id;
			this.content = content;
			this.order = order;

			this.userId = createUser;
			this.time = createDate;

			this.updateUser = updateUser;
			this.updateDate = updatedDate;
		}
	}

}
