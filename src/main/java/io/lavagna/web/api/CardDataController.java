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
import io.lavagna.model.Event.EventType;
import io.lavagna.service.*;
import io.lavagna.web.helper.CardCommentOwnershipChecker;
import io.lavagna.web.helper.ExpectPermission;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Controller
public class CardDataController {

    private static final Logger LOG = LogManager.getLogger();

    private final CardDataService cardDataService;
    private final CardDataRepository cardDataRepository;
    private final CardRepository cardRepository;
    private final EventRepository eventRepository;
    private final EventEmitter eventEmitter;
    private final ConfigurationRepository configurationRepository;


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
        return cardDataService.findLatestDescriptionByCardId(cardId);
    }

    @ExpectPermission(Permission.UPDATE_CARD)
    @RequestMapping(value = "/api/card/{cardId}/description", method = RequestMethod.POST)
    @ResponseBody
    public int updateDescription(@PathVariable("cardId") int cardId, @RequestBody Content content, User user) {
        CardDataHistory previousDescription = cardDataService.findLatestDescriptionByCardId(cardId);
        int result = cardDataService.updateDescription(cardId, content.content, new Date(), user.getId());
        CardDataHistory newDescription = cardDataService.findLatestDescriptionByCardId(cardId);
        eventEmitter.emitUpdateDescription(cardRepository.findColumnIdById(cardId), cardId, previousDescription, newDescription, user);
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
                instance.setUserId(comment.getUserId());
                instance.setTime(comment.getTime());
            }
            if (comment.getEventType() == EventType.COMMENT_UPDATE) {
                instance.setUpdatedCount(instance.getUpdatedCount() + 1);
                // never null because the object's fields are always initialized
                if (comment.getTime().getTime() > instance.getUpdateDate().getTime()) {
                    instance.setUpdateUser(comment.getUserId());
                    instance.setUpdateDate(comment.getTime());
                }
            }
            duplicates.put(comment.getId(), instance);
        }
        return new ArrayList<>(duplicates.values());
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
        CardData comment = cardDataService.createComment(cardId, commentData.content, new Date(), user.getId());
        eventEmitter.emitCreateComment(cardRepository.findColumnIdById(cardId), cardId, comment, user);
        return comment;
    }

    @ExpectPermission(value = Permission.UPDATE_CARD_COMMENT, ownershipChecker = CardCommentOwnershipChecker.class)
    @RequestMapping(value = "/api/card-data/comment/{commentId}", method = RequestMethod.POST)
    @ResponseBody
    public void updateComment(@PathVariable("commentId") int commentId, @RequestBody Content content, User user) {
        CardData previousComment = cardDataRepository.getDataLightById(commentId);
        cardDataService.updateComment(commentId, content.content, new Date(), user);
        eventEmitter.emitUpdateComment(cardDataRepository.getUndeletedDataLightById(commentId).getCardId(), previousComment, content.content, user);
    }

    @ExpectPermission(value = Permission.DELETE_CARD_COMMENT, ownershipChecker = CardCommentOwnershipChecker.class)
    @RequestMapping(value = "/api/card-data/comment/{commentId}", method = RequestMethod.DELETE)
    @ResponseBody
    public Event deleteComment(@PathVariable("commentId") int commentId, User user) {
        CardData commentToDelete = cardDataRepository.getDataLightById(commentId);
        Event res = cardDataService.deleteComment(commentId, user, new Date());
        eventEmitter.emitDeleteComment(cardRepository.findBy(res.getCardId()).getColumnId(), res.getCardId(), commentToDelete, user);
        return res;
    }

    @ExpectPermission(value = Permission.DELETE_CARD_COMMENT, ownershipChecker = CardCommentOwnershipChecker.class)
    @RequestMapping(value = "/api/card-data/undo/{eventId}/comment", method = RequestMethod.POST)
    @ResponseBody
    public int undoDeleteComment(@PathVariable("eventId") int eventId, User user) {
        Event event = eventRepository.getEventById(eventId);
        Validate.isTrue(event.getEvent() == EventType.COMMENT_DELETE);

        cardDataService.undoDeleteComment(event);
        CardData undeletedComment = cardDataRepository.getDataLightById(event.getDataId());
        eventEmitter.emitUndoDeleteComment(cardRepository.findColumnIdById(event.getCardId()), event.getCardId(), undeletedComment, user);

        return event.getDataId();
    }

    @ExpectPermission(Permission.MANAGE_ACTION_LIST)
    @RequestMapping(value = "/api/card/{cardId}/actionlist", method = RequestMethod.POST)
    @ResponseBody
    public CardData createActionList(@PathVariable("cardId") int cardId, @RequestBody Content actionListData,
                                     User user) {
        CardData actionList = cardDataService.createActionList(cardId, actionListData.content, user.getId(),
            new Date());
        eventEmitter.emitCreateActionList(cardId, actionListData.content, user);
        return actionList;
    }

    @ExpectPermission(Permission.MANAGE_ACTION_LIST)
    @RequestMapping(value = "/api/card-data/actionlist/{actionListId}", method = RequestMethod.DELETE)
    @ResponseBody
    public Event deleteActionList(@PathVariable("actionListId") int actionListId, User user) {
        Event res = cardDataService.deleteActionList(actionListId, user, new Date());
        CardData actionList = cardDataRepository.getDataLightById(actionListId);
        eventEmitter.emitDeleteActionList(cardRepository.findColumnIdById(res.getCardId()), res.getCardId(), actionList.getContent(), user);
        return res;
    }

    @ExpectPermission(value = Permission.MANAGE_ACTION_LIST)
    @RequestMapping(value = "/api/card-data/undo/{eventId}/actionlist", method = RequestMethod.POST)
    @ResponseBody
    public int undoDeleteActionList(@PathVariable("eventId") int eventId, User user) {
        Event event = eventRepository.getEventById(eventId);
        Validate.isTrue(event.getEvent() == EventType.ACTION_LIST_DELETE);
        cardDataService.undoDeleteActionList(event);
        CardData actionList = cardDataRepository.getDataLightById(event.getDataId());
        eventEmitter.emitUndoDeleteActionList(cardRepository.findColumnIdById(event.getCardId()), event.getCardId(), actionList.getContent(), user);
        return event.getDataId();
    }

    @ExpectPermission(Permission.MANAGE_ACTION_LIST)
    @RequestMapping(value = "/api/card-data/actionlist/{actionListId}/update", method = RequestMethod.POST)
    @ResponseBody
    public int updateActionList(@PathVariable("actionListId") int actionListId, @RequestBody Content data, User user) {
        CardData actionList = cardDataRepository.getDataLightById(actionListId);
        int res = cardDataService.updateActionList(actionListId, data.content);
        eventEmitter.emitUpdateActionList(actionList.getCardId(), actionList.getContent(), data.content, user);
        return res;
    }

    @ExpectPermission(Permission.MANAGE_ACTION_LIST)
    @RequestMapping(value = "/api/card-data/actionlist/{actionListId}/item", method = RequestMethod.POST)
    @ResponseBody
    public CardData createActionItem(@PathVariable("actionListId") int actionListId,
                                     @RequestBody Content actionItemData, User user) {
        CardData actionList = cardDataRepository.getUndeletedDataLightById(actionListId);
        int cardId = actionList.getCardId();
        CardData actionItem = cardDataService.createActionItem(cardId, actionListId, actionItemData.content, user.getId(), new Date());
        eventEmitter.emitCreateActionItem(cardRepository.findColumnIdById(cardId), cardId, actionList.getContent(), actionItemData.content, user);
        return actionItem;
    }

    @ExpectPermission(Permission.MANAGE_ACTION_LIST)
    @RequestMapping(value = "/api/card-data/actionitem/{actionItemId}", method = RequestMethod.DELETE)
    @ResponseBody
    public Event deleteActionItem(@PathVariable("actionItemId") int actionItemId, User user) {
        CardData actionItem = cardDataRepository.getUndeletedDataLightById(actionItemId);
        CardData actionList = cardDataRepository.getDataLightById(actionItem.getReferenceId());
        int cardId = actionItem.getCardId();
        Event res = cardDataService.deleteActionItem(actionItemId, user, new Date());
        eventEmitter.emitDeleteActionItem(cardRepository.findColumnIdById(cardId), cardId, actionList.getContent(), actionItem.getContent(), user);
        return res;
    }

    @ExpectPermission(Permission.MANAGE_ACTION_LIST)
    @RequestMapping(value = "/api/card-data/undo/{eventId}/actionitem", method = RequestMethod.POST)
    @ResponseBody
    public int undoDeleteActionItem(@PathVariable("eventId") int eventId, User user) {
        Event event = eventRepository.getEventById(eventId);
        Validate.isTrue(event.getEvent() == EventType.ACTION_ITEM_DELETE);

        cardDataService.undoDeleteActionItem(event);

        CardData actionItem = cardDataRepository.getUndeletedDataLightById(event.getDataId());
        CardData actionList = cardDataRepository.getDataLightById(actionItem.getReferenceId());

        eventEmitter.emiteUndoDeleteActionItem(cardRepository.findColumnIdById(event.getCardId()), event.getCardId(), actionList.getContent(), actionItem.getContent(), user);

        return event.getDataId();
    }

    @ExpectPermission(Permission.MANAGE_ACTION_LIST)
    @RequestMapping(value = "/api/card-data/actionitem/{actionItemId}/toggle/{status}", method = RequestMethod.POST)
    @ResponseBody
    public int toggleActionItem(@PathVariable("actionItemId") int actionItemId, @PathVariable("status") Boolean status,
                                User user) {
        CardData actionItem = cardDataRepository.getUndeletedDataLightById(actionItemId);
        CardData actionList = cardDataRepository.getDataLightById(actionItem.getReferenceId());
        int cardId = actionItem.getCardId();

        int res = cardDataService.toggleActionItem(actionItemId, status, user.getId(), new Date());

        eventEmitter.emitToggleActionItem(cardRepository.findColumnIdById(cardId), cardId, actionList.getContent(), actionItem.getContent(), status, user);
        return res;
    }

    @ExpectPermission(Permission.MANAGE_ACTION_LIST)
    @RequestMapping(value = "/api/card-data/actionitem/{actionItemId}/update", method = RequestMethod.POST)
    @ResponseBody
    public int updateActionItem(@PathVariable("actionItemId") int actionItemId, @RequestBody Content data, User user) {
        CardData actionItem = cardDataRepository.getUndeletedDataLightById(actionItemId);
        CardData actionList = cardDataRepository.getDataLightById(actionItem.getReferenceId());
        int cardId = actionItem.getCardId();
        int res = cardDataService.updateActionItem(actionItemId, data.content);
        eventEmitter.emitUpdateUpdateActionItem(cardId, actionList.getContent(), actionItem.getContent(), data.content, user);
        return res;
    }

    @ExpectPermission(Permission.MANAGE_ACTION_LIST)
    @RequestMapping(value = "/api/card-data/actionitem/{actionItemId}/move-to-actionlist/{to}", method = RequestMethod.POST)
    @ResponseBody
    public boolean moveActionItem(@PathVariable("actionItemId") int actionItemId,
                                  @PathVariable("to") Integer newReferenceId, @RequestBody OrderData dataOrder, User user) {

        CardData actionItem = cardDataRepository.getUndeletedDataLightById(actionItemId);;
        CardData fromActionList = cardDataRepository.getDataLightById(actionItem.getReferenceId());
        CardData toActionList = cardDataRepository.getDataLightById(newReferenceId);

        int cardId = actionItem.getCardId();

        cardDataService.moveActionItem(cardId, actionItemId, newReferenceId, dataOrder.newContainer, user, new Date());

        eventEmitter.emitMoveActionItem(cardId, fromActionList.getContent(), toActionList.getContent(), actionItem.getContent(), user);
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
    @RequestMapping(value = "/api/card/file", method = RequestMethod.POST)
    @ResponseBody
    public List<String> uploadNewCardFiles(@RequestParam("files") List<MultipartFile> files,
                                               User user,
                                               HttpServletResponse resp) throws IOException {
        LOG.debug("Files uploaded: {}", files.size());

        if (!ensureFileSize(files)) {
            resp.setStatus(422);
            return Collections.emptyList();
        }

        List<String> digests = new ArrayList<>(files.size());
        for (MultipartFile file : files) {
            Path p = Files.createTempFile("lavagna", "upload");
            try (InputStream fileIs = file.getInputStream()) {
                Files.copy(fileIs, p, StandardCopyOption.REPLACE_EXISTING);
                String digest = DigestUtils.sha256Hex(Files.newInputStream(p));
                String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

                cardDataService.createFile(digest, files.size(), fileIs, contentType);

                LOG.debug("file uploaded! size: {}, original name: {}, body-type: {}, by user: {}", file.getSize(),
                    file.getOriginalFilename(), file.getContentType(), user.getId());
                digests.add(digest);
            } finally {
                Files.delete(p);
                LOG.debug("deleted temp file {}", p);
            }
        }

        return digests;
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

        List<String> digests = new ArrayList<>(files.size());
        List<String> fileNames = new ArrayList<>(files.size());
        for (MultipartFile file : files) {
            Path p = Files.createTempFile("lavagna", "upload");
            try (InputStream fileIs = file.getInputStream()) {
                Files.copy(fileIs, p, StandardCopyOption.REPLACE_EXISTING);
                String digest = DigestUtils.sha256Hex(Files.newInputStream(p));
                String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
                boolean result = cardDataService.createFile(file.getOriginalFilename(), digest, file.getSize(), cardId,
                    Files.newInputStream(p), contentType, user, new Date()).getLeft();
                if (result) {
                    LOG.debug("file uploaded! size: {}, original name: {}, body-type: {}", file.getSize(),
                        file.getOriginalFilename(), file.getContentType());
                    digests.add(digest);
                    fileNames.add(file.getOriginalFilename());
                }
            } finally {
                Files.delete(p);
                LOG.debug("deleted temp file {}", p);
            }
        }
        eventEmitter.emitUploadFile(cardRepository.findColumnIdById(cardId), cardId, fileNames, user);
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

    private static Set<String> WHITE_LIST_MIME_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(//
        "image/gif", "image/jpeg", "image/png", "image/webp", "image/bmp",// images
        "video/webm", "video/ogg", "video/mp4",//
        "text/plain"//
    )));

    // TODO: fix exception handling
    @ExpectPermission(Permission.READ)
    @RequestMapping(value = "/api/card-data/file/{fileId}/{ignore:.+}", method = RequestMethod.GET)
    public void getFile(@PathVariable("fileId") int fileId, HttpServletResponse response) {
        FileDataLight fileData = cardDataRepository.getUndeletedFileByCardDataId(fileId);
        try (OutputStream out = response.getOutputStream()) {
            if (WHITE_LIST_MIME_TYPES.contains(fileData.getContentType())) {
                response.setContentType(fileData.getContentType());
            } else {
                response.setHeader("Content-Disposition", "attachment;filename=\"" + fileData.getName() + "\"");
                response.setContentType("application/octet-stream");
            }
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
        FileDataLight file = cardDataRepository.getUndeletedFileByCardDataId(fileId);
        Event result = cardDataService.deleteFile(fileId, user, new Date());
        eventEmitter.emitDeleteFile(cardRepository.findBy(result.getCardId()).getColumnId(), result.getCardId(), file.getName(), user);
        return result;
    }

    @ExpectPermission(Permission.DELETE_FILE)
    @RequestMapping(value = "/api/card-data/undo/{eventId}/file", method = RequestMethod.POST)
    @ResponseBody
    public int undoDeleteFile(@PathVariable("eventId") int eventId, User user) {
        Event event = eventRepository.getEventById(eventId);
        Validate.isTrue(event.getEvent() == EventType.FILE_DELETE);
        cardDataService.undoDeleteFile(event);
        FileDataLight file = cardDataRepository.getUndeletedFileByCardDataId(event.getDataId());
        eventEmitter.emiteUndoDeleteFile(cardRepository.findColumnIdById(event.getCardId()), event.getCardId(), file.getName(), user);
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

    public static class Content {
        private String content;

        public String getContent() {
            return this.content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public static class OrderData {
        private List<Integer> newContainer;

        public List<Integer> getNewContainer() {
            return this.newContainer;
        }

        public void setNewContainer(List<Integer> newContainer) {
            this.newContainer = newContainer;
        }
    }
}
