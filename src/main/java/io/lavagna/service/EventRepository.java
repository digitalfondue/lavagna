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

import io.lavagna.model.CardLabel.LabelType;
import io.lavagna.model.CardLabelValue.LabelValue;
import io.lavagna.model.Event;
import io.lavagna.model.Event.EventType;
import io.lavagna.model.EventsCount;
import io.lavagna.query.EventQuery;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Repository
@Transactional(readOnly = true)
public class EventRepository {

	private final NamedParameterJdbcTemplate jdbc;
	private final EventQuery queries;

	private static final int FEED_SIZE = 20;

	public EventRepository(NamedParameterJdbcTemplate jdbc, EventQuery queries) {
		this.jdbc = jdbc;
		this.queries = queries;
	}

	public int count() {
		return queries.count();
	}

	public List<Event> find(int offset, int amount) {
		return queries.find(offset, amount);
	}

	public List<Event> findNextEventFor(Event e) {
		return queries.findNextEventFor(e.getDataId(), e.getId(), e.getEvent().toString());
	}

	public Event getEventById(int eventId) {
		return queries.getById(eventId);
	}

	@Transactional(readOnly = false)
	public Event insertLabelEvent(String labelName, int cardId, int userId, EventType event, LabelValue value,
			LabelType labelType, Date time) {

		queries.insertLabelEvent(labelName, labelType.toString(), cardId, userId, time, event.toString(),
				value.getValueInt(), value.getValueString(), value.getValueTimestamp(), value.getValueCard(),
				value.getValueUser());

		return queries.findLastCreated();
	}

	@Transactional(readOnly = false)
	public void insertCardEvents(List<Integer> cardIds, Integer previousColumnId, int columnId, int userId,
			EventType event, Date time, String name) {
		List<SqlParameterSource> param = new ArrayList<>(cardIds.size());
		for (Integer cardId : cardIds) {
			param.add(prepareForCardEvent(cardId, previousColumnId, columnId, userId, event, time, name));
		}
		jdbc.batchUpdate(queries.insertCardEvent(), param.toArray(new SqlParameterSource[param.size()]));
	}

	@Transactional(readOnly = false)
	public Event insertCardEvent(int cardId, Integer previousColumnId, int columnId, int userId, EventType event,
			Date time, String name) {
		insertCardEvents(Collections.singletonList(cardId), previousColumnId, columnId, userId, event, time, name);
		return queries.findLastCreated();
	}

	private static SqlParameterSource prepareForCardEvent(int cardId, Integer previousColumnId, int columnId,
			int userId, EventType event, Date time, String name) {
		return new MapSqlParameterSource("cardId", cardId).addValue("previousColumnId", previousColumnId)
				.addValue("columnId", columnId).addValue("time", time).addValue("userId", userId)
				.addValue("event", event.toString()).addValue("valueString", name);
	}

	@Transactional(readOnly = false)
	public Event insertCardEvent(int cardId, int columnId, int userId, EventType event, Date time, String name) {
		return insertCardEvent(cardId, null, columnId, userId, event, time, name);
	}

	@Transactional(readOnly = false)
	public void insertCardEvent(List<Integer> cardIds, int columnId, int userId, EventType eventType, Date date) {
		List<SqlParameterSource> params = new ArrayList<>(cardIds.size());
		for (Integer cardId : cardIds) {
			params.add(prepareForCardEvent(cardId, null, columnId, userId, eventType, date, null));
		}

		jdbc.batchUpdate(queries.insertCardEvent(), params.toArray(new SqlParameterSource[] { }));
	}

	@Transactional(readOnly = false)
	public Event insertCardDataEvent(int cardDataId, int cardId, EventType event, int userId, Integer referenceId,
			Date time) {
		return insertCardDataEvent(cardDataId, cardId, event, userId, referenceId, null, time);
	}

	@Transactional(readOnly = false)
	public Event insertCardDataEvent(int cardDataId, int cardId, EventType event, int userId, Integer referenceId,
			Integer newReferenceId, Date time) {

		queries.insertCardDataEvent(cardDataId, cardId, userId, time, event.toString(), referenceId, newReferenceId);
		return queries.findLastCreated();
	}

	@Transactional(readOnly = false)
	public Event insertFileEvent(int cardDataId, int cardId, EventType event, int userId, Integer referenceId,
			String name, Date time) {

		queries.insertFileEvent(cardDataId, cardId, userId, time, event.toString(), referenceId, name);
		return queries.findLastCreated();
	}

	public Set<Integer> findUsersIdFor(int cardDataId, EventType event) {
		return new HashSet<>(queries.findUsersIdForCardData(cardDataId, event.toString()));
	}

	@Transactional(readOnly = false)
	public void remove(int id, int cardId, EventType event) {
		queries.remove(id, cardId, event.toString());
	}

	// profile

    public List<Event> getLatestActivity(int userId, Date fromDate) {
        return queries.getLatestActivity(userId, fromDate);
    }

    public List<Event> getLatestActivityByProjects(int userId, Date fromDate, Collection<Integer> projects) {
        return queries.getLatestActivityByProjects(userId, projects, fromDate);
    }

	public List<Event> getLatestActivityByPage(int userId, int page) {
		return queries.getLatestActivityByPage(userId, FEED_SIZE + 1, page * FEED_SIZE);
	}

	public List<Event> getLatestActivityByPageAndProjects(int userId, int page, Collection<Integer> projects) {
		return queries.getLatestActivityByPageAndProjects(userId, projects, FEED_SIZE + 1, page * FEED_SIZE);
	}

	public List<EventsCount> getUserActivityForProjects(int userId, Date fromDate, Collection<Integer> projectIds) {
		return projectIds.isEmpty() ? Collections.<EventsCount>emptyList() : queries.getUserActivityByProjects(
				userId, projectIds, fromDate);
	}

	public List<EventsCount> getUserActivity(int userId, Date fromDate) {
		return queries.getUserActivity(userId, fromDate);
	}
}
