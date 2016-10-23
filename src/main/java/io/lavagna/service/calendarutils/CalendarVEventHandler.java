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

import io.lavagna.model.CardDataHistory;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.Created;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.LastModified;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Url;

public class CalendarVEventHandler implements CalendarEventHandler {

    private final List<VEvent> events;

    public CalendarVEventHandler(List<VEvent> events) {
        this.events = events;
    }

    private VAlarm createReminder(String name) {
        final VAlarm reminder = new VAlarm(new Dur(0, 0, 0, 0));
        reminder.getProperties().add(Action.DISPLAY);
        reminder.getProperties().add(new Description(name));
        return reminder;
    }

    public void addMilestoneEvent(UUID id, String name, java.util.Date date, String description, URI uri, boolean isActive) {

        final VEvent event = new VEvent(new Date(date.getTime()), name);
        event.getProperties().getProperty(Property.DTSTART).getParameters().add(Value.DATE);

        event.getProperties().add(new Description(description));

        event.getProperties().add(new Uid(id.toString()));

        // Reminder on milestone's date
        if (isActive) {
            event.getAlarms().add(createReminder(name));
        }

        // Url
        event.getProperties().add(new Url(uri));
        events.add(event);
    }

    public void addCardEvent(UUID id, String name, java.util.Date date, CardDataHistory cardDesc, URI uri, boolean isActive,
        java.util.Date creationDate, java.util.Date modifiedDate, String creatorName, String creatorEmail) {

        final VEvent event = new VEvent(new Date(date), name);
        event.getProperties().getProperty(Property.DTSTART).getParameters().add(Value.DATE);

        event.getProperties().add(new Created(new DateTime(creationDate)));
        event.getProperties().add(new LastModified(new DateTime(modifiedDate)));

        event.getProperties().add(new Uid(id.toString()));

        // Reminder on label's date
        if (isActive) {
            event.getAlarms().add(createReminder(name));
        }

        // Organizer
        Organizer organizer = new Organizer(URI.create(creatorEmail));
        organizer.getParameters().add(new Cn(creatorName));
        event.getProperties().add(organizer);

        // Url
        event.getProperties().add(new Url(uri));

        // Description
        if (cardDesc != null) {
            event.getProperties().add(new Description(cardDesc.getContent()));
        }

        events.add(event);
    }
}
