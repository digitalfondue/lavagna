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
import io.lavagna.model.Card;
import io.lavagna.model.CardFull;
import io.lavagna.model.Event;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@QueryRepository
public interface CardQuery {

	@Query("INSERT INTO LA_CARD(CARD_NAME, CARD_BOARD_COLUMN_ID_FK, CARD_ORDER, CARD_USER_ID_FK, CARD_SEQ_NUMBER, CARD_LAST_UPDATED, CARD_LAST_UPDATED_USER_ID_FK) VALUES "
			+ " (:name, :columnId, (SELECT * FROM (SELECT COALESCE(MAX(CARD_ORDER), 0) + 1 FROM LA_CARD WHERE CARD_BOARD_COLUMN_ID_FK = :columnId) AS MAX_CARD_ORDER), :userId, :cardSequence, NOW(), :userId)")
	int createCard(@Bind("name") String name, @Bind("columnId") int columnId, @Bind("userId") int userId,
			@Bind("cardSequence") int cardSequence);

	@Query("SELECT CARD_ID, CARD_NAME, CARD_BOARD_COLUMN_ID_FK, CARD_USER_ID_FK, CARD_ORDER, CARD_SEQ_NUMBER FROM LA_CARD_WITH_BOARD_ID WHERE BOARD_ID = :boardId AND "
			+ " BOARD_COLUMN_LOCATION = :location " + " ORDER BY CARD_ORDER ASC, CARD_NAME ASC")
	List<Card> findAllByBoardIdAndLocation(@Bind("boardId") int boardId, @Bind("location") String location);

	@Query("SELECT CARD_ID FROM LA_CARD WHERE CARD_ID IN (:cardIds) AND CARD_BOARD_COLUMN_ID_FK = :columnId")
	List<Integer> findCardIdsInColumnId(@Bind("cardIds") List<Integer> cardIds, @Bind("columnId") int columnId);

    @Query("SELECT CARD_ID FROM LA_CARD WHERE CARD_BOARD_COLUMN_ID_FK = :columnId")
    List<Integer> findCardIdsByColumnId(@Bind("columnId") int columnId);

	@Query("SELECT * FROM LA_CARD_FULL WHERE BOARD_SHORT_NAME = :boardShortName")
	List<CardFull> findAllByBoardShortName(@Bind("boardShortName") String boardShortName);


	//----------------------

	String FIND_CARD_BY_BOARD_SHORT_NAME = ":boardShortName is not null AND BOARD_SHORT_NAME LIKE CONCAT('%', CONCAT(:boardShortName, '%')) ";
	String FIND_CARD_BY_SEQ_NR = " AND (:sequenceNr IS NULL OR CARD_SEQ_NUMBER LIKE CONCAT(:sequenceNr, '%'))";
	String FIND_CARD_BY_SEQ_NR_PGSQL = " AND (:sequenceNr IS NULL OR CAST(CARD_SEQ_NUMBER AS TEXT) LIKE CONCAT(:sequenceNr, '%'))";

	@Query("SELECT * FROM LA_CARD_FULL WHERE (LOWER(CARD_NAME) LIKE CONCAT('%', CONCAT(LOWER(:term), '%')) OR CARD_SEQ_NUMBER LIKE CONCAT(:term, '%')"
			+ " OR (" + FIND_CARD_BY_BOARD_SHORT_NAME + FIND_CARD_BY_SEQ_NR + ")) AND PROJECT_ID IN (:projectIdFilter) ORDER BY BOARD_SHORT_NAME ASC, CARD_SEQ_NUMBER ASC LIMIT 10")
	@QueriesOverride({
		@QueryOverride(db = DB.PGSQL, value = "SELECT * FROM LA_CARD_FULL WHERE (LOWER(CARD_NAME) LIKE CONCAT('%', CONCAT(LOWER(:term), '%')) OR CAST(CARD_SEQ_NUMBER AS TEXT) LIKE CONCAT(:term, '%')"
				+ " OR (" + FIND_CARD_BY_BOARD_SHORT_NAME + ")) AND PROJECT_ID IN (:projectIdFilter) ORDER BY BOARD_SHORT_NAME ASC, CARD_SEQ_NUMBER ASC LIMIT 10")
	})
	List<CardFull> findCardBy(@Bind("term") String term, @Bind("boardShortName") String maybeBoardShortName, @Bind("sequenceNr") Integer maybeSequenceNumber,
			@Bind("projectIdFilter") Set<Integer> projectIdFilter);

	@Query("SELECT * FROM LA_CARD_FULL WHERE :term IS NOT NULL AND ((LOWER(CARD_NAME) LIKE CONCAT('%', CONCAT(LOWER(:term), '%')) OR CARD_SEQ_NUMBER LIKE CONCAT(:term, '%')"
			+ " OR (" + FIND_CARD_BY_BOARD_SHORT_NAME + FIND_CARD_BY_SEQ_NR + "))) ORDER BY BOARD_SHORT_NAME ASC, CARD_SEQ_NUMBER ASC LIMIT 10")
	@QueriesOverride({
		@QueryOverride(db = DB.PGSQL, value = "SELECT * FROM LA_CARD_FULL WHERE :term IS NOT NULL AND ((LOWER(CARD_NAME) LIKE CONCAT('%', CONCAT(LOWER(:term), '%')) OR CAST(CARD_SEQ_NUMBER AS TEXT) LIKE CONCAT(:term, '%') "
				+ " OR (" + FIND_CARD_BY_BOARD_SHORT_NAME + "))) ORDER BY BOARD_SHORT_NAME ASC, CARD_SEQ_NUMBER ASC LIMIT 10")
	})
	List<CardFull> findCardBy(@Bind("term") String term, @Bind("boardShortName") String maybeBoardShortName, @Bind("sequenceNr") Integer maybeSequenceNumber);

	//----------------------

	@Query("SELECT CARD_ID FROM LA_CARD "
			+ " INNER JOIN LA_BOARD_COLUMN ON CARD_BOARD_COLUMN_ID_FK = BOARD_COLUMN_ID  "
			+ " WHERE "
			+ " BOARD_COLUMN_BOARD_ID_FK = :boardId AND "
			+ " BOARD_COLUMN_LOCATION = :location "
			+ " ORDER BY CARD_LAST_UPDATED DESC " + " LIMIT :amount OFFSET :offset ")
	List<Integer> fetchPaginatedByBoardIdAndLocation(@Bind("boardId") int boardId, @Bind("location") String location,
			@Bind("amount") int amount, @Bind("offset") int offset);

	@Query("SELECT * FROM LA_CARD_FULL WHERE CARD_BOARD_COLUMN_ID_FK = :columnId ORDER BY CARD_ORDER ASC, CARD_NAME ASC")
	List<CardFull> findAllFullByColumnId(@Bind("columnId") int columnId);

	@Query("SELECT * FROM LA_CARD_FULL WHERE CARD_ID IN (:ids)")
	List<CardFull> findAllByIds(@Bind("ids") Collection<Integer> ids);

	@Query("SELECT CARD_ID, CARD_NAME,CARD_BOARD_COLUMN_ID_FK, CARD_ORDER, CARD_USER_ID_FK, CARD_SEQ_NUMBER FROM LA_CARD_WITH_BOARD_ID WHERE BOARD_ID = :boardId AND CARD_NAME LIKE CONCAT('%', :criteria,'%') ORDER BY CARD_NAME")
	List<Card> findCards(@Bind("boardId") int boardId, @Bind("criteria") String criteria);

	@Query("SELECT CARD_ID, CARD_NAME, CARD_BOARD_COLUMN_ID_FK, CARD_ORDER, CARD_USER_ID_FK, CARD_SEQ_NUMBER FROM LA_CARD WHERE CARD_ID = :cardId")
	Card findBy(@Bind("cardId") int cardId);

	@Query("SELECT CARD_ID, CARD_NAME, CARD_SEQ_NUMBER, CARD_ORDER, CARD_BOARD_COLUMN_ID_FK, CREATE_USER, CREATE_TIME, LAST_UPDATE_USER, LAST_UPDATE_TIME, BOARD_COLUMN_DEFINITION_VALUE, BOARD_SHORT_NAME, PROJECT_SHORT_NAME FROM LA_CARD_FULL WHERE CARD_ID = :cardId")
	CardFull findFullBy(@Bind("cardId") int cardId);

	@Query("SELECT CARD_ID, CARD_NAME, CARD_SEQ_NUMBER, CARD_ORDER, CARD_BOARD_COLUMN_ID_FK, CREATE_USER, CREATE_TIME, LAST_UPDATE_USER, LAST_UPDATE_TIME, BOARD_COLUMN_DEFINITION_VALUE, BOARD_SHORT_NAME, PROJECT_SHORT_NAME FROM LA_CARD_FULL "
			+ " WHERE CARD_SEQ_NUMBER = :seqNumber AND BOARD_SHORT_NAME = :shortName")
	CardFull findFullBy(@Bind("shortName") String shortName, @Bind("seqNumber") int seqNumber);

	@Query("SELECT CARD_ID FROM LA_CARD "
			+ " INNER JOIN LA_BOARD_COLUMN ON LA_BOARD_COLUMN.BOARD_COLUMN_ID = LA_CARD.CARD_BOARD_COLUMN_ID_FK "
			+ " INNER JOIN LA_BOARD ON LA_BOARD.BOARD_ID = LA_BOARD_COLUMN.BOARD_COLUMN_BOARD_ID_FK WHERE "
			+ " LA_CARD.CARD_SEQ_NUMBER = :seqNumber AND LA_BOARD.BOARD_SHORT_NAME = :shortName")
	Integer findCardIdByBoardNameAndSeq(@Bind("shortName") String shortName, @Bind("seqNumber") int seqNumber);


	@Query("SELECT COUNT(CARD_ID) FROM LA_CARD "
			+ " INNER JOIN LA_BOARD_COLUMN ON LA_BOARD_COLUMN.BOARD_COLUMN_ID = LA_CARD.CARD_BOARD_COLUMN_ID_FK "
			+ " INNER JOIN LA_BOARD ON LA_BOARD.BOARD_ID = LA_BOARD_COLUMN.BOARD_COLUMN_BOARD_ID_FK WHERE "
			+ " LA_CARD.CARD_SEQ_NUMBER = :seqNumber AND LA_BOARD.BOARD_SHORT_NAME = :shortName")
	Integer countCardIdByBoardNameAndSeq(@Bind("shortName") String shortName, @Bind("seqNumber") int seqNumber);

	@Query("UPDATE LA_CARD SET CARD_NAME = :name WHERE CARD_ID = :cardId")
	int updateCard(@Bind("name") String name, @Bind("cardId") int cardId);

	@Query("SELECT CARD_ID, CARD_NAME, CARD_BOARD_COLUMN_ID_FK, CARD_ORDER, CARD_USER_ID_FK, CARD_SEQ_NUMBER FROM LA_CARD WHERE CARD_ID = IDENTITY()")
	@QueriesOverride({
			@QueryOverride(db = DB.MYSQL, value = "SELECT CARD_ID, CARD_NAME, CARD_BOARD_COLUMN_ID_FK, CARD_ORDER, CARD_USER_ID_FK, CARD_SEQ_NUMBER FROM LA_CARD WHERE CARD_ID = LAST_INSERT_ID()"),
			@QueryOverride(db = DB.PGSQL, value = "SELECT CARD_ID, CARD_NAME, CARD_BOARD_COLUMN_ID_FK, CARD_ORDER, CARD_USER_ID_FK, CARD_SEQ_NUMBER FROM LA_CARD WHERE CARD_ID = (SELECT CURRVAL(pg_get_serial_sequence('la_card','card_id')))") })
	Card findLastCreatedCard();

	@Query(type = QueryType.TEMPLATE, value = "UPDATE LA_CARD SET CARD_BOARD_COLUMN_ID_FK = :columnId WHERE CARD_ID = :cardId AND CARD_BOARD_COLUMN_ID_FK = :previousColumnId")
	String moveCardToColumn();

	@Query(type = QueryType.TEMPLATE, value = "UPDATE LA_CARD SET CARD_ORDER = :cardOrder WHERE CARD_ID = :cardId AND CARD_BOARD_COLUMN_ID_FK = :columnId")
	String updateCardOrder();

	@Query("UPDATE LA_CARD SET CARD_ORDER = :order WHERE CARD_ID = :cardId")
	int updateCardOrder(@Bind("cardId") int cardId, @Bind("order") int order);

	@Query("UPDATE LA_CARD SET CARD_ORDER = CARD_ORDER + 1 WHERE CARD_BOARD_COLUMN_ID_FK = :columnId")
	int incrementCardsOrder(@Bind("columnId") int columnId);

	@Query("SELECT BOARD_COUNTER_CARD_SEQUENCE FROM LA_BOARD_COUNTER WHERE BOARD_COUNTER_ID_FK = (SELECT BOARD_COLUMN_BOARD_ID_FK FROM LA_BOARD_COLUMN WHERE BOARD_COLUMN_ID = :columnId) FOR UPDATE")
	Integer fetchAndLockCardSequence(@Bind("columnId") int columnId);

	@Query("UPDATE LA_BOARD_COUNTER SET BOARD_COUNTER_CARD_SEQUENCE = BOARD_COUNTER_CARD_SEQUENCE + 1 WHERE BOARD_COUNTER_CARD_SEQUENCE = :expectedSequenceValue AND BOARD_COUNTER_ID_FK = (SELECT BOARD_COLUMN_BOARD_ID_FK FROM LA_BOARD_COLUMN WHERE BOARD_COLUMN_ID = :columnId)")
	int incrementSequence(@Bind("expectedSequenceValue") int expectedSequenceValue, @Bind("columnId") int columnId);

	@Query("SELECT * FROM LA_EVENT WHERE EVENT_CARD_ID_FK = :cardId ORDER BY EVENT_TIME DESC")
	List<Event> fetchAllActivityByCardId(@Bind("cardId") int cardId);

	@Query(type = QueryType.TEMPLATE, value = "SELECT CONCAT(CONCAT(BOARD_SHORT_NAME, '-'), CARD_SEQ_NUMBER) AS CARD_IDENTIFIER, CARD_ID FROM LA_CARD "
			+ " INNER JOIN LA_BOARD_COLUMN ON CARD_BOARD_COLUMN_ID_FK = BOARD_COLUMN_ID "
			+ " INNER JOIN LA_BOARD ON BOARD_COLUMN_BOARD_ID_FK = BOARD_ID WHERE (BOARD_SHORT_NAME, CARD_SEQ_NUMBER) IN (:projShortNameAndCardSeq)")
	String findCardsIs();

	@Query("select CARD_BOARD_COLUMN_ID_FK from LA_CARD where CARD_ID = :cardId")
	Integer findColumnIdById(@Bind("cardId") int cardId);
}
