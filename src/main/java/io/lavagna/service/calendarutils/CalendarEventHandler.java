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
import java.util.UUID;

public interface CalendarEventHandler {

    void addMilestoneEvent(UUID id, String name, java.util.Date date, String description, URI uri, boolean isActive);

    void addCardEvent(UUID id, String name, java.util.Date date, CardDataHistory cardDesc, URI uri, boolean isActive,
        java.util.Date creationDate, java.util.Date modifiedDate, String creatorName, String creatorEmail);

}
