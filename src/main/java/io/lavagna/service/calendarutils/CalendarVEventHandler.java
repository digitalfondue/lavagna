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

package io.lavagna.service.calendarutils;

import io.lavagna.model.*;
import io.lavagna.service.CardDataService;
import io.lavagna.service.UserRepository;
import net.fortuna.ical4j.model.*;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

public class CalendarVEventHandler implements CalendarEventHandler {

    private final Map<Integer, UserDescription> usersCache = new HashMap<>();
    private final String applicationUrl;
    private final CardDataService cardDataService;
    private final UserRepository userRepository;
    private final List<VEvent> events;

    public CalendarVEventHandler(String applicationUrl, CardDataService cardDataService, UserRepository userRepository,
        List<VEvent> events) {
        this.applicationUrl = applicationUrl;
        this.cardDataService = cardDataService;
        this.userRepository = userRepository;
        this.events = events;
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

    private VAlarm createReminder(String name) {
        final VAlarm reminder = new VAlarm(new Dur(0, 0, 0, 0));
        reminder.getProperties().add(Action.DISPLAY);
        reminder.getProperties().add(new Description(name));
        return reminder;
    }

    public void addMilestoneEvent(String projectShortName, java.util.Date date, LabelListValueWithMetadata m,
        SearchResults cards) throws URISyntaxException {

        URI uri = new URI(String.format("%s%s/milestones/", applicationUrl, projectShortName));

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

        final String name = String.format("%s - %s (%.0f%%)", projectShortName, m.getValue(),
            total > 0 ? 100 * closed / total : 100);

        final UUID id = new UUID(getLong(m.getCardLabelId(), m.getId()), getLong(m.getOrder(), 0));

        DateTime dueDate = new DateTime(date.getTime());
        dueDate.setUtc(true);
        final VEvent event = new VEvent(dueDate, name);
        event.getProperties().<Property>getProperty(Property.DTSTART).getParameters().add(Value.DATE_TIME);

        event.getProperties().add(new Description(descBuilder.toString()));

        event.getProperties().add(new Uid(id.toString()));

        // Reminder on milestone's date
        if (!m.getMetadata().containsKey("status") || !m.getMetadata().get("status").equals("CLOSED")) {
            event.getAlarms().add(createReminder(name));
        }

        // Url
        event.getProperties().add(new Url(uri));
        events.add(event);
    }

    public void addCardEvent(CardFullWithCounts card, LabelAndValue lav) throws URISyntaxException {

        URI uri = new URI(String.format("%s%s/%s-%s", applicationUrl, card.getProjectShortName(),
            card.getBoardShortName(), card.getSequence()));

        CardDataHistory cardDesc = cardDataService.findLatestDescriptionByCardId(card.getId());

        String name = getEventName(card);

        final UUID id = new UUID(getLong(card.getColumnId(), card.getId()), getLong(lav.getLabelId(),
            lav.getLabelValueId()));

        // Organizer
        UserDescription ud = getUserDescription(card.getCreationUser(), usersCache);

        DateTime dueDate = new DateTime(lav.getLabelValueTimestamp());
        dueDate.setUtc(true);
        final VEvent event = new VEvent(dueDate, name);
        event.getProperties().<Property>getProperty(Property.DTSTART).getParameters().add(Value.DATE_TIME);

        event.getProperties().add(new Created(new DateTime(card.getCreationDate())));
        event.getProperties().add(new LastModified(new DateTime(card.getLastUpdateTime())));

        event.getProperties().add(new Uid(id.toString()));

        // Reminder on label's date
        if (card.getColumnDefinition() != ColumnDefinition.CLOSED) {
            event.getAlarms().add(createReminder(name));
        }

        // Organizer
        Organizer organizer = new Organizer(URI.create(ud.getEmail()));
        organizer.getParameters().add(new Cn(ud.getName()));
        event.getProperties().add(organizer);

        // Url
        event.getProperties().add(new Url(uri));

        // Description
        if (cardDesc != null) {
            event.getProperties().add(new Description(cardDesc.getContent()));
        }

        events.add(event);
    }

    private class UserDescription {
        private String name;
        private String email;

        @java.beans.ConstructorProperties({ "name", "email" }) public UserDescription(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() {
            return this.name;
        }

        public String getEmail() {
            return this.email;
        }
    }
}
