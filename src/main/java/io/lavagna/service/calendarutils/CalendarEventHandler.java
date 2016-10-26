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

import java.net.URISyntaxException;
import java.util.Date;

public interface CalendarEventHandler {


    void addCardEvent(CardFullWithCounts card, LabelAndValue lav) throws URISyntaxException;

    void addMilestoneEvent(String shortName, Date date, LabelListValueWithMetadata m, SearchResults cards)
        throws URISyntaxException;
}
