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
import io.lavagna.web.api.CardDataController.Content;
import io.lavagna.web.api.CardDataController.OrderData;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

//TODO complete with verify
@RunWith(MockitoJUnitRunner.class)
public class CardDataControllerTest {

	private final int cardId = RandomUtils.nextInt(0, 9001);
	private final int itemId = RandomUtils.nextInt(0, 9001);
	private final int commentId = RandomUtils.nextInt(0, 9001);
	private final int eventId = RandomUtils.nextInt(0, 9001);

	@Mock
	private User user;
	@Mock
	private CardDataService cardDataService;
	@Mock
	private CardDataRepository cardDataRepository;
	@Mock
	private CardRepository cardRepository;
	@Mock
	private EventEmitter eventEmitter;
	@Mock
	private HttpServletResponse response;
	@Mock
	private Event event;
	@Mock
	private FileDataLight fileDataLight;
	@Mock
	private EventRepository eventRepository;
	@Mock
	private ConfigurationRepository configurationRepository;

	@Mock
	private Card card;

	private CardDataController cardDataController;

	@Before
	public void prepare() {
		cardDataController = new CardDataController(cardDataService, cardDataRepository, cardRepository,
				configurationRepository, eventRepository, eventEmitter);
		when(cardRepository.findBy(cardId)).thenReturn(card);
	}

	@Test
	public void getDescription() throws InterruptedException {
		CardDataFull cardDataFull2 = new CardDataFull(1, null, cardId, "description 2", user.getId(), 2, new Date(),
				CardType.DESCRIPTION_HISTORY, 1, EventType.DESCRIPTION_CREATE);

		CardDataFull cardDataFull3 = new CardDataFull(2, null, cardId, "description 1", user.getId(), 0,
				DateUtils.addMilliseconds(new Date(),500), CardType.DESCRIPTION_HISTORY, 1, EventType.DESCRIPTION_UPDATE);

		CardDataFull cardDataFull1 = new CardDataFull(0, null, cardId, "description 3", user.getId(), 1,
				DateUtils.addMilliseconds(new Date(),500), CardType.DESCRIPTION, 1, EventType.DESCRIPTION_UPDATE);

		List<CardDataFull> cardData = new ArrayList<>();
		cardData.add(cardDataFull2);
		cardData.add(cardDataFull3);
		cardData.add(cardDataFull1);

		cardDataController.description(cardId);
	}

	@Test
	public void updateDescrition() {
		Content data = new Content();
		cardDataController.updateDescription(cardId, data, user);
	}

	@Test
	public void createActionItem() {

		when(cardDataRepository.getUndeletedDataLightById(0)).thenReturn(Mockito.mock(CardData.class));
		Content actionItemData = new Content();
		cardDataController.createActionItem(0, actionItemData, user);
	}

	@Test
	public void createActionList() {
		Content actionListData = new Content();
		cardDataController.createActionList(cardId, actionListData, user);
	}

	@Test
	public void createComment() {

		Content commentData = new Content();
		cardDataController.createComment(cardId, commentData, user);
	}

	@Test
	public void deleteActionItemAndUndo() {
		Event event = new Event(eventId, cardId, user.getId(), new Date(), EventType.ACTION_ITEM_DELETE, itemId, null,
				null, null, 0, null, null, null, null, null, null, null);
		when(cardDataRepository.getUndeletedDataLightById(itemId)).thenReturn(Mockito.mock(CardData.class));
		when(cardDataService.deleteActionItem(eq(itemId), eq(user), any(Date.class))).thenReturn(event);
		when(cardDataRepository.getDataLightById(0)).thenReturn(Mockito.mock(CardData.class));
		when(eventRepository.getEventById(eventId)).thenReturn(event);
		cardDataController.deleteActionItem(itemId, user);
		cardDataController.undoDeleteActionItem(eventId, user);
	}

	@Test
	public void deleteActionListAndUndo() {
		Event event = new Event(eventId, cardId, user.getId(), new Date(), EventType.ACTION_LIST_DELETE, itemId, null,
				null, null, 0, null, null, null, null, null, null, null);
		when(cardDataService.deleteActionList(eq(itemId), eq(user), any(Date.class))).thenReturn(event);
		when(eventRepository.getEventById(eventId)).thenReturn(event);
		when(cardDataRepository.getDataLightById(itemId)).thenReturn(Mockito.mock(CardData.class));
		cardDataController.deleteActionList(itemId, user);
		cardDataController.undoDeleteActionList(eventId, user);
	}

	@Test
	public void deleteCommentAndUndo() {
		Event event = new Event(eventId, cardId, user.getId(), new Date(), EventType.COMMENT_DELETE, itemId, null,
				null, null, 0, null, null, null, null, null, null, null);
		when(cardDataService.deleteComment(eq(commentId), eq(user), any(Date.class))).thenReturn(event);
		when(eventRepository.getEventById(eventId)).thenReturn(event);
		when(cardRepository.findBy(event.getCardId())).thenReturn(card);
		cardDataController.deleteComment(commentId, user);
		cardDataController.undoDeleteComment(eventId, user);
	}

	@Test
	public void deleteFileAndUndo() {
		Event event = new Event(eventId, cardId, user.getId(), new Date(), EventType.FILE_DELETE, itemId, null, null,
				null, 0, null, null, null, null, null, null, null);
		when(cardDataService.deleteFile(eq(itemId), eq(user), any(Date.class))).thenReturn(event);
		when(eventRepository.getEventById(eventId)).thenReturn(event);
		when(cardRepository.findBy(event.getCardId())).thenReturn(card);
		when(cardDataRepository.getUndeletedFileByCardDataId(itemId)).thenReturn(Mockito.mock(FileDataLight.class));
		cardDataController.deleteFile(itemId, user);
		cardDataController.undoDeleteFile(eventId, user);
	}

	@Test
	public void findAllActionLists() {
		cardDataController.findAllActionLists(cardId);
	}

	@Test
	public void findAllComments() throws InterruptedException {
		// comment 1
		CardDataFull cardDataFull1 = new CardDataFull(0, null, cardId, "comment 1", user.getId(), 1, new Date(),
				CardType.COMMENT, 5, EventType.COMMENT_CREATE);

		CardDataFull cardDataFull2 = new CardDataFull(0, null, cardId, "comment 1 update 1", user.getId(), 1,
				DateUtils.addMilliseconds(new Date(),500), CardType.COMMENT, 0, EventType.COMMENT_UPDATE);

		// comment 2
		CardDataFull cardDataFull3 = new CardDataFull(1, null, cardId, "comment 2", user.getId(), 2,
				DateUtils.addMilliseconds(new Date(),500), CardType.COMMENT, 3, EventType.COMMENT_CREATE);

		CardDataFull cardDataFull4 = new CardDataFull(1, null, cardId, "comment 2 update 1", user.getId(), 2,
				DateUtils.addMilliseconds(new Date(),500), CardType.COMMENT, 4, EventType.COMMENT_UPDATE);

		CardDataFull cardDataFull5 = new CardDataFull(1, null, cardId, "comment 2 update 2", user.getId(), 2,
				DateUtils.addMilliseconds(new Date(),500), CardType.COMMENT, 1, EventType.COMMENT_UPDATE);

		// comment 3
		CardDataFull cardDataFull6 = new CardDataFull(2, null, cardId, "comment 3", user.getId(), 3,
				DateUtils.addMilliseconds(new Date(),500), CardType.COMMENT_HISTORY, 2, EventType.COMMENT_CREATE);

		List<CardDataFull> cardData = new ArrayList<>();
		cardData.add(cardDataFull1);
		cardData.add(cardDataFull2);
		cardData.add(cardDataFull4);
		cardData.add(cardDataFull3);
		cardData.add(cardDataFull5);
		cardData.add(cardDataFull6);

		when(cardDataService.findAllCommentsByCardId(cardId)).thenReturn(cardData);

		cardDataController.findAllComments(cardId);
	}

	@Test
	public void findAllFiles() {
		cardDataController.findAllFiles(cardId);
	}

	@Test
	public void findAllLightByCardId() {
		cardDataController.findAllLightByCardId(cardId);
	}

	@Test
	public void getFile() throws IOException {
		when(cardDataRepository.getUndeletedFileByCardDataId(itemId)).thenReturn(fileDataLight);
		when(response.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
		cardDataController.getFile(itemId, response);
	}

	@Test
	public void moveActionItem() {
		when(cardDataRepository.getUndeletedDataLightById(0)).thenReturn(Mockito.mock(CardData.class));

		when(cardDataRepository.getDataLightById(1)).thenReturn(Mockito.mock(CardData.class));
		when(cardDataRepository.getDataLightById(0)).thenReturn(Mockito.mock(CardData.class));
		OrderData dataOrder = new OrderData();
		cardDataController.moveActionItem(0, 1, dataOrder, user);
	}

	@Test
	public void reorderActionItems() {
		when(cardDataRepository.getUndeletedDataLightById(0)).thenReturn(
				new CardData(0, 0, 0, CardType.ACTION_LIST, null, 0));
		cardDataController.reorderActionItems(0, Arrays.<Number> asList(0, 1, 2));
	}

	@Test
	public void reorderActionLists() {
		cardDataController.reorderActionLists(cardId, Arrays.<Number> asList(0, 1, 2));
	}

	@Test
	public void toggleActionItem() {
		when(cardDataRepository.getUndeletedDataLightById(itemId)).thenReturn(Mockito.mock(CardData.class));
		when(cardDataRepository.getDataLightById(0)).thenReturn(Mockito.mock(CardData.class));
		cardDataController.toggleActionItem(itemId, true, user);
	}

	@Test
	public void updateActionItem() {
		when(cardDataRepository.getUndeletedDataLightById(itemId)).thenReturn(Mockito.mock(CardData.class));
		when(cardDataRepository.getDataLightById(0)).thenReturn(Mockito.mock(CardData.class));
		Content data = new Content();
		cardDataController.updateActionItem(itemId, data, user);
	}

	@Test
	public void updateActionList() {
		when(cardDataRepository.getDataLightById(itemId)).thenReturn(Mockito.mock(CardData.class));
		Content data = new Content();
		cardDataController.updateActionList(itemId, data, user);
	}

	@Test
	public void updateComment() {
		when(cardDataRepository.getUndeletedDataLightById(commentId)).thenReturn(Mockito.mock(CardData.class));
		Content content = new Content();
		cardDataController.updateComment(commentId, content, user);
	}

	@Test
	public void uploadFiles() throws NoSuchAlgorithmException, IOException {
		MultipartFile f = mock(MultipartFile.class);
		when(f.getOriginalFilename()).thenReturn("fileName");
		when(f.getSize()).thenReturn(7L);
		when(f.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[] { 42, 42, 42, 42, 84, 84, 84 }));
		when(
				cardDataService
						.createFile(any(String.class), any(String.class), any(Long.class), any(Integer.class),
								any(InputStream.class), any(String.class), any(User.class), any(Date.class)))
				.thenReturn(ImmutablePair.of(true, mock(CardData.class)));
		List<MultipartFile> files = Arrays.asList(f);
		cardDataController.uploadFiles(cardId, files, user, new MockHttpServletResponse());
	}
}
