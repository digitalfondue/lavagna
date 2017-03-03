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

import ch.digitalfondue.npjt.Bind;
import ch.digitalfondue.npjt.Query;
import ch.digitalfondue.npjt.QueryRepository;
import ch.digitalfondue.npjt.QueryType;
import io.lavagna.model.Event;

import java.util.Date;
import java.util.List;

@QueryRepository
public interface NotificationQuery {

	@Query(type = QueryType.TEMPLATE, value = "SELECT COUNT(EVENT_ID) COUNT_EVENT_ID, USER_ID FROM LA_EVENT INNER JOIN (SELECT USER_ID, CARD_ID_FK, USER_LAST_CHECKED FROM LA_CARD_LABEL "
			+ " INNER JOIN LA_CARD_LABEL_VALUE ON CARD_LABEL_ID = CARD_LABEL_ID_FK "
			+ " INNER JOIN LA_USER ON CARD_LABEL_VALUE_USER_FK = USER_ID "
			+ " WHERE CARD_LABEL_DOMAIN = 'SYSTEM' AND CARD_LABEL_NAME IN ('ASSIGNED', 'WATCHED_BY')) CARD_FOR_USER "
			+ " ON CARD_ID_FK = EVENT_CARD_ID_FK "
			+ " WHERE USER_LAST_CHECKED IS NULL OR EVENT_TIME >= USER_LAST_CHECKED GROUP BY USER_ID  HAVING COUNT(EVENT_ID) > 0")
	String countNewForUsersId();

	@Query(type = QueryType.TEMPLATE, value = "UPDATE LA_USER SET USER_LAST_CHECKPOINT_COUNT = USER_LAST_CHECKPOINT_COUNT + :count  WHERE  USER_ID = :userId")
	String updateCount();

	@Query("UPDATE LA_USER SET USER_LAST_CHECKED = :checkDate")
	int updateCheckDate(@Bind("checkDate") Date checkDate);

	@Query(type = QueryType.TEMPLATE, value = "SELECT USER_ID FROM LA_USER WHERE  USER_LAST_CHECKPOINT_COUNT > 0")
	String usersToNotify();

	@Query(type = QueryType.TEMPLATE, value = "UPDATE LA_USER SET USER_LAST_CHECKPOINT_COUNT = 0 WHERE USER_LAST_CHECKPOINT_COUNT > 0 ")
	String reset();

	@Query(type = QueryType.TEMPLATE, value = " AND USER_ID NOT IN  (:userWithChanges) ")
	String notIn();

	@Query("SELECT USER_LAST_EMAIL_SENT FROM LA_USER WHERE USER_ID = :userId")
	Date lastEmailSent(@Bind("userId") int userId);

	@Query("SELECT * FROM (SELECT DISTINCT CARD_ID_FK FROM LA_CARD_LABEL "
			+ " INNER JOIN LA_CARD_LABEL_VALUE ON CARD_LABEL_ID = CARD_LABEL_ID_FK "
			+ " WHERE CARD_LABEL_VALUE_USER_FK = :userId AND CARD_LABEL_DOMAIN = 'SYSTEM' AND "
			+ " CARD_LABEL_NAME IN ('ASSIGNED', 'WATCHED_BY') ) CARD_IDS "
			+ " INNER JOIN LA_EVENT ON CARD_ID_FK = EVENT_CARD_ID_FK WHERE EVENT_TIME BETWEEN :from AND :upTo ORDER BY EVENT_TIME ASC")
	List<Event> eventsForUser(@Bind("userId") int userId, @Bind("from") Date from, @Bind("upTo") Date upTo);

    @Query("SELECT * FROM (SELECT DISTINCT CARD_ID_FK FROM LA_CARD_LABEL "
        + " INNER JOIN LA_CARD_LABEL_VALUE ON CARD_LABEL_ID = CARD_LABEL_ID_FK "
        + " WHERE CARD_LABEL_VALUE_USER_FK = :userId AND CARD_LABEL_DOMAIN = 'SYSTEM' AND "
        + " CARD_LABEL_NAME IN ('ASSIGNED', 'WATCHED_BY') ) CARD_IDS "
        + " INNER JOIN LA_EVENT ON CARD_ID_FK = EVENT_CARD_ID_FK "
        + " WHERE EVENT_USER_ID_FK <> :userId AND EVENT_TIME BETWEEN :from AND :upTo ORDER BY EVENT_TIME ASC")
    List<Event> eventsForUserWithoutHisOwns(@Bind("userId") int userId, @Bind("from") Date from, @Bind("upTo") Date upTo);

	@Query("UPDATE LA_USER SET USER_LAST_EMAIL_SENT = :sentDate WHERE USER_ID = :userId")
	int updateSentEmailDate(@Bind("sentDate") Date sentDate, @Bind("userId") int userId);
}
