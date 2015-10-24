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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.lavagna.config.PersistenceAndServiceConfig;
import io.lavagna.model.Board;
import io.lavagna.model.BoardColumn;
import io.lavagna.model.BoardColumn.BoardColumnLocation;
import io.lavagna.model.BoardColumnDefinition;
import io.lavagna.model.Card;
import io.lavagna.model.CardFull;
import io.lavagna.model.CardLabel;
import io.lavagna.model.Key;
import io.lavagna.model.CardLabel.LabelDomain;
import io.lavagna.model.CardLabelValue;
import io.lavagna.model.MailConfig;
import io.lavagna.model.Project;
import io.lavagna.model.User;
import io.lavagna.service.config.TestServiceConfig;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestServiceConfig.class, PersistenceAndServiceConfig.class })
@Transactional
public class NotificationServiceTest {

	@Autowired
	private ConfigurationRepository configurationRepository;

	@Autowired
	private ProjectService projectService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BoardRepository boardRepository;

	@Autowired
	private BoardColumnRepository boardColumnRepository;

	@Autowired
	private CardRepository cardRepository;

	@Autowired
	private CardService cardService;

	@Autowired
	private CardDataService cardDataService;

	@Autowired
	private CardDataRepository cardDataRepo;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private LabelService labelService;

	@Autowired
	private CardLabelRepository cardLabelRepository;

	private Board board;
	private BoardColumn col1;
	private Card card1;
	private Card card2;
    private User user;
    private User otherUser;
	private CardLabel assignedLabel;
	private CardLabel watchedLabel;

	@Before
	public void prepare() {

		configurationRepository.insert(Key.BASE_APPLICATION_URL, "https://base.application.lavagna.io/");

		userRepository.createUser("test", "test-user", "test@test.test", "display name", true);
		user = userRepository.findUserByName("test", "test-user");

        userRepository.createUser("test", "other-user", "other@test.test", "display name", true);
        otherUser = userRepository.findUserByName("test", "other-user");

		Project project = projectService.create("test", "TEST", "desc");
		board = boardRepository.createNewBoard("test-board", "TEST-BRD", null, project.getId());

		List<BoardColumnDefinition> definitions = projectService.findColumnDefinitionsByProjectId(project.getId());
		boardColumnRepository.addColumnToBoard("col1", definitions.get(0).getId(), BoardColumnLocation.BOARD,
				board.getId());
		List<BoardColumn> cols = boardColumnRepository.findAllColumnsFor(board.getId(), BoardColumnLocation.BOARD);
		col1 = cols.get(0);
		cardService.createCard("card1", col1.getId(), new Date(), user);
		cardService.createCard("card2", col1.getId(), new Date(), user);
		List<CardFull> cards = cardRepository.findAllByColumnId(col1.getId());
		card1 = cards.get(0);
		card2 = cards.get(1);

		assignedLabel = cardLabelRepository.findLabelByName(project.getId(), "ASSIGNED", LabelDomain.SYSTEM);
		watchedLabel = cardLabelRepository.findLabelByName(project.getId(), "WATCHED_BY", LabelDomain.SYSTEM);
	}

	@Test
	public void testCheck() {
	    Set<Integer> noUsersToNotify = notificationService.check(DateUtils.addDays(new Date(), -1));
	    Assert.assertTrue(noUsersToNotify.isEmpty());

	    // assign card
	    labelService.addLabelValueToCard(assignedLabel.getId(), card1.getId(), new CardLabelValue.LabelValue(null,
                null, null, null, user.getId(), null), user, new Date());

	    cardDataService.createComment(card1.getId(), "first comment", new Date(), user);

	    notificationService.check(DateUtils.addDays(new Date(), 1));
	    Assert.assertTrue(notificationService.check(DateUtils.addDays(new Date(), 2)).contains(user.getId()));
	}

	@Test
	public void sendEmailTest() {

		labelService.addLabelValueToCard(assignedLabel.getId(), card1.getId(), new CardLabelValue.LabelValue(null,
				null, null, null, user.getId(), null), user, new Date());
		labelService.addLabelValueToCard(watchedLabel.getId(), card2.getId(), new CardLabelValue.LabelValue(null, null,
				null, null, user.getId(), null), user, new Date());

		cardDataService.createComment(card1.getId(), "first comment", new Date(), user);

		cardDataService.createComment(card2.getId(), "first comment on card 2", new Date(), user);

		MailConfig mc = mock(MailConfig.class);
		when(mc.isMinimalConfigurationPresent()).thenReturn(true);
		when(mc.getFrom()).thenReturn("from@lavagna.io");
		notificationService.notifyUser(user.getId(), new Date(), true, mc);

		verify(mc).send(
				eq("test@test.test"),
				eq("Lavagna: TEST-BRD-1, TEST-BRD-2"),
				any(String.class), any(String.class));
	}

    @Test
    public void sendEmailTestWithoutMyEvents() {

        userRepository.updateProfile(user, user.getEmail(), user.getDisplayName(), true, true);

        labelService.addLabelValueToCard(assignedLabel.getId(), card1.getId(), new CardLabelValue.LabelValue(null,
            null, null, null, user.getId(), null), user, new Date());
        labelService.addLabelValueToCard(watchedLabel.getId(), card2.getId(), new CardLabelValue.LabelValue(null, null,
            null, null, user.getId(), null), user, new Date());

        cardDataService.createComment(card1.getId(), "first comment", new Date(), user);

        cardDataService.createComment(card2.getId(), "first comment on card 2", new Date(), otherUser);

        MailConfig mc = mock(MailConfig.class);
        when(mc.isMinimalConfigurationPresent()).thenReturn(true);
        when(mc.getFrom()).thenReturn("from@lavagna.io");
        notificationService.notifyUser(user.getId(), new Date(), true, mc);

        verify(mc).send(
            eq("test@test.test"),
            eq("Lavagna: TEST-BRD-2"),
            any(String.class), any(String.class));
    }
}
