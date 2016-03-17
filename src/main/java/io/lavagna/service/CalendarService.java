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
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.TzId;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Created;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.LastModified;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Url;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.util.TimeZones;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
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
            String email = String.format("mail:%s", firstNonNull(u.getEmail(), "no-e-mail"));
            cache.put(userId, new UserDescription(name, email));
        }
        return cache.get(userId);
    }

    public Calendar getUserCalendar(String userToken) throws URISyntaxException, ParseException {
        UserWithPermission user;

        try {
            user = findUserFromCalendarToken(userToken);
        } catch (EmptyResultDataAccessException ex) {
            throw new SecurityException("Invalid token");
        }

        if (userRepository.isCalendarFeedDisabled(user)) {
            throw new SecurityException("Calendar feed disabled");
        }

        final String utcTimeZone = TimeZones.getUtcTimeZone().getID();
        final TzId tzParam = new TzId(utcTimeZone);


        final Calendar calendar = new Calendar();
        calendar.getProperties().add(new ProdId("-//Lavagna//iCal4j 1.0//EN"));
        calendar.getProperties().add(Version.VERSION_2_0);
        calendar.getProperties().add(CalScale.GREGORIAN);
        calendar.getProperties().add(Method.PUBLISH);

        final String applicationUrl = StringUtils.appendIfMissing(
            configurationRepository.getValue(Key.BASE_APPLICATION_URL), "/");

        final List<VEvent> events = new ArrayList<>();

        final SimpleDateFormat releaseDateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        // Milestones
        List<Project> projects = projectService.findAllProjects(user);
        for (Project project : projects) {
            CardLabel milestoneLabel = cardLabelRepository.findLabelByName(project.getId(), "MILESTONE",
                CardLabel.LabelDomain.SYSTEM);

            Url mUrl = new Url(new URI(String.format("%s%s/milestones/", applicationUrl, project.getShortName())));

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

                    final VEvent event = new VEvent(new Date(date.getTime()), name);

                    event.getProperties().add(new Description(descBuilder.toString()));

                    final UUID id = new UUID(getLong(m.getCardLabelId(), m.getId()), getLong(m.getOrder(), 0));
                    event.getProperties().add(new Uid(id.toString()));

                    // Reminder on milestone's date
                    if (!m.getMetadata().containsKey("status") || m.getMetadata().get("status").equals("CLOSED")) {
                        final VAlarm reminder = new VAlarm(new Dur(0, 0, 0, 0));
                        reminder.getProperties().add(Action.DISPLAY);
                        reminder.getProperties().add(new Description(name));
                        event.getAlarms().add(reminder);
                    }

                    event.getProperties().getProperty(Property.DTSTART).getParameters().add(tzParam);

                    // Url
                    event.getProperties().add(mUrl);

                    events.add(event);
                }
            }
        }

        // Cards
        Map<Integer, UserDescription> usersCache = new HashMap<>();
        Map<Integer, CardFullWithCounts> map = new LinkedHashMap<>();

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

            Url cardUrl = new Url(new URI(String.format("%s%s/%s-%s", applicationUrl, card.getProjectShortName(),
                card.getBoardShortName(), card.getSequence())));

            CardDataHistory cardDesc = cardDataService.findLatestDescriptionByCardId(card.getId());

            for (LabelAndValue lav : card.getLabelsWithType(LabelType.TIMESTAMP)) {
                String name = getEventName(card);

                final VEvent event = new VEvent(new Date(lav.getLabelValueTimestamp()), name);
                event.getProperties().add(new Created(new DateTime(card.getCreationDate())));
                event.getProperties().add(new LastModified(new DateTime(card.getLastUpdateTime())));

                final UUID id = new UUID(getLong(card.getColumnId(), card.getId()),
                    getLong(lav.getLabelId(), lav.getLabelValueId()));
                event.getProperties().add(new Uid(id.toString()));

                // Reminder on label's date
                if (card.getColumnDefinition() != ColumnDefinition.CLOSED) {
                    final VAlarm reminder = new VAlarm(new Dur(0, 0, 0, 0));
                    reminder.getProperties().add(Action.DISPLAY);
                    reminder.getProperties().add(new Description(name));
                    event.getAlarms().add(reminder);
                }

                event.getProperties().getProperty(Property.DTSTART).getParameters().add(tzParam);

                // Organizer
                UserDescription ud = getUserDescription(card.getCreationUser(), usersCache);
                Organizer organizer = new Organizer(URI.create(ud.getEmail()));
                organizer.getParameters().add(new Cn(ud.getName()));
                event.getProperties().add(organizer);

                // Url
                event.getProperties().add(cardUrl);

                // Description
                if (cardDesc != null) {
                    event.getProperties().add(new Description(cardDesc.getContent()));
                }

                events.add(event);
            }
        }

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
