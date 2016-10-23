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

import static io.lavagna.service.SearchFilter.filter;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import io.lavagna.model.BoardColumn;
import io.lavagna.model.CalendarInfo;
import io.lavagna.model.CardDataHistory;
import io.lavagna.model.CardFullWithCounts;
import io.lavagna.model.CardLabel;
import io.lavagna.model.CardLabel.LabelType;
import io.lavagna.model.ColumnDefinition;
import io.lavagna.model.Key;
import io.lavagna.model.LabelAndValue;
import io.lavagna.model.LabelListValueWithMetadata;
import io.lavagna.model.Project;
import io.lavagna.model.SearchResults;
import io.lavagna.model.User;
import io.lavagna.model.UserWithPermission;
import io.lavagna.model.util.CalendarTokenNotFoundException;
import io.lavagna.service.calendarutils.CalendarEventHandler;
import io.lavagna.service.calendarutils.CalendarVEventHandler;
import io.lavagna.service.calendarutils.StandardCalendarEventHandler;
import io.lavagna.service.calendarutils.CalendarEvent;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CalendarService {

    private final ConfigurationRepository configurationRepository;
    private final SearchService searchService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final CardDataService cardDataService;
    private final ProjectService projectService;
    private final CardLabelRepository cardLabelRepository;

    public CalendarService(ConfigurationRepository configurationRepository, SearchService searchService,
        UserService userService, UserRepository userRepository, CardDataService cardDataService,
        ProjectService projectService, CardLabelRepository cardLabelRepository) {
        this.configurationRepository = configurationRepository;
        this.searchService = searchService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.cardDataService = cardDataService;
        this.projectService = projectService;
        this.cardLabelRepository = cardLabelRepository;
    }

    @Transactional(readOnly = false)
    public void setCalendarFeedDisabled(User user, boolean isDisabled) {
        userRepository.setCalendarFeedDisabled(user, isDisabled);
    }

    @Transactional(readOnly = false)
    public CalendarInfo findCalendarInfoFromUser(User user) {
        try {
            return userRepository.findCalendarInfoFromUserId(user);
        } catch (CalendarTokenNotFoundException ex) {
            String token = UUID.randomUUID().toString();// <- this use secure random
            String hashedToken = DigestUtils.sha256Hex(token);
            userRepository.registerCalendarToken(user, hashedToken);
            return findCalendarInfoFromUser(user);
        }
    }

    private UserWithPermission findUserFromCalendarToken(String token) {
        int userId = userRepository.findUserIdFromCalendarToken(token);
        return userService.findUserWithPermission(userId);
    }

    private long getLong(int x, int y) {
        return (((long) x) << 32) | (y & 0xffffffffL);
    }

    private String getEventName(CardFullWithCounts card) {
        return String.format("%s-%s %s (%s)", card.getBoardShortName(), card.getSequence(), card.getName(),
            card.getColumnDefinition());
    }

    private UserDescription getUserDescription(int userId, Map<Integer, UserDescription> cache) {
        if (!cache.containsKey(userId)) {
            User u = userRepository.findById(userId);
            String name = firstNonNull(u.getDisplayName(), u.getEmail(), u.getUsername());
            String email = String.format("MAILTO:%s", firstNonNull(u.getEmail(), "unknown@unknown.com"));
            cache.put(userId, new UserDescription(name, email));
        }
        return cache.get(userId);
    }

    private void addMilestoneEvents(CalendarEventHandler handler, UserWithPermission user)
        throws URISyntaxException, ParseException {

        final String applicationUrl = StringUtils
            .appendIfMissing(configurationRepository.getValue(Key.BASE_APPLICATION_URL), "/");
        final SimpleDateFormat releaseDateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        List<Project> projects = projectService.findAllProjects(user);
        for (Project project : projects) {
            CardLabel milestoneLabel = cardLabelRepository.findLabelByName(project.getId(), "MILESTONE",
                CardLabel.LabelDomain.SYSTEM);

            URI uri = new URI(String.format("%s%s/milestones/", applicationUrl, project.getShortName()));

            for (LabelListValueWithMetadata m : cardLabelRepository.findListValuesByLabelId(milestoneLabel.getId())) {
                if (m.getMetadata().containsKey("releaseDate")) {

                    java.util.Date date = releaseDateFormatter.parse(m.getMetadata().get("releaseDate") + " 12:00");

                    SearchFilter filter = filter(SearchFilter.FilterType.MILESTONE, SearchFilter.ValueType.STRING,
                        m.getValue());
                    SearchFilter notTrashFilter = filter(SearchFilter.FilterType.NOTLOCATION,
                        SearchFilter.ValueType.STRING, BoardColumn.BoardColumnLocation.TRASH.toString());
                    SearchResults cards = searchService.find(Arrays.asList(filter, notTrashFilter), project.getId(),
                        null, user);

                    double closed = 0;
                    double total = 0;
                    StringBuilder descBuilder = new StringBuilder();
                    for (CardFullWithCounts card : cards.getFound()) {
                        if (card.getColumnDefinition() == ColumnDefinition.CLOSED) {
                            closed++;
                        }
                        total++;
                        descBuilder.append(getEventName(card));
                        descBuilder.append("\n");
                    }

                    final String name = String.format("%s - %s (%.0f%%)", project.getShortName(), m.getValue(),
                        total > 0 ? 100 * closed / total : 100);

                    final UUID id = new UUID(getLong(m.getCardLabelId(), m.getId()), getLong(m.getOrder(), 0));

                    handler.addMilestoneEvent(id, name, date, descBuilder.toString(), uri,
                        !m.getMetadata().containsKey("status") || !m.getMetadata().get("status").equals("CLOSED"));
                }
            }
        }

    }

    private void addCardEvents(CalendarEventHandler handler, UserWithPermission user)
        throws URISyntaxException, ParseException {

        Map<Integer, UserDescription> usersCache = new HashMap<>();
        Map<Integer, CardFullWithCounts> map = new LinkedHashMap<>();

        final String applicationUrl = StringUtils
            .appendIfMissing(configurationRepository.getValue(Key.BASE_APPLICATION_URL), "/");

        SearchFilter locationFilter = filter(SearchFilter.FilterType.LOCATION, SearchFilter.ValueType.STRING,
            BoardColumn.BoardColumnLocation.BOARD.toString());

        SearchFilter aFilter = filter(SearchFilter.FilterType.ASSIGNED, SearchFilter.ValueType.CURRENT_USER, "me");
        for (CardFullWithCounts card : searchService.find(Arrays.asList(locationFilter, aFilter), null, null, user)
            .getFound()) {
            map.put(card.getId(), card);
        }

        SearchFilter wFilter = filter(SearchFilter.FilterType.WATCHED_BY, SearchFilter.ValueType.CURRENT_USER, "me");
        for (CardFullWithCounts card : searchService.find(Arrays.asList(locationFilter, wFilter), null, null, user)
            .getFound()) {
            map.put(card.getId(), card);
        }

        for (CardFullWithCounts card : map.values()) {

            URI uri = new URI(String.format("%s%s/%s-%s", applicationUrl, card.getProjectShortName(),
                card.getBoardShortName(), card.getSequence()));

            CardDataHistory cardDesc = cardDataService.findLatestDescriptionByCardId(card.getId());

            for (LabelAndValue lav : card.getLabelsWithType(LabelType.TIMESTAMP)) {
                String name = getEventName(card);

                final UUID id = new UUID(getLong(card.getColumnId(), card.getId()), getLong(lav.getLabelId(),
                    lav.getLabelValueId()));

                // Organizer
                UserDescription ud = getUserDescription(card.getCreationUser(), usersCache);

                handler.addCardEvent(id, name, lav.getLabelValueTimestamp(), cardDesc, uri,
                    card.getColumnDefinition() != ColumnDefinition.CLOSED,
                    card.getCreationDate(), card.getLastUpdateTime(), ud.getName(), ud.getEmail());
            }
        }

    }

    public List<CalendarEvent> getUserCalendar(UserWithPermission user) throws URISyntaxException, ParseException {

        final List<CalendarEvent> events = new ArrayList<>();
        final CalendarEventHandler handler = new StandardCalendarEventHandler(events);

        // Milestones
        addMilestoneEvents(handler, user);

        // Cards
        addCardEvents(handler, user);

        return events;
    }

    public Calendar getCalDavCalendar(String userToken) throws URISyntaxException, ParseException {
        UserWithPermission user;

        try {
            user = findUserFromCalendarToken(userToken);
        } catch (EmptyResultDataAccessException ex) {
            throw new SecurityException("Invalid token");
        }

        if (userRepository.isCalendarFeedDisabled(user)) {
            throw new SecurityException("Calendar feed disabled");
        }

        final Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//Lavagna//iCal4j 1.0//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);
        calendar.getProperties().add(Method.PUBLISH);

        final List<VEvent> events = new ArrayList<>();
        final CalendarEventHandler handler = new CalendarVEventHandler(events);

        // Milestones
        addMilestoneEvents(handler, user);

        // Cards
        addCardEvents(handler, user);

        calendar.getComponents().addAll(events);

        return calendar;
    }

    @Getter
    @AllArgsConstructor
    static class UserDescription {
        private String name;
        private String email;
    }
}
