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

import java.util.Date;
import java.util.HashSet;

public class StandardCalendarEventHandler implements CalendarEventHandler {

    private final CalendarEvents events;

    public StandardCalendarEventHandler(CalendarEvents events) {
        this.events = events;
    }

    private CalendarEvents.MilestoneDayEvents getDayEventsFromDate(Date date) {
        if (!events.getDailyEvents().containsKey(date)) {
            events.getDailyEvents().put(date, new CalendarEvents.MilestoneDayEvents(
                new HashSet<CalendarEvents.MilestoneEvent>(),
                new HashSet<CardFullWithCounts>()));
        }
        return events.getDailyEvents().get(date);
    }

    public void addMilestoneEvent(String projectShortName, Date date, LabelListValueWithMetadata m,
        SearchResults cards) {

        double closed = 0;
        double total = 0;
        for (CardFullWithCounts card : cards.getFound()) {
            if (card.getColumnDefinition() == ColumnDefinition.CLOSED) {
                closed++;
            }
            total++;
        }

        final String name = String.format("%s (%.0f%%)", m.getValue(), total > 0 ? 100 * closed / total : 100);

        getDayEventsFromDate(date).getMilestones().add(new CalendarEvents.MilestoneEvent(projectShortName, name, m));

    }

    public void addCardEvent(CardFullWithCounts card, LabelAndValue lav) {

        Date date = lav.getLabelValueTimestamp();
        getDayEventsFromDate(date).getCards().add(card);

    }
}
