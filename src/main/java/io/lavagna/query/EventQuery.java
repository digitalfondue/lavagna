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
package io.lavagna.query;

import ch.digitalfondue.npjt.*;
import io.lavagna.model.Event;
import io.lavagna.model.EventsCount;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@QueryRepository
public interface EventQuery {

	@Query("SELECT * FROM LA_EVENT WHERE EVENT_ID = IDENTITY()")
	@QueriesOverride({
			@QueryOverride(db = DB.MYSQL, value = "SELECT * FROM LA_EVENT WHERE EVENT_ID = LAST_INSERT_ID()"),
			@QueryOverride(db = DB.PGSQL, value = "SELECT * FROM LA_EVENT WHERE EVENT_ID = (SELECT CURRVAL(pg_get_serial_sequence('la_event','event_id')))") })
	Event findLastCreated();

	@Query("SELECT * FROM LA_EVENT WHERE EVENT_ID = :id")
	Event getById(@Bind("id") int id);

	@Query("SELECT * FROM LA_EVENT ORDER BY EVENT_ID ASC LIMIT :amount OFFSET :offset ")
	List<Event> find(@Bind("offset") int offset, @Bind("amount") int amount);

	@Query("SELECT COUNT(EVENT_ID) FROM LA_EVENT")
	Integer count();

	@Query("SELECT * FROM LA_EVENT WHERE EVENT_CARD_DATA_ID_FK = :cardDataId AND EVENT_ID > :eventId AND EVENT_TYPE = :eventType ORDER BY EVENT_ID ASC LIMIT 1")
	List<Event> findNextEventFor(@Bind("cardDataId") int cardDataId, @Bind("eventId") int eventId,
			@Bind("eventType") String eventType);

	@Query("INSERT INTO LA_EVENT(EVENT_LABEL_NAME, EVENT_LABEL_TYPE, EVENT_CARD_ID_FK, EVENT_USER_ID_FK, EVENT_TIME, EVENT_TYPE, EVENT_VALUE_INT, EVENT_VALUE_STRING, EVENT_VALUE_TIMESTAMP, EVENT_VALUE_CARD_FK, EVENT_VALUE_USER_FK) "
			+ " VALUES (:labelName, :labelType, :cardId, :userId, :now, :event, :valueInt, :valueString, :valueTimestamp, :valueCard, :valueUser)")
	int insertLabelEvent(@Bind("labelName") String labelName, @Bind("labelType") String labelType,
			@Bind("cardId") int cardId, @Bind("userId") int userId, @Bind("now") Date now, @Bind("event") String event,
			@Bind("valueInt") Integer valueInt, @Bind("valueString") String valueString,
			@Bind("valueTimestamp") Date valueTimestamp, @Bind("valueCard") Integer valueCard,
			@Bind("valueUser") Integer valueUser);

	@Query(type = QueryType.TEMPLATE, value = "INSERT INTO LA_EVENT(EVENT_CARD_ID_FK, EVENT_PREV_COLUMN_ID_FK, EVENT_COLUMN_ID_FK, EVENT_USER_ID_FK, EVENT_TIME, EVENT_TYPE, EVENT_VALUE_STRING) "
			+ " VALUES (:cardId, :previousColumnId, :columnId, :userId, :time, :event, :valueString)")
	String insertCardEvent();

	@Query("INSERT INTO LA_EVENT(EVENT_CARD_DATA_ID_FK, EVENT_CARD_ID_FK, EVENT_USER_ID_FK, EVENT_TIME, EVENT_TYPE, EVENT_PREV_CARD_DATA_ID_FK, EVENT_NEW_CARD_DATA_ID_FK) "
			+ " VALUES (:cardDataId, :cardId, :userId, :time, :event, :referenceId, :newReferenceId)")
	int insertCardDataEvent(@Bind("cardDataId") int cardDataId, @Bind("cardId") int cardId, @Bind("userId") int userId,
			@Bind("time") Date time, @Bind("event") String event, @Bind("referenceId") Integer referenceId,
			@Bind("newReferenceId") Integer newReferenceId);

	@Query("INSERT INTO LA_EVENT(EVENT_CARD_DATA_ID_FK, EVENT_CARD_ID_FK, EVENT_USER_ID_FK, EVENT_TIME, EVENT_TYPE, EVENT_PREV_CARD_DATA_ID_FK, EVENT_VALUE_STRING) "
			+ " VALUES (:cardDataId, :cardId, :userId, :time, :event, :referenceId, :name)")
	int insertFileEvent(@Bind("cardDataId") int cardDataId, @Bind("cardId") int cardId, @Bind("userId") int userId,
			@Bind("time") Date time, @Bind("event") String event, @Bind("referenceId") Integer referenceId,
			@Bind("name") String name);

	@Query("SELECT EVENT_USER_ID_FK FROM LA_EVENT WHERE EVENT_CARD_DATA_ID_FK = :cardDataId AND EVENT_TYPE = :event")
	List<Integer> findUsersIdForCardData(@Bind("cardDataId") int cardDataId, @Bind("event") String event);

	@Query("DELETE FROM LA_EVENT WHERE EVENT_ID = :id AND EVENT_CARD_ID_FK = :cardId AND EVENT_TYPE = :event")
	int remove(@Bind("id") int id, @Bind("cardId") int cardId, @Bind("event") String event);

	// Profile

    @Query("SELECT EVENT_ID, EVENT_CARD_ID_FK, EVENT_USER_ID_FK, EVENT_TYPE, EVENT_TIME, EVENT_CARD_DATA_ID_FK,"
        + " EVENT_PREV_CARD_DATA_ID_FK, EVENT_NEW_CARD_DATA_ID_FK, EVENT_COLUMN_ID_FK, EVENT_PREV_COLUMN_ID_FK, EVENT_LABEL_NAME, EVENT_LABEL_TYPE, EVENT_VALUE_INT,"
        + " EVENT_VALUE_STRING, EVENT_VALUE_TIMESTAMP, EVENT_VALUE_CARD_FK, EVENT_VALUE_USER_FK FROM LA_EVENT"
        + " WHERE EVENT_USER_ID_FK = :userId AND EVENT_TIME >= :fromDate ORDER BY EVENT_TIME DESC")
    List<Event> getLatestActivity(@Bind("userId") int user, @Bind("fromDate") Date fromDate);

    @Query("SELECT EVENT_ID, EVENT_CARD_ID_FK, EVENT_USER_ID_FK, EVENT_TYPE, EVENT_TIME, EVENT_CARD_DATA_ID_FK,"
        + " EVENT_PREV_CARD_DATA_ID_FK, EVENT_NEW_CARD_DATA_ID_FK, EVENT_COLUMN_ID_FK, EVENT_PREV_COLUMN_ID_FK, EVENT_LABEL_NAME, EVENT_LABEL_TYPE, EVENT_VALUE_INT,"
        + " EVENT_VALUE_STRING, EVENT_VALUE_TIMESTAMP, EVENT_VALUE_CARD_FK, EVENT_VALUE_USER_FK"
        + " FROM LA_EVENT INNER JOIN LA_CARD ON LA_EVENT.EVENT_CARD_ID_FK = LA_CARD.CARD_ID"
        + " INNER JOIN LA_BOARD_COLUMN ON LA_BOARD_COLUMN.BOARD_COLUMN_ID = LA_CARD.CARD_BOARD_COLUMN_ID_FK"
        + " INNER JOIN LA_BOARD ON LA_BOARD_COLUMN.BOARD_COLUMN_BOARD_ID_FK = LA_BOARD.BOARD_ID AND LA_BOARD.BOARD_PROJECT_ID_FK in (:projects)"
        + " WHERE EVENT_USER_ID_FK = :userId AND EVENT_TIME >= :fromDate ORDER BY EVENT_TIME DESC")
    List<Event> getLatestActivityByProjects(@Bind("userId") int user,
        @Bind("projects") Collection<Integer> projects, @Bind("fromDate") Date fromDate);

	@Query("SELECT EVENT_ID, EVENT_CARD_ID_FK, EVENT_USER_ID_FK, EVENT_TYPE, EVENT_TIME, EVENT_CARD_DATA_ID_FK,"
			+ " EVENT_PREV_CARD_DATA_ID_FK, EVENT_NEW_CARD_DATA_ID_FK, EVENT_COLUMN_ID_FK, EVENT_PREV_COLUMN_ID_FK, EVENT_LABEL_NAME, EVENT_LABEL_TYPE, EVENT_VALUE_INT,"
			+ " EVENT_VALUE_STRING, EVENT_VALUE_TIMESTAMP, EVENT_VALUE_CARD_FK, EVENT_VALUE_USER_FK FROM LA_EVENT"
			+ " WHERE EVENT_USER_ID_FK = :userId ORDER BY EVENT_TIME DESC LIMIT :amount OFFSET :offset")
	List<Event> getLatestActivityByPage(@Bind("userId") int user, @Bind("amount") int amount, @Bind("offset") int offset);

	@Query("SELECT EVENT_ID, EVENT_CARD_ID_FK, EVENT_USER_ID_FK, EVENT_TYPE, EVENT_TIME, EVENT_CARD_DATA_ID_FK,"
			+ " EVENT_PREV_CARD_DATA_ID_FK, EVENT_NEW_CARD_DATA_ID_FK, EVENT_COLUMN_ID_FK, EVENT_PREV_COLUMN_ID_FK, EVENT_LABEL_NAME, EVENT_LABEL_TYPE, EVENT_VALUE_INT,"
			+ " EVENT_VALUE_STRING, EVENT_VALUE_TIMESTAMP, EVENT_VALUE_CARD_FK, EVENT_VALUE_USER_FK"
			+ " FROM LA_EVENT INNER JOIN LA_CARD ON LA_EVENT.EVENT_CARD_ID_FK = LA_CARD.CARD_ID"
			+ " INNER JOIN LA_BOARD_COLUMN ON LA_BOARD_COLUMN.BOARD_COLUMN_ID = LA_CARD.CARD_BOARD_COLUMN_ID_FK"
			+ " INNER JOIN LA_BOARD ON LA_BOARD_COLUMN.BOARD_COLUMN_BOARD_ID_FK = LA_BOARD.BOARD_ID AND LA_BOARD.BOARD_PROJECT_ID_FK in (:projects)"
			+ " WHERE EVENT_USER_ID_FK = :userId ORDER BY EVENT_TIME DESC LIMIT :amount OFFSET :offset")
	List<Event> getLatestActivityByPageAndProjects(@Bind("userId") int user,
			@Bind("projects") Collection<Integer> projects, @Bind("amount") int amount, @Bind("offset") int offset);

	@Query("SELECT CAST(EVENT_TIME AS DATE) AS EVENT_DATE, COUNT(*) AS EVENT_COUNT FROM LA_EVENT "
			+ " WHERE EVENT_USER_ID_FK = :userId AND EVENT_TIME >= :fromDate  GROUP BY EVENT_DATE ORDER BY EVENT_DATE")
	List<EventsCount> getUserActivity(@Bind("userId") int userId, @Bind("fromDate") Date fromDate);

	@Query("SELECT CAST(EVENT_TIME AS DATE) AS EVENT_DATE, COUNT(*) AS EVENT_COUNT FROM LA_EVENT "
			+ " INNER JOIN LA_CARD_FULL ON EVENT_CARD_ID_FK = LA_CARD_FULL.CARD_ID AND PROJECT_ID IN (:projectIds) "
			+ " WHERE EVENT_USER_ID_FK = :userId AND EVENT_TIME >= :fromDate GROUP BY EVENT_DATE ORDER BY EVENT_DATE")
	List<EventsCount> getUserActivityByProjects(@Bind("userId") int userId,
			@Bind("projectIds") Collection<Integer> projectIds, @Bind("fromDate") Date fromDate);

}
