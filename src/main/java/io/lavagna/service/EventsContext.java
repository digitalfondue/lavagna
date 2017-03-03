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

import io.lavagna.model.BoardColumn;
import io.lavagna.model.CardFull;
import io.lavagna.model.CardLabel.LabelType;
import io.lavagna.model.Event;
import io.lavagna.model.User;

import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.String.format;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

/**
 * Contains all the necessary data for formatting an email to the user.
 */
class EventsContext {
	// aggregate the events by card id
	final Map<Integer, List<Event>> events = new TreeMap<>();
	final Map<Integer, User> users = new HashMap<>();
	final Map<Integer, CardFull> cards = new HashMap<>();
	final Map<Integer, String> cardData;
	final Map<Integer, BoardColumn> columns = new HashMap<>();

	EventsContext(List<Event> events, List<User> users, List<CardFull> cards, Map<Integer, String> cardData,
			List<BoardColumn> columns) {
		this.cardData = cardData;

		for (Event e : events) {
			if (!this.events.containsKey(e.getCardId())) {
				this.events.put(e.getCardId(), new ArrayList<Event>());
			}
			this.events.get(e.getCardId()).add(e);
		}
		for (User u : users) {
			this.users.put(u.getId(), u);
		}
		for (CardFull c : cards) {
			this.cards.put(c.getId(), c);
		}
		for (BoardColumn bc : columns) {
			this.columns.put(bc.getId(), bc);
		}
	}

	String formatLabel(Event e) {
		return new StringBuilder(e.getLabelName()).append(e.getLabelType() != LabelType.NULL ? "::" : "")
				.append(formatLabelValue(e)).toString();
	}

	String formatLabelValue(Event e) {
		if (e.getLabelType() == LabelType.STRING) {
			return e.getValueString();
		} else if (e.getLabelType() == LabelType.INT) {
			return Integer.toString(e.getValueInt());
		} else if (e.getLabelType() == LabelType.TIMESTAMP) {
			return new SimpleDateFormat("dd.MM.yyyy").format(e.getValueTimestamp());
		} else if (e.getLabelType() == LabelType.CARD) {
			CardFull cf = cards.get(e.getValueCard());
			return cf.getBoardShortName() + "-" + cf.getSequence();
		} else if (e.getLabelType() == LabelType.USER) {
			return formatUser(e.getValueUser());
		} else {
			return "";
		}
	}

	String formatColumn(Integer colId) {
		if (colId != null && columns.containsKey(colId)) {
			BoardColumn col = columns.get(colId);
			return format("%s (%s::%s)", col.getName(), col.getLocation(), col.getStatus());
		} else {
			return "-";
		}
	}

	String formatUser(int userId) {
		User u = users.get(userId);
		String name = firstNonNull(u.getDisplayName(), u.getEmail(), u.getProvider() + ":" + u.getUsername());
		return name + (u.getEmail() != null && !name.equals(u.getEmail()) ? " <" + u.getEmail() + ">" : "");
	}
}
