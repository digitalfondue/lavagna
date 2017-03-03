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

@QueryRepository
public interface SearchQuery {

	@Query(type = QueryType.TEMPLATE, value = "SELECT COUNT(LA_CARD.CARD_ID) FROM ")
	String findFirstSelectCount();

	@Query(type = QueryType.TEMPLATE, value = "SELECT LA_CARD.CARD_ID FROM ")
	String findFirstSelect();

	@Query(type = QueryType.TEMPLATE, value = "LA_CARD "
			+ " INNER JOIN LA_BOARD_COLUMN ON LA_CARD.CARD_BOARD_COLUMN_ID_FK = LA_BOARD_COLUMN.BOARD_COLUMN_ID "
			+ " INNER JOIN LA_BOARD_COLUMN_DEFINITION ON BOARD_COLUMN_DEFINITION_ID_FK = BOARD_COLUMN_DEFINITION_ID "
			+ " INNER JOIN LA_BOARD ON LA_BOARD.BOARD_ID = LA_BOARD_COLUMN.BOARD_COLUMN_BOARD_ID_FK "
			+ " INNER JOIN LA_PROJECT ON LA_BOARD.BOARD_PROJECT_ID_FK = LA_PROJECT.PROJECT_ID " + " INNER JOIN (")
	String findFirstFrom();

	@Query(type = QueryType.TEMPLATE, value = " ) AS CARD_R ON LA_CARD.CARD_ID = CARD_R.CARD_ID ")
	String findSecond();

	@Query(type = QueryType.TEMPLATE, value = " WHERE ")
	String findThirdWhere();

	@Query(type = QueryType.TEMPLATE, value = " LA_BOARD.BOARD_ID = ? ")
	String findFourthInBoardId();

	@Query(type = QueryType.TEMPLATE, value = " LA_BOARD.BOARD_PROJECT_ID_FK = ? ")
	String findInFifthProjectId();

	@Query(type = QueryType.TEMPLATE, value = " LA_BOARD.BOARD_PROJECT_ID_FK IN ")
	String findSixthRestrictedReadAccess();

	@Query(type = QueryType.TEMPLATE, value = " ORDER BY LA_CARD.CARD_LAST_UPDATED DESC ")
	String findSeventhOrderBy();

	@Query(type = QueryType.TEMPLATE, value = " LIMIT ? OFFSET ?")
	String findEighthLimit();

	@Query(type = QueryType.TEMPLATE, value = "SELECT LA_CARD.CARD_ID FROM LA_CARD LEFT JOIN  (")
	String findCardIdNotInOpen();

	@Query(type = QueryType.TEMPLATE, value = ") TO_EXCLUDE ON LA_CARD.CARD_ID = TO_EXCLUDE.CARD_ID WHERE TO_EXCLUDE.CARD_ID IS NULL")
	String findCardIdNotInClose();

	@Query(type = QueryType.TEMPLATE, value = "(SELECT CARD_ID FROM LA_CARD WHERE CARD_SEQ_NUMBER LIKE CONCAT(?, '%')) UNION (SELECT CARD_ID FROM LA_CARD WHERE LA_TEXT_SEARCH(CARD_NAME, ?)) "
			+ " UNION (SELECT CARD_DATA_CARD_ID_FK CARD_ID FROM LA_CARD_DATA WHERE CARD_DATA_DELETED = FALSE AND "
			+ " CARD_DATA_TYPE IN ('COMMENT', 'ACTION_LIST', 'ACTION_CHECKED', 'ACTION_UNCHECKED', 'DESCRIPTION') AND LA_TEXT_SEARCH_CLOB(CARD_DATA_CONTENT, ?))")
	@QueriesOverride({
			@QueryOverride(db = DB.MYSQL, value = "SELECT CARD_ID FROM (SELECT CARD_ID FROM LA_CARD WHERE CARD_SEQ_NUMBER LIKE CONCAT(?, '%') UNION "
					+ " SELECT CARD_ID FROM (SELECT CARD_FTS_SUPPORT_CARD_ID_FK CARD_ID FROM LA_CARD_FTS_SUPPORT WHERE MATCH (CARD_FTS_SUPPORT_CARD_NAME) AGAINST (? IN BOOLEAN MODE)) AS CARD_SEQ_AND_CARD_NAME"
					+ " UNION "
					+ " SELECT CARD_DATA_CARD_ID_FK CARD_ID FROM LA_CARD_DATA "
					+ " INNER JOIN LA_CARD_DATA_FTS_SUPPORT ON "
					+ " CARD_DATA_FTS_SUPPORT_CARD_DATA_ID_FK = CARD_DATA_ID "
					+ " WHERE CARD_DATA_DELETED <> TRUE AND "
					+ " CARD_DATA_TYPE IN ('COMMENT', 'ACTION_LIST', 'ACTION_CHECKED', 'ACTION_UNCHECKED', 'DESCRIPTION') AND "
					+ " MATCH(CARD_DATA_FTS_SUPPORT_CARD_DATA_CONTENT) AGAINST (? IN BOOLEAN MODE)) AS FTS_RES"),
			@QueryOverride(db = DB.PGSQL, value = "(SELECT CARD_ID FROM LA_CARD WHERE CAST(CARD_SEQ_NUMBER AS TEXT) LIKE CONCAT(?, '%')) UNION "
					+ " SELECT CARD_ID FROM LA_CARD WHERE card_name_tsvector @@ plainto_tsquery('english', unaccent(?)) UNION "
					+ " SELECT CARD_DATA_CARD_ID_FK CARD_ID FROM LA_CARD_DATA WHERE CARD_DATA_DELETED <> TRUE AND "
					+ " CARD_DATA_TYPE IN ('COMMENT', 'ACTION_LIST', 'ACTION_CHECKED', 'ACTION_UNCHECKED', 'DESCRIPTION') AND "
					+ " card_data_content_tsvector @@ plainto_tsquery('english', unaccent(?))") })
	String findByFreeText();

	@Query(type = QueryType.TEMPLATE, value = "SELECT CARD_ID FROM LA_CARD INNER JOIN LA_BOARD_COLUMN ON CARD_BOARD_COLUMN_ID_FK = BOARD_COLUMN_ID "
			+ " INNER JOIN LA_BOARD_COLUMN_DEFINITION ON BOARD_COLUMN_DEFINITION_ID_FK = BOARD_COLUMN_DEFINITION_ID "
			+ " WHERE BOARD_COLUMN_DEFINITION_VALUE = ?")
	String findByStatus();

	@Query(type = QueryType.TEMPLATE, value = "SELECT CARD_ID FROM LA_CARD INNER JOIN LA_BOARD_COLUMN ON CARD_BOARD_COLUMN_ID_FK = BOARD_COLUMN_ID "
			+ " INNER JOIN LA_BOARD ON LA_BOARD.BOARD_ID = LA_BOARD_COLUMN.BOARD_COLUMN_BOARD_ID_FK "
			+ " WHERE BOARD_ARCHIVED = ?")
	String findByBoardStatus();

	@Query(type = QueryType.TEMPLATE, value = "SELECT CARD_ID FROM LA_CARD INNER JOIN LA_BOARD_COLUMN ON CARD_BOARD_COLUMN_ID_FK = BOARD_COLUMN_ID "
			+ " INNER JOIN LA_BOARD_COLUMN_DEFINITION ON BOARD_COLUMN_DEFINITION_ID_FK = BOARD_COLUMN_DEFINITION_ID "
			+ " WHERE BOARD_COLUMN_LOCATION = ?")
	String findByLocation();

    @Query(type = QueryType.TEMPLATE, value = "SELECT CARD_ID FROM LA_CARD INNER JOIN LA_BOARD_COLUMN ON CARD_BOARD_COLUMN_ID_FK = BOARD_COLUMN_ID "
        + " INNER JOIN LA_BOARD_COLUMN_DEFINITION ON BOARD_COLUMN_DEFINITION_ID_FK = BOARD_COLUMN_DEFINITION_ID "
        + " WHERE BOARD_COLUMN_LOCATION <> ?")
    String findByNotLocation();

	@Query(type = QueryType.TEMPLATE, value = "SELECT CARD_ID_FK AS CARD_ID FROM  LA_CARD_LABEL_VALUE "
			+ " INNER JOIN LA_CARD_LABEL ON CARD_LABEL_ID = CARD_LABEL_ID_FK "
			+ " WHERE CARD_LABEL_VALUE_DELETED <> TRUE AND CARD_LABEL_DOMAIN = 'SYSTEM' AND CARD_LABEL_NAME = ?")
	String findBySystemLabel();

	@Query(type = QueryType.TEMPLATE, value = "SELECT CARD_ID_FK AS CARD_ID FROM  LA_CARD_LABEL_VALUE "
			+ " INNER JOIN LA_CARD_LABEL ON CARD_LABEL_ID = CARD_LABEL_ID_FK "
			+ " WHERE CARD_LABEL_VALUE_DELETED <> TRUE AND CARD_LABEL_DOMAIN = 'USER' AND CARD_LABEL_NAME LIKE CONCAT(? ,'%')")
	String findByUserLabel();

	@Query(type = QueryType.TEMPLATE, value = "  AND (CASE "
			+ " WHEN CARD_LABEL_VALUE_TYPE = 'STRING' THEN CARD_LABEL_VALUE_STRING LIKE CONCAT(?, '%') "
			+ " WHEN CARD_LABEL_VALUE_TYPE = 'INT' THEN CARD_LABEL_VALUE_INT = ? "
			+ " WHEN CARD_LABEL_VALUE_TYPE = 'TIMESTAMP' THEN CARD_LABEL_VALUE_TIMESTAMP BETWEEN ? AND ? "
			+ " WHEN CARD_LABEL_VALUE_TYPE = 'USER' THEN CARD_LABEL_VALUE_USER_FK = ? "
			+ " WHEN CARD_LABEL_VALUE_TYPE = 'CARD' THEN CARD_LABEL_VALUE_CARD_FK = ? "
			+ " WHEN CARD_LABEL_VALUE_TYPE = 'LIST' THEN (CARD_LABEL_ID_FK, CARD_LABEL_VALUE_LIST_VALUE_FK) IN (SELECT CARD_LABEL_ID_FK, CARD_LABEL_LIST_VALUE_ID FROM LA_CARD_LABEL_LIST_VALUE WHERE CARD_LABEL_LIST_VALUE = ?) "
			+ " END)")
	String andLabelValueString();

	@Query(type = QueryType.TEMPLATE, value = " AND CARD_LABEL_VALUE_TIMESTAMP >= ? AND CARD_LABEL_VALUE_TIMESTAMP < ?")
	String andLabelValueDate();

	@Query(type = QueryType.TEMPLATE, value = " AND CARD_LABEL_VALUE_USER_FK = ? ")
	String andLabelValueUser();

	@Query(type = QueryType.TEMPLATE, value = " AND (CARD_LABEL_ID_FK, CARD_LABEL_VALUE_LIST_VALUE_FK) IN (SELECT CARD_LABEL_ID_FK, CARD_LABEL_LIST_VALUE_ID FROM LA_CARD_LABEL_LIST_VALUE WHERE CARD_LABEL_LIST_VALUE = ?) ")
	String andLabelListValueEq();

	@Query(type = QueryType.TEMPLATE, value = " SELECT CARD_ID FROM LA_CARD INNER JOIN LA_EVENT ON CARD_ID = EVENT_CARD_ID_FK WHERE EVENT_TYPE = 'CARD_CREATE' AND EVENT_TIME BETWEEN ? AND ? ")
	String findByCardCreationEventDate();

	@Query(type = QueryType.TEMPLATE, value = " SELECT CARD_ID FROM LA_CARD INNER JOIN LA_EVENT ON CARD_ID = EVENT_CARD_ID_FK WHERE EVENT_TYPE = 'CARD_CREATE' AND EVENT_USER_ID_FK = ? ")
	String findByCardCreationEventUser();

	@Query(type = QueryType.TEMPLATE, value = " SELECT CARD_ID FROM LA_CARD WHERE CARD_LAST_UPDATED BETWEEN ? AND ? ")
	String findByUpdated();

	@Query(type = QueryType.TEMPLATE, value = " SELECT CARD_ID FROM LA_CARD WHERE CARD_LAST_UPDATED_USER_ID_FK = ? ")
	String findByUpdatedBy();
}
