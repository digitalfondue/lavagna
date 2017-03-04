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

import io.lavagna.config.PersistenceAndServiceConfig;
import io.lavagna.model.*;
import io.lavagna.model.CardLabel.LabelDomain;
import io.lavagna.service.calendarutils.CalendarEvents;
import io.lavagna.service.config.TestServiceConfig;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.*;

import static io.lavagna.common.Constants.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestServiceConfig.class, PersistenceAndServiceConfig.class })
@Transactional
public class CalendarServiceTest {

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
    private PermissionService permissionService;
    @Autowired
    private CardService cardService;
    @Autowired
    private CardDataService cardDataService;
    @Autowired
    private CalendarService calendarService;
    @Autowired
    private LabelService labelService;
    @Autowired
    private CardLabelRepository cardLabelRepository;

    private Project project;

    private BoardColumn col;

    private User user;

    @Before
    public void prepare() {

        Helper.createUser(userRepository, "test", "test-user");
        user = userRepository.findUserByName("test", "test-user");

        project = projectService.create("test", "TEST", "desc");
        Board board = boardRepository.createNewBoard("test-board", "TEST-BRD", null, project.getId());

        List<BoardColumnDefinition> definitions = projectService.findColumnDefinitionsByProjectId(project.getId());
        for (BoardColumnDefinition def : definitions) {
            if (def.getValue() == ColumnDefinition.OPEN) {
                col = boardColumnRepository.addColumnToBoard("col1", def.getId(), BoardColumn.BoardColumnLocation.BOARD,
                    board.getId());
            }
        }
        Assert.assertEquals(BoardColumn.BoardColumnLocation.BOARD, col.getLocation());
        Assert.assertEquals(ColumnDefinition.OPEN, col.getStatus());

        Role r = new Role("TEST");
        permissionService.createRole(r);
        permissionService.updatePermissionsToRole(r, EnumSet.of(Permission.READ));
        permissionService.assignRolesToUsers(Collections.singletonMap(r, Collections.singleton(user.getId())));

        configurationRepository.insert(Key.BASE_APPLICATION_URL, "http://localhost");
    }

    @Test
    public void testTokenCreation() {
        CalendarInfo ci = calendarService.findCalendarInfoFromUser(user);
        Assert.assertNotNull(ci.getToken());
        Assert.assertEquals(64, ci.getToken().length());
    }

    @Test
    public void testDoubleFindTokenCreation() {
        CalendarInfo ci = calendarService.findCalendarInfoFromUser(user);
        CalendarInfo secondCi = calendarService.findCalendarInfoFromUser(user);

        Assert.assertNotNull(secondCi.getToken());
        Assert.assertEquals(64, secondCi.getToken().length());
        Assert.assertEquals(secondCi.getToken(), ci.getToken());
    }

    @Test
    public void testDeleteTokenCreation() {
        CalendarInfo ci = calendarService.findCalendarInfoFromUser(user);
        userRepository.deleteCalendarToken(user);
        CalendarInfo newCi = calendarService.findCalendarInfoFromUser(user);

        Assert.assertNotNull(newCi.getToken());
        Assert.assertEquals(64, newCi.getToken().length());
        Assert.assertNotEquals(newCi.getToken(), ci.getToken());
    }

    @Test
    public void testSetCalendarFeedDisabled() {
        Assert.assertFalse(calendarService.findCalendarInfoFromUser(user).getDisabled());

        calendarService.setCalendarFeedDisabled(user, true);

        Assert.assertTrue(calendarService.findCalendarInfoFromUser(user).getDisabled());
    }

    @Test(expected = SecurityException.class)
    public void testGetUserCalendarWithWrongToken() throws URISyntaxException, ParseException {
        calendarService.getCalDavCalendar("abcd");
    }

    @Test
    public void testGetUserCalendarOnEmpty() throws URISyntaxException, ParseException {
        CalendarInfo ci = calendarService.findCalendarInfoFromUser(user);

        Calendar calendar = calendarService.getCalDavCalendar(ci.getToken());

        Assert.assertNotNull(calendar);
        Assert.assertEquals(0, calendar.getComponents().size());
    }

    @Test(expected = SecurityException.class)
    public void testGetUserCalendarOnDisabled() throws URISyntaxException, ParseException {

        CalendarInfo ci = calendarService.findCalendarInfoFromUser(user);
        calendarService.setCalendarFeedDisabled(user, true);

        calendarService.getCalDavCalendar(ci.getToken());
    }

    @Test
    public void testGetProjectStandardCalendar() throws URISyntaxException, ParseException {

        Card assignedCard = cardService.createCard("card1", col.getId(), new Date(), user);
        cardDataService.updateDescription(assignedCard.getId(), "Desc", new Date(), user.getId());

        Card watchedCard = cardService.createCard("card2", col.getId(), new Date(), user);

        Date now = new Date();

        CardLabel dueDate = cardLabelRepository.findLabelByName(project.getId(), SYSTEM_LABEL_DUE_DATE, LabelDomain.SYSTEM);
        labelService.addLabelValueToCard(dueDate, assignedCard.getId(), new CardLabelValue.LabelValue(now), user, now);
        labelService.addLabelValueToCard(dueDate, watchedCard.getId(), new CardLabelValue.LabelValue(now), user, now);

        UserWithPermission uwpGlobalRead = new UserWithPermission(user, EnumSet.of(Permission.READ),
            Collections.<String, Set<Permission>>emptyMap(), Collections.<Integer, Set<Permission>>emptyMap());

        CalendarEvents events = calendarService.getProjectCalendar(project.getShortName(), uwpGlobalRead);

        Assert.assertNotNull(events);
        Assert.assertEquals(2, events.getDailyEvents().values().iterator().next().getCards().size());

    }

    @Test
    public void testGetStandardCalendar() throws URISyntaxException, ParseException {

        Card assignedCard = cardService.createCard("card1", col.getId(), new Date(), user);
        cardDataService.updateDescription(assignedCard.getId(), "Desc", new Date(), user.getId());

        Card watchedCard = cardService.createCard("card2", col.getId(), new Date(), user);

        Date now = new Date();

        CardLabel assigned = cardLabelRepository.findLabelByName(project.getId(), SYSTEM_LABEL_ASSIGNED, LabelDomain.SYSTEM);
        labelService.addLabelValueToCard(assigned, assignedCard.getId(), new CardLabelValue.LabelValue(user.getId()),
            user, now);

        CardLabel watched = cardLabelRepository.findLabelByName(project.getId(), SYSTEM_LABEL_WATCHED_BY, LabelDomain.SYSTEM);
        labelService.addLabelValueToCard(watched, watchedCard.getId(), new CardLabelValue.LabelValue(user.getId()),
            user, now);

        CardLabel dueDate = cardLabelRepository.findLabelByName(project.getId(), SYSTEM_LABEL_DUE_DATE, LabelDomain.SYSTEM);
        labelService.addLabelValueToCard(dueDate, assignedCard.getId(), new CardLabelValue.LabelValue(now), user, now);
        labelService.addLabelValueToCard(dueDate, watchedCard.getId(), new CardLabelValue.LabelValue(now), user, now);

        UserWithPermission uwpGlobalRead = new UserWithPermission(user, EnumSet.of(Permission.READ),
            Collections.<String, Set<Permission>>emptyMap(), Collections.<Integer, Set<Permission>>emptyMap());

        CalendarEvents events = calendarService.getUserCalendar(uwpGlobalRead);

        Assert.assertNotNull(events);
        Assert.assertEquals(2, events.getDailyEvents().values().iterator().next().getCards().size());

    }

    @Test
    public void testGetCalDavCalendar() throws URISyntaxException, ParseException {

        Card assignedCard = cardService.createCard("card1", col.getId(), new Date(), user);
        cardDataService.updateDescription(assignedCard.getId(), "Desc", new Date(), user.getId());

        Card watchedCard = cardService.createCard("card2", col.getId(), new Date(), user);

        Date now = new Date();

        CardLabel assigned = cardLabelRepository.findLabelByName(project.getId(), SYSTEM_LABEL_ASSIGNED, LabelDomain.SYSTEM);
        labelService.addLabelValueToCard(assigned, assignedCard.getId(), new CardLabelValue.LabelValue(user.getId()),
            user, now);

        CardLabel watched = cardLabelRepository.findLabelByName(project.getId(), SYSTEM_LABEL_WATCHED_BY, LabelDomain.SYSTEM);
        labelService.addLabelValueToCard(watched, watchedCard.getId(), new CardLabelValue.LabelValue(user.getId()),
            user, now);

        CalendarInfo ci = calendarService.findCalendarInfoFromUser(user);

        CardLabel dueDate = cardLabelRepository.findLabelByName(project.getId(), SYSTEM_LABEL_DUE_DATE, LabelDomain.SYSTEM);
        labelService.addLabelValueToCard(dueDate, assignedCard.getId(), new CardLabelValue.LabelValue(now), user, now);
        labelService.addLabelValueToCard(dueDate, watchedCard.getId(), new CardLabelValue.LabelValue(now), user, now);

        Calendar calendar = calendarService.getCalDavCalendar(ci.getToken());

        Assert.assertNotNull(calendar);
        Assert.assertEquals(2, calendar.getComponents().size());

        VEvent event1 = (VEvent) calendar.getComponents().get(0);
        Assert.assertEquals("TEST-BRD-1 card1 (OPEN)", event1.getSummary().getValue());
        Assert.assertEquals("http://localhost/TEST/TEST-BRD-1", event1.getUrl().getUri().toASCIIString());
    }
}
