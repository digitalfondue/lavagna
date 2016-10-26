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

import io.lavagna.model.CardFullWithCounts;
import io.lavagna.model.LabelAndValue;
import io.lavagna.model.LabelListValueWithMetadata;
import io.lavagna.model.SearchResults;

import java.util.Date;
import java.util.HashSet;

public class StandardCalendarEventHandler implements CalendarEventHandler {

    private final CalendarEvents events;

    public StandardCalendarEventHandler(CalendarEvents events) {
        this.events = events;
    }

    public void addMilestoneEvent(String shortName, Date date, LabelListValueWithMetadata m, SearchResults cards) {

        if (!events.getMilestones().containsKey(date)) {
            events.getMilestones().put(date, new HashSet<LabelListValueWithMetadata>());
        }
        events.getMilestones().get(date).add(m);
    }

    public void addCardEvent(CardFullWithCounts card, LabelAndValue lav) {

        Date date = lav.getLabelValueTimestamp();
        if (!events.getCards().containsKey(date)) {
            events.getCards().put(date, new HashSet<CardFullWithCounts>());
        }
        events.getCards().get(date).add(card);
    }
}
